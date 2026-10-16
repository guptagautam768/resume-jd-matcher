package dev.gautam.matcher.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

/**
 * Deterministic bag-of-tokens embedding used by the demo and test profiles.
 * Shared tokens land in the same dimensions, so cosine similarity tracks lexical overlap.
 */
public final class HashEmbeddingModel implements EmbeddingModel {

    private static final Set<String> STOPWORDS = Set.of(
            "the", "and", "for", "with", "that", "this", "from", "have", "has", "was", "were",
            "are", "you", "your", "our", "not", "but", "they", "their", "will", "into", "over",
            "such", "using", "used", "than", "then", "also", "both", "each", "more", "most",
            "other", "some", "about", "across", "after", "before", "under", "while", "where",
            "which", "who", "what", "when", "how", "team", "work", "working", "years", "year");

    private final int dimensions;

    public HashEmbeddingModel(int dimensions) {
        if (dimensions < 8) {
            throw new IllegalArgumentException("dimensions must be at least 8");
        }
        this.dimensions = dimensions;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<String> inputs = request.getInstructions();
        List<Embedding> embeddings = new ArrayList<>(inputs.size());
        for (int i = 0; i < inputs.size(); i++) {
            embeddings.add(new Embedding(vector(inputs.get(i)), i));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return vector(document.getText());
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    private float[] vector(String text) {
        float[] values = new float[dimensions];
        if (text != null) {
            for (String token : text.toLowerCase(Locale.ROOT).split("[^a-z0-9+#]+")) {
                if (token.length() < 3 || STOPWORDS.contains(token)) {
                    continue;
                }
                int bucket = Math.floorMod(token.hashCode(), dimensions);
                values[bucket] += 1.0f;
            }
        }
        float norm = 0f;
        for (float value : values) {
            norm += value * value;
        }
        if (norm == 0f) {
            values[0] = 1f;
            return values;
        }
        float scale = (float) (1.0 / Math.sqrt(norm));
        for (int i = 0; i < values.length; i++) {
            values[i] *= scale;
        }
        return values;
    }
}
