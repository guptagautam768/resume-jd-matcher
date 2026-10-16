package dev.gautam.matcher.resume;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ResumeResponse(
        UUID id,
        String candidateName,
        String fileName,
        String rawText,
        OffsetDateTime createdAt
) {
    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getCandidateName(),
                resume.getFileName(),
                resume.getRawText(),
                resume.getCreatedAt());
    }
}
