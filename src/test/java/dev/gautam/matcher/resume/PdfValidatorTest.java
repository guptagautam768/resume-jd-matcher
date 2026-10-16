package dev.gautam.matcher.resume;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.gautam.matcher.common.InvalidResumeFileException;
import dev.gautam.matcher.config.MatcherProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class PdfValidatorTest {

    private final PdfValidator validator = new PdfValidator(new MatcherProperties());

    @Test
    void rejectsNonPdfExtension() throws Exception {
        MultipartFile file = file("notes.txt", "%PDF-1.4".getBytes(StandardCharsets.US_ASCII), 10);

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidResumeFileException.class)
                .hasMessageContaining("PDF");
    }

    @Test
    void rejectsPdfExtensionWithoutMagicBytes() throws IOException {
        MultipartFile file = file("resume.pdf", "hello".getBytes(StandardCharsets.US_ASCII), 5);

        assertThatThrownBy(() -> validator.validate(file))
                .isInstanceOf(InvalidResumeFileException.class)
                .hasMessageContaining("not a PDF");
    }

    @Test
    void acceptsPdfHeader() throws IOException {
        MultipartFile file = file("resume.pdf", "%PDF-1.4".getBytes(StandardCharsets.US_ASCII), 8);

        assertThatCode(() -> validator.validate(file)).doesNotThrowAnyException();
    }

    private static MultipartFile file(String name, byte[] bytes, long size) throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(name);
        when(file.getSize()).thenReturn(size);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        return file;
    }
}
