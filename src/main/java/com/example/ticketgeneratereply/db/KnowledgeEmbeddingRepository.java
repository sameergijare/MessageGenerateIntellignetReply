package com.example.ticketgeneratereply.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface KnowledgeEmbeddingRepository extends JpaRepository<KnowledgeEmbedding, UUID> {

    @Query(value = """
        SELECT id, tenant_id, reference_title, raw_content_chunk 
        FROM knowledge_embeddings 
        WHERE tenant_id = :tenantId 
        ORDER BY embedding <=> cast(:targetEmbedding AS vector) 
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findNearestMatchesRaw(
            @Param("tenantId") String tenantId,
            @Param("targetEmbedding") float[] targetEmbedding,
            @Param("limit") int limit
    );
}
