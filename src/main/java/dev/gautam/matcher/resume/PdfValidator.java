package dev.gautam.matcher.resume;

import dev.gautam.matcher.common.InvalidResumeFileException;
import dev.gautam.matcher.config.MatcherProperties;
import java.io.IOException;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PdfValidator {

    private final MatcherProperties properties;

    public PdfValidator(MatcherProperties properties) {
        this.properties = properties;
    }

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidResumeFileException("A non-empty PDF file is required");
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        if (!name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new InvalidResumeFileException("Only PDF resumes are accepted");
        }
        if (file.getSize() > properties.getMaxFileBytes()) {
            throw new InvalidResumeFileException("PDF exceeds the maximum file size");
        }
        byte[] header = new byte[5];
        try {
            int read = file.getInputStream().read(header);
            if (read < 5 || header[0] != '%' || header[1] != 'P' || header[2] != 'D' || header[3] != 'F' || header[4] != '-') {
                throw new InvalidResumeFileException("File is not a PDF");
            }
        } catch (IOException ex) {
            throw new InvalidResumeFileException("Could not read the uploaded file");
        }
    }
}
