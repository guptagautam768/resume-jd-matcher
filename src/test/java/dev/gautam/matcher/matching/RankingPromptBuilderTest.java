package dev.gautam.matcher.matching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.gautam.matcher.common.ModelResponseException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class RankingPromptBuilderTest {

    private final RankingPromptBuilder builder =
            new RankingPromptBuilder(new ClassPathResource("prompts/rank-candidate.st"));

    @Test
    void rendersTheJobAndTheSchema() {
        String prompt = builder.render("Senior Backend Engineer", "Java and PostgreSQL", "Priya Natarajan", "Spring Boot");

        assertThat(prompt).contains("Senior Backend Engineer");
        assertThat(prompt).contains("Priya Natarajan");
        assertThat(prompt).contains("Spring Boot");
        assertThat(prompt).contains("score");
    }

    @Test
    void parsesFencedJsonAndClampsTheScore() {
        CandidateAssessment assessment = builder.parse("""
                ```json
                {"score":140,"strengths":["Java"],"gaps":[],"rationale":"Evidence includes Java."}
                ```
                """);

        assertThat(assessment.score()).isEqualTo(100);
        assertThat(assessment.strengths()).containsExactly("Java");
        assertThat(assessment.rationale()).contains("Java");
    }

    @Test
    void rejectsUnparseableOutput() {
        assertThatThrownBy(() -> builder.parse("not json"))
                .isInstanceOf(ModelResponseException.class);
    }
}
