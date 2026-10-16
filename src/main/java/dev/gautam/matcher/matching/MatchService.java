package dev.gautam.matcher.matching;

import dev.gautam.matcher.common.InvalidRequestException;
import dev.gautam.matcher.common.NotFoundException;
import dev.gautam.matcher.config.MatcherProperties;
import dev.gautam.matcher.job.JobDescription;
import dev.gautam.matcher.job.JobRepository;
import dev.gautam.matcher.job.JobService;
import dev.gautam.matcher.resume.Resume;
import dev.gautam.matcher.resume.ResumeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class MatchService {

    private static final Logger log = LoggerFactory.getLogger(MatchService.class);

    private final JobService jobService;
    private final JobRepository jobRepository;
    private final ResumeRepository resumeRepository;
    private final MatchResultRepository matchResultRepository;
    private final VectorStore vectorStore;
    private final ChunkGrouper chunkGrouper;
    private final RankingPromptBuilder rankingPromptBuilder;
    private final ScoreCalculator scoreCalculator;
    private final ChatClient chatClient;
    private final MatcherProperties properties;
    private final TransactionTemplate transactions;

    public MatchService(
            JobService jobService,
            JobRepository jobRepository,
            ResumeRepository resumeRepository,
            MatchResultRepository matchResultRepository,
            VectorStore vectorStore,
            ChunkGrouper chunkGrouper,
            RankingPromptBuilder rankingPromptBuilder,
            ScoreCalculator scoreCalculator,
            ChatClient chatClient,
            MatcherProperties properties,
            PlatformTransactionManager transactionManager) {
        this.jobService = jobService;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
        this.matchResultRepository = matchResultRepository;
        this.vectorStore = vectorStore;
        this.chunkGrouper = chunkGrouper;
        this.rankingPromptBuilder = rankingPromptBuilder;
        this.scoreCalculator = scoreCalculator;
        this.chatClient = chatClient;
        this.properties = properties;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    public List<MatchResponse> match(UUID jobId, int topK) {
        int resolvedTopK = resolveTopK(topK);
        JobDescription job = jobService.require(jobId);
        List<CandidateEvidence> top = retrieve(job, resolvedTopK, null).stream()
                .limit(resolvedTopK)
                .toList();
        List<ScoredCandidate> scored = scoreCandidates(job, top);
        List<MatchResponse> saved = transactions.execute(status -> replaceResults(jobId, scored));
        log.info("Ranked {} candidate(s) for job {}", saved == null ? 0 : saved.size(), jobId);
        return saved == null ? List.of() : saved;
    }

    public MatchResponse explain(UUID jobId, UUID resumeId) {
        return matchResultRepository.findFirstByJob_IdAndResume_IdOrderByCreatedAtDesc(jobId, resumeId)
                .map(result -> transactions.execute(status -> MatchResponse.from(
                        matchResultRepository.findById(result.getId()).orElseThrow())))
                .orElseGet(() -> computeOne(jobId, resumeId));
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> list(UUID jobId) {
        jobService.require(jobId);
        return matchResultRepository.findByJob_IdOrderByScoreDescSimilarityDesc(jobId).stream()
                .map(MatchResponse::from)
                .toList();
    }

    private MatchResponse computeOne(UUID jobId, UUID resumeId) {
        JobDescription job = jobService.require(jobId);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new NotFoundException("Resume not found: " + resumeId));
        List<CandidateEvidence> evidence = retrieve(job, 1, resumeId);
        CandidateEvidence chosen = evidence.isEmpty()
                ? new CandidateEvidence(resumeId, resume.getCandidateName(), 0d, resume.getRawText())
                : evidence.getFirst();
        ScoredCandidate scored = scoreOne(job, chosen);
        return transactions.execute(status -> {
            JobDescription managedJob = jobRepository.findById(jobId).orElseThrow();
            Resume managedResume = resumeRepository.findById(resumeId).orElseThrow();
            MatchResult saved = matchResultRepository.save(toEntity(managedJob, managedResume, scored));
            return MatchResponse.from(saved);
        });
    }

    private List<CandidateEvidence> retrieve(JobDescription job, int topK, UUID resumeId) {
        int fetch = Math.max(properties.getRetrievalTopChunks(), topK * 4);
        SearchRequest.Builder request = SearchRequest.builder()
                .query(job.getTitle() + "\n" + job.getDescription())
                .topK(fetch)
                .similarityThreshold(0.0);
        if (resumeId != null) {
            request.filterExpression(new FilterExpressionBuilder().eq("resumeId", resumeId.toString()).build());
        }
        List<Document> hits = vectorStore.similaritySearch(request.build());
        if (hits == null) {
            hits = List.of();
        }
        return chunkGrouper.group(hits, properties.getAggregation());
    }

    private List<ScoredCandidate> scoreCandidates(JobDescription job, List<CandidateEvidence> evidence) {
        List<ScoredCandidate> scored = new ArrayList<>();
        for (CandidateEvidence candidate : evidence) {
            if (resumeRepository.existsById(candidate.resumeId())) {
                scored.add(scoreOne(job, candidate));
            }
        }
        return scored;
    }

    private ScoredCandidate scoreOne(JobDescription job, CandidateEvidence candidate) {
        Resume resume = resumeRepository.findById(candidate.resumeId())
                .orElseThrow(() -> new NotFoundException("Resume not found: " + candidate.resumeId()));
        String evidence = candidate.evidence() == null || candidate.evidence().isBlank()
                ? resume.getRawText()
                : candidate.evidence();
        String prompt = rankingPromptBuilder.render(
                job.getTitle(),
                job.getDescription(),
                resume.getCandidateName(),
                evidence);
        String content = chatClient.prompt().user(prompt).call().content();
        CandidateAssessment assessment = rankingPromptBuilder.parse(content);
        double blended = scoreCalculator.combine(candidate.similarity(), assessment.score());
        return new ScoredCandidate(candidate.resumeId(), candidate.similarity(), blended, assessment);
    }

    private List<MatchResponse> replaceResults(UUID jobId, List<ScoredCandidate> scored) {
        JobDescription job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Job not found: " + jobId));
        matchResultRepository.deleteByJob_Id(jobId);
        matchResultRepository.flush();
        List<MatchResponse> responses = new ArrayList<>();
        for (ScoredCandidate candidate : scored) {
            Resume resume = resumeRepository.findById(candidate.resumeId())
                    .orElseThrow(() -> new NotFoundException("Resume not found: " + candidate.resumeId()));
            MatchResult saved = matchResultRepository.save(toEntity(job, resume, candidate));
            responses.add(MatchResponse.from(saved));
        }
        responses.sort(Comparator.comparing(MatchResponse::score).reversed());
        return responses;
    }

    private MatchResult toEntity(JobDescription job, Resume resume, ScoredCandidate candidate) {
        return new MatchResult(
                UUID.randomUUID(),
                job,
                resume,
                BigDecimal.valueOf(candidate.blendedScore()).setScale(2, RoundingMode.HALF_UP),
                BigDecimal.valueOf(clampUnit(candidate.similarity())).setScale(4, RoundingMode.HALF_UP),
                candidate.assessment().strengths(),
                candidate.assessment().gaps(),
                candidate.assessment().rationale());
    }

    private int resolveTopK(int topK) {
        if (topK < 1 || topK > properties.getMaxTopK()) {
            throw new InvalidRequestException("topK must be between 1 and " + properties.getMaxTopK());
        }
        return topK;
    }

    private static double clampUnit(double similarity) {
        return Math.max(0d, Math.min(1d, similarity));
    }

    private record ScoredCandidate(
            UUID resumeId,
            double similarity,
            double blendedScore,
            CandidateAssessment assessment
    ) {
    }
}
