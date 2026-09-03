package com.ledgerlens.service.rag;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Utility for vector operations and cosine similarity calculation.
 */
public final class VectorSimilarityUtil {

    private VectorSimilarityUtil() {}

    /**
     * Calculates the cosine similarity between two vectors.
     * Returns a value between -1.0 and 1.0 (clamped to 0.0 - 1.0 for normalized semantic retrieval).
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @return Cosine similarity score (0.00 to 1.00)
     */
    public static BigDecimal cosineSimilarity(List<Float> v1, List<Float> v2) {
        if (v1 == null || v2 == null || v1.isEmpty() || v2.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int size = Math.min(v1.size(), v2.size());
        if (size == 0) {
            return BigDecimal.ZERO;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < size; i++) {
            float a = v1.get(i);
            float b = v2.get(i);
            dotProduct += (double) a * b;
            normA += (double) a * a;
            normB += (double) b * b;
        }

        if (normA <= 0.0 || normB <= 0.0) {
            return BigDecimal.ZERO;
        }

        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        // Clamp to [0, 1] for semantic distance
        if (Double.isNaN(similarity) || similarity < 0.0) {
            similarity = 0.0;
        } else if (similarity > 1.0) {
            similarity = 1.0;
        }

        return BigDecimal.valueOf(similarity).setScale(4, RoundingMode.HALF_UP);
    }
}
