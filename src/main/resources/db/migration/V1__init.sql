CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE resume (
    id UUID PRIMARY KEY,
    candidate_name TEXT NOT NULL,
    file_name TEXT NOT NULL,
    raw_text TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE job_description (
    id UUID PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE match_result (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES job_description (id) ON DELETE CASCADE,
    resume_id UUID NOT NULL REFERENCES resume (id) ON DELETE CASCADE,
    score NUMERIC(5, 2) NOT NULL,
    similarity NUMERIC(6, 4) NOT NULL,
    strengths JSONB NOT NULL,
    gaps JSONB NOT NULL,
    rationale TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_match_result_job ON match_result (job_id);
CREATE INDEX idx_match_result_resume ON match_result (resume_id);

-- vector_store (id, content, metadata, embedding) is created by VectorStoreConfig
-- through Spring AI PgVectorStore.initializeSchema.
