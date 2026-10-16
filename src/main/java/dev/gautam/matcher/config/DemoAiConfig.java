package dev.gautam.matcher.config;

import dev.gautam.matcher.demo.HashEmbeddingModel;
import dev.gautam.matcher.demo.HeuristicRankChatModel;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * Stand-in models for the {@code demo} and {@code test} profiles.
 * They never call OpenAI. Ranking quality in these profiles is not a measured result.
 */
@Configuration
@Profile({"demo", "test"})
public class DemoAiConfig {

    @Bean
    @Primary
    public EmbeddingModel hashEmbeddingModel(MatcherProperties properties) {
        return new HashEmbeddingModel(properties.getEmbeddingDimensions());
    }

    @Bean
    @Primary
    public ChatModel heuristicChatModel() {
        return new HeuristicRankChatModel();
    }
}
