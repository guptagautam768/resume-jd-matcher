package dev.gautam.matcher.matching;

import dev.gautam.matcher.config.MatcherProperties;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

@Component
public class ChunkGrouper {

    private static final int MAX_EVIDENCE_CHARS = 8_000;
    private static final int MAX_CHUNKS_IN_EVIDENCE = 4;

    public List<CandidateEvidence> group(List<Document> documents, MatcherProperties.Aggregation aggregation) {
        Map<UUID, Accumulator> grouped = new LinkedHashMap<>();
        if (documents != null) {
            for (Document document : documents) {
                Object rawId = document.getMetadata().get("resumeId");
                if (rawId == null) {
                    continue;
                }
                UUID resumeId;
                try {
                    resumeId = UUID.fromString(rawId.toString());
                } catch (IllegalArgumentException ex) {
                    continue;
                }
                double score = document.getScore() == null ? 0d : document.getScore();
                String name = String.valueOf(document.getMetadata().getOrDefault("candidateName", ""));
                String text = document.getText() == null ? "" : document.getText();
                grouped.computeIfAbsent(resumeId, ignored -> new Accumulator(resumeId, name))
                        .add(score, text);
            }
        }
        return grouped.values().stream()
                .map(accumulator -> accumulator.toEvidence(aggregation))
                .sorted(Comparator.comparingDouble(CandidateEvidence::similarity).reversed())
                .toList();
    }

    private static final class Accumulator {
        private final UUID resumeId;
        private final String candidateName;
        private final List<ScoredChunk> chunks = new ArrayList<>();

        private Accumulator(UUID resumeId, String candidateName) {
            this.resumeId = resumeId;
            this.candidateName = candidateName;
        }

        private void add(double score, String text) {
            chunks.add(new ScoredChunk(score, text));
        }

        private CandidateEvidence toEvidence(MatcherProperties.Aggregation aggregation) {
            double similarity = switch (aggregation) {
                case MEAN -> chunks.stream().mapToDouble(ScoredChunk::score).average().orElse(0d);
                case MAX -> chunks.stream().mapToDouble(ScoredChunk::score).max().orElse(0d);
            };
            String evidence = chunks.stream()
                    .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                    .limit(MAX_CHUNKS_IN_EVIDENCE)
                    .map(ScoredChunk::text)
                    .filter(text -> text != null && !text.isBlank())
                    .reduce((left, right) -> left + "\n\n" + right)
                    .orElse("");
            if (evidence.length() > MAX_EVIDENCE_CHARS) {
                evidence = evidence.substring(0, MAX_EVIDENCE_CHARS);
            }
            return new CandidateEvidence(resumeId, candidateName, similarity, evidence);
        }
    }

    private record ScoredChunk(double score, String text) {
    }
}
