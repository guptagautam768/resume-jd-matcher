package dev.gautam.matcher.matching;

import java.util.List;

public record CandidateAssessment(
        int score,
        List<String> strengths,
        List<String> gaps,
        String rationale
) {
}
