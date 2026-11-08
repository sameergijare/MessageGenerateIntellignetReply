package com.example.ticketgeneratereply.datainit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import dev.langchain4j.model.embedding.EmbeddingModel; // Adjust import if using LangChain4j / ONNX

import java.util.Arrays;
import java.util.List;

@Component
public class TicketAnchorPopulator implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;

    public TicketAnchorPopulator(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
    }

    private record AnchorSeed(String categoryTag, String priority, String text) {}

    @Override
    public void run(String... args) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ticket_classification_anchors", Integer.class
        );
        if (count != null && count > 0) {
            return;
        }

        List<AnchorSeed> seeds = List.of(
                // High / Critical Priority Examples
                new AnchorSeed("OUTAGE", "CRITICAL", "Production system is completely down and users cannot connect"),
                new AnchorSeed("SECURITY", "CRITICAL", "Suspicious login detected and unauthorised API key access"),
                new AnchorSeed("BILLING", "HIGH", "Duplicate charge on invoice and payment gateway failing"),

                // Medium Priority Examples
                new AnchorSeed("BUG_REPORT", "MEDIUM", "Export to CSV button throws a 500 internal server error"),
                new AnchorSeed("ACCOUNT_ACCESS", "MEDIUM", "User is locked out and password reset email never arrives"),

                // Low Priority Examples
                new AnchorSeed("FEATURE_REQUEST", "LOW", "Would love to have dark mode added to the analytics dashboard"),
                new AnchorSeed("GENERAL_INQUIRY", "LOW", "How do I update my company address on future invoices?")
        );

        String insertSql = """
            INSERT INTO ticket_classification_anchors (category_tag, inferred_priority, anchor_text, embedding)
            VALUES (?, ?, ?, cast(? AS vector))
            """;

        for (AnchorSeed seed : seeds) {
            float[] vector = embeddingModel.embed(seed.text()).content().vector();
            String vectorLiteral = Arrays.toString(vector); // Formats as "[0.012, -0.045, ...]"
            jdbcTemplate.update(insertSql, seed.categoryTag(), seed.priority(), seed.text(), vectorLiteral);
        }
    }
}

