package dev.gautam.matcher.matching;

import static org.assertj.core.api.Assertions.assertThat;

import dev.gautam.matcher.config.MatcherProperties;
import org.junit.jupiter.api.Test;

class ScoreCalculatorTest {

    private final ScoreCalculator calculator = new ScoreCalculator(new MatcherProperties());

    @Test
    void blendsScaledSimilarityWithModelScore() {
        assertThat(calculator.combine(0.8, 90)).isEqualTo(87.0);
    }

    @Test
    void clampsInputsAndResult() {
        assertThat(calculator.combine(2, 150)).isEqualTo(100.0);
        assertThat(calculator.combine(-1, -5)).isEqualTo(0.0);
    }
}
