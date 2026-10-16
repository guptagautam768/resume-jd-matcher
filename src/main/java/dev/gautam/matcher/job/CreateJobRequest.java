package dev.gautam.matcher.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateJobRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 20_000) String description
) {
}
