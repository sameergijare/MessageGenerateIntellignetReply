package com.example.ticketgeneratereply.datainit;

import com.example.ticketgeneratereply.db.KnowledgeEmbedding;
import com.example.ticketgeneratereply.db.KnowledgeEmbeddingRepository;
import com.example.ticketgeneratereply.TenantContext;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final KnowledgeEmbeddingRepository embeddingRepository;
    private final EmbeddingModel embeddingModel;

    public DataInitializer(KnowledgeEmbeddingRepository embeddingRepository, EmbeddingModel embeddingModel) {
        this.embeddingRepository = embeddingRepository;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public void run(String... args) throws Exception {
        String testTenant = "uk-retail-test-ltd";

        // Force the multi-tenant context holder manually for this initialization thread
        TenantContext.setCurrentTenantId(testTenant);

        System.out.println("Checking data presence for tenant: " + testTenant);

        try {
            // Check if we can pull rows from the database provider
            long count = embeddingRepository.count(); // Or verify if findNearestMatches yields nothing

            if (count == 0) {
                System.out.println("Seeding knowledge base vectors for " + testTenant + " using local Bge model...");

                seedChunk(testTenant, "Password Reset Policy",
                        "To reset your password, navigate to the portal login screen and click on Forgot Password. Enter your registered email address.");

                seedChunk(testTenant, "Billing Discrepancies",
                        "If a profile reports a double charge on their credit card statement, duplicate system transactions are flagged automatically and refunded within 3 days.");

                System.out.println("Local Java data initialization completely finished!");
            } else {
                System.out.println("Tenant knowledge table already has " + count + " active data records.");
            }
        } catch (Exception e) {
            System.err.println("DataInitializer skipped. Table schema may not be created yet: " + e.getMessage());
        } finally {
            TenantContext.clear(); // Always clean up context allocations
        }
    }

    private void seedChunk(String tenantId, String title, String text) {
        KnowledgeEmbedding doc = new KnowledgeEmbedding();

        // REMOVED doc.setId(...) COMPLETELY.
        // Your entity handles its own key assignment automatically!

        doc.setTenantId(tenantId);
        doc.setReferenceTitle(title);
        doc.setRawContentChunk(text);

        // Extract the raw float[] array directly from your local Bge model
        float[] vectors = embeddingModel.embed(text).content().vector();

        // Pass the float[] array directly to your setter
        doc.setEmbedding(vectors);

        embeddingRepository.save(doc);
    }

}
