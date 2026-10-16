package dev.gautam.matcher.demo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.Prompt;

class HeuristicRankChatModelTest {

    private final HeuristicRankChatModel model = new HeuristicRankChatModel();

    @Test
    void scoresOverlapAndDoesNotTreatJavaScriptAsJava() {
        assertThat(HeuristicRankChatModel.containsTerm("JavaScript services", "Java")).isFalse();
        assertThat(HeuristicRankChatModel.containsTerm("Java 21 and Spring Boot", "Java")).isTrue();

        String json = model.call(new Prompt("""
                Job description:
                Java, Spring Boot, PostgreSQL, AWS, Docker, JUnit, Kafka
                Candidate:
                Priya Natarajan
                Resume evidence:
                Java, Spring Boot, PostgreSQL, AWS, Docker, JUnit, Kafka
                Give an integer score
                """)).getResult().getOutput().getText();

        assertThat(json).contains("\"score\":100");
        assertThat(json).contains("Spring Boot");
    }

    @Test
    void givesALowScoreWhenEvidenceMissesTheJobSkills() {
        String json = model.call(new Prompt("""
                Job description:
                React and TypeScript
                Candidate:
                Priya Natarajan
                Resume evidence:
                Java and Spring Boot only
                Give an integer score
                """)).getResult().getOutput().getText();

        assertThat(json).contains("\"score\":0");
        assertThat(json).contains("React");
    }
}
