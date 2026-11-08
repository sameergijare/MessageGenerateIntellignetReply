package com.example.ticketgeneratereply.utils;



import java.util.StringJoiner;

public final class VectorUtils {

    private VectorUtils() {}

    /**
     * Standardizes a float array into a cast-ready text format for pgvector database parameter targets.
     * Converts float[] {0.1, 0.2, 0.3} into "[0.1,0.2,0.3]"
     */
    public static String toPgVectorString(float[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalArgumentException("Target coordinate embedding vector cannot be null or empty.");
        }
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (float val : vector) {
            joiner.add(String.valueOf(val));
        }
        return joiner.toString();
    }
}
