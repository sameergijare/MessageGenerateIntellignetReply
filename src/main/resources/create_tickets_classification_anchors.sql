
CREATE TABLE IF NOT EXISTS ticket_classification_anchors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_tag VARCHAR(100) NOT NULL,
    inferred_priority VARCHAR(50) NOT NULL,
    anchor_text TEXT NOT NULL,
    embedding vector(384) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ticket_anchors_embedding 
ON ticket_classification_anchors 
USING hnsw (embedding vector_cosine_ops);
