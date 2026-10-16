package dev.gautam.matcher.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class VectorStoreConfig {

    @Bean
    @DependsOn("flywayInitializer")
    public VectorStore vectorStore(
            JdbcTemplate jdbcTemplate,
            EmbeddingModel embeddingModel,
            MatcherProperties properties) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(properties.getEmbeddingDimensions())
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName("vector_store")
                .maxDocumentBatchSize(10_000)
                .build();
    }
}
