package dev.gautam.matcher.job;

import java.time.OffsetDateTime;
import java.util.UUID;

public record JobResponse(
        UUID id,
        String title,
        String description,
        OffsetDateTime createdAt
) {
    public static JobResponse from(JobDescription job) {
        return new JobResponse(job.getId(), job.getTitle(), job.getDescription(), job.getCreatedAt());
    }
}
