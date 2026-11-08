package com.example.ticketgeneratereply.service;

import com.example.ticketgeneratereply.TicketAnalysisResult;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class LocalStructuralAnalyzer {

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public TicketAnalysisResult extractMetadataLocally(String messageBody, String tenantId) {
// 1. Generate the vector embedding using your local model
        Embedding embedding = embeddingModel.embed(messageBody).content();
        float[] vectorArray = embedding.vector();

// 2. Format the float array cleanly without spaces so pgvector accepts it natively
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vectorArray.length; i++) {
            sb.append(vectorArray[i]);
            if (i < vectorArray.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        String vectorString = sb.toString();
        // 2. Modified SQL query using explicit quotes to enforce case-sensitivity if needed
        String sql = """
            SELECT category_tag, inferred_priority, (1 - (embedding <=> cast(? AS vector))) AS similarity_score
            FROM ticket_classification_anchors
            ORDER BY embedding <=> cast(? AS vector)
            LIMIT 1
        """;
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, vectorString, vectorString);

        if (rows != null && !rows.isEmpty()) {
            Map<String, Object> closestMatch = rows.get(0);
            String category = (String) closestMatch.getOrDefault("category_tag", "unclassified");
            String priority = (String) closestMatch.getOrDefault("inferred_priority", "MEDIUM");

            Number scoreObj = (Number) closestMatch.get("similarity_score");
            double confidence = (scoreObj != null) ? scoreObj.doubleValue() : 0.85;

            return new TicketAnalysisResult(priority, category, confidence);
        }

        System.out.println("No anchor classifications found for this tenant. Serving standard defaults.");
        return new TicketAnalysisResult("MEDIUM", "unclassified-new-tenant", 1.0);
    }
}
