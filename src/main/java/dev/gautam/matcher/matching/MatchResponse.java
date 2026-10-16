package dev.gautam.matcher.matching;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MatchResponse(
        UUID id,
        UUID jobId,
        UUID resumeId,
        String candidateName,
        String fileName,
        BigDecimal score,
        BigDecimal similarity,
        List<String> strengths,
        List<String> gaps,
        String rationale,
        OffsetDateTime createdAt
) {
    public static MatchResponse from(MatchResult result) {
        return new MatchResponse(
                result.getId(),
                result.getJob().getId(),
                result.getResume().getId(),
                result.getResume().getCandidateName(),
                result.getResume().getFileName(),
                result.getScore(),
                result.getSimilarity(),
                result.getStrengths(),
                result.getGaps(),
                result.getRationale(),
                result.getCreatedAt());
    }
}
