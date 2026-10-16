package dev.gautam.matcher.resume;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CandidateNameExtractorTest {

    private final CandidateNameExtractor extractor = new CandidateNameExtractor();

    @Test
    void usesTheFirstNonBlankLine() {
        assertThat(extractor.fromText("\nPriya Natarajan\nEngineer\n", "ignored.pdf"))
                .isEqualTo("Priya Natarajan");
    }

    @Test
    void stripsANameLabel() {
        assertThat(extractor.fromText("Name: Jonah Blake\nSkills", "resume.pdf"))
                .isEqualTo("Jonah Blake");
    }

    @Test
    void fallsBackToTheFileName() {
        assertThat(extractor.fromText("   ", "01-priya-natarajan.pdf"))
                .isEqualTo("01 priya natarajan");
    }
}
