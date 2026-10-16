package dev.gautam.matcher.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "matcher")
public class MatcherProperties {

    public enum Aggregation {
        MAX,
        MEAN
    }

    private double similarityWeight = 0.3;
    private double llmWeight = 0.7;
    private int retrievalTopChunks = 40;
    private int defaultTopK = 5;
    private int maxTopK = 20;
    private Aggregation aggregation = Aggregation.MAX;
    private int embeddingDimensions = 1536;
    private int maxResumeTextChars = 50_000;
    private int maxFilesPerUpload = 10;
    private long maxFileBytes = 5_242_880;

    public double getSimilarityWeight() {
        return similarityWeight;
    }

    public void setSimilarityWeight(double similarityWeight) {
        this.similarityWeight = similarityWeight;
    }

    public double getLlmWeight() {
        return llmWeight;
    }

    public void setLlmWeight(double llmWeight) {
        this.llmWeight = llmWeight;
    }

    public int getRetrievalTopChunks() {
        return retrievalTopChunks;
    }

    public void setRetrievalTopChunks(int retrievalTopChunks) {
        this.retrievalTopChunks = retrievalTopChunks;
    }

    public int getDefaultTopK() {
        return defaultTopK;
    }

    public void setDefaultTopK(int defaultTopK) {
        this.defaultTopK = defaultTopK;
    }

    public int getMaxTopK() {
        return maxTopK;
    }

    public void setMaxTopK(int maxTopK) {
        this.maxTopK = maxTopK;
    }

    public Aggregation getAggregation() {
        return aggregation;
    }

    public void setAggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
    }

    public int getEmbeddingDimensions() {
        return embeddingDimensions;
    }

    public void setEmbeddingDimensions(int embeddingDimensions) {
        this.embeddingDimensions = embeddingDimensions;
    }

    public int getMaxResumeTextChars() {
        return maxResumeTextChars;
    }

    public void setMaxResumeTextChars(int maxResumeTextChars) {
        this.maxResumeTextChars = maxResumeTextChars;
    }

    public int getMaxFilesPerUpload() {
        return maxFilesPerUpload;
    }

    public void setMaxFilesPerUpload(int maxFilesPerUpload) {
        this.maxFilesPerUpload = maxFilesPerUpload;
    }

    public long getMaxFileBytes() {
        return maxFileBytes;
    }

    public void setMaxFileBytes(long maxFileBytes) {
        this.maxFileBytes = maxFileBytes;
    }
}
