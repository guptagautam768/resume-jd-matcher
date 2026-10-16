package dev.gautam.matcher.matching;

import dev.gautam.matcher.config.MatcherProperties;
import org.springframework.stereotype.Component;

@Component
public class ScoreCalculator {

    private final MatcherProperties properties;

    public ScoreCalculator(MatcherProperties properties) {
        this.properties = properties;
    }

    /**
     * Similarity is on a 0-1 scale and the model score is 0-100.
     * Similarity is scaled to 0-100 before the weighted blend so the two terms share a unit.
     * The default weights are 0.3 and 0.7. The result is clamped to 0-100.
     */
    public double combine(double similarity, int llmScore) {
        double similarityPercent = clamp(similarity, 0, 1) * 100.0;
        double modelScore = clamp(llmScore, 0, 100);
        double blended = (properties.getSimilarityWeight() * similarityPercent)
                + (properties.getLlmWeight() * modelScore);
        return Math.round(clamp(blended, 0, 100) * 100.0) / 100.0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
