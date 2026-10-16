package dev.gautam.matcher.matching;

import java.util.UUID;

public record CandidateEvidence(
        UUID resumeId,
        String candidateName,
        double similarity,
        String evidence
) {
}
