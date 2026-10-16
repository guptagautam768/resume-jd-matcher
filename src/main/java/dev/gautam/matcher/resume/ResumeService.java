package dev.gautam.matcher.resume;

import dev.gautam.matcher.common.InvalidResumeFileException;
import dev.gautam.matcher.common.NotFoundException;
import dev.gautam.matcher.config.MatcherProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final PdfIngestionService ingestionService;
    private final VectorStore vectorStore;
    private final MatcherProperties properties;

    public ResumeService(
            ResumeRepository resumeRepository,
            PdfIngestionService ingestionService,
            VectorStore vectorStore,
            MatcherProperties properties) {
        this.resumeRepository = resumeRepository;
        this.ingestionService = ingestionService;
        this.vectorStore = vectorStore;
        this.properties = properties;
    }

    @Transactional
    public List<ResumeResponse> upload(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new InvalidResumeFileException("At least one PDF is required");
        }
        if (files.size() > properties.getMaxFilesPerUpload()) {
            throw new InvalidResumeFileException(
                    "Upload at most " + properties.getMaxFilesPerUpload() + " PDFs at a time");
        }
        List<ResumeResponse> created = new ArrayList<>();
        for (MultipartFile file : files) {
            created.add(ResumeResponse.from(ingestionService.ingestPdf(file)));
        }
        return created;
    }

    @Transactional
    public ResumeResponse uploadText(String candidateName, String text) {
        return ResumeResponse.from(ingestionService.ingestText(candidateName, text));
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> list() {
        return resumeRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ResumeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumeResponse get(UUID id) {
        return ResumeResponse.from(require(id));
    }

    @Transactional
    public void delete(UUID id) {
        Resume resume = require(id);
        vectorStore.delete(new FilterExpressionBuilder().eq("resumeId", id.toString()).build());
        resumeRepository.delete(resume);
    }

    private Resume require(UUID id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resume not found: " + id));
    }
}
