package dev.gautam.matcher.matching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import dev.gautam.matcher.config.MatcherProperties;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

class ChunkGrouperTest {

    private final ChunkGrouper grouper = new ChunkGrouper();

    @Test
    void usesMaxSimilarityAndKeepsTheStrongerChunkFirst() {
        UUID id = UUID.randomUUID();
        Document low = document(id, "A", "Java basics", 0.2);
        Document high = document(id, "A", "Spring Boot services", 0.9);

        List<CandidateEvidence> grouped = grouper.group(List.of(low, high), MatcherProperties.Aggregation.MAX);

        assertThat(grouped).hasSize(1);
        assertThat(grouped.getFirst().similarity()).isEqualTo(0.9);
        assertThat(grouped.getFirst().evidence()).startsWith("Spring Boot services");
    }

    @Test
    void averagesWhenAggregationIsMeanAndSortsResumesByScore() {
        UUID strong = UUID.randomUUID();
        UUID weak = UUID.randomUUID();
        List<CandidateEvidence> grouped = grouper.group(List.of(
                document(weak, "Weak", "notes", 0.1),
                document(strong, "Strong", "one", 0.4),
                document(strong, "Strong", "two", 0.8)),
                MatcherProperties.Aggregation.MEAN);

        assertThat(grouped).extracting(CandidateEvidence::resumeId).containsExactly(strong, weak);
        assertThat(grouped.getFirst().similarity()).isCloseTo(0.6, within(0.0001));
    }

    private static Document document(UUID resumeId, String name, String text, double score) {
        return Document.builder()
                .text(text)
                .metadata(Map.of(
                        "resumeId", resumeId.toString(),
                        "candidateName", name,
                        "chunkIdx", 0))
                .score(score)
                .build();
    }
}
