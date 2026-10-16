package dev.gautam.matcher.resume;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.core.io.FileSystemResource;

class SampleResumePdfTest {

    @Test
    void samplePdfsAreSyntheticAndExtractable() {
        Path pdf = Path.of("samples/resumes/01-priya-natarajan.pdf");
        String text = new PagePdfDocumentReader(new FileSystemResource(pdf)).get().stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        String normalized = text.replaceAll("\\s+", " ");
        assertThat(normalized).contains("Priya Natarajan");
        assertThat(normalized).contains("SYNTHETIC SAMPLE");
        assertThat(normalized).contains("Spring Boot");
        assertThat(new CandidateNameExtractor().fromText(text, pdf.getFileName().toString()))
                .isEqualTo("Priya Natarajan");
    }
}
