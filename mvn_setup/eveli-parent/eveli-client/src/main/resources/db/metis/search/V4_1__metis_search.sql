---
-- #%L
-- eveli-client
-- %%
-- Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- #L%
---
-- Metis site search. Needs pgvector.
-- Flyway history is shared: site search uses V4_x; later capabilities take V5_x+.

CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE TABLE metis_search_index (
    id              BIGSERIAL PRIMARY KEY,
    workflow_id     TEXT NOT NULL,
    topic_id        TEXT,
    locale          VARCHAR(5) NOT NULL,
    title           TEXT NOT NULL,
    category        TEXT,
    search_text     TEXT NOT NULL,
    ai_metadata     TEXT,
    search_vector   TSVECTOR,
    embedding       VECTOR(1024),
    content_hash    TEXT,
    indexed_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (workflow_id, locale)
);

CREATE INDEX idx_metis_search_fts ON metis_search_index USING GIN (search_vector);
CREATE INDEX idx_metis_search_embedding ON metis_search_index USING hnsw (embedding vector_cosine_ops);

-- Supports the fuzzy / compound-word fallback of the full text search.
CREATE INDEX idx_metis_search_trgm ON metis_search_index USING GIN (search_text gin_trgm_ops);

CREATE TABLE metis_search_reindex_job (
    id                   BIGSERIAL PRIMARY KEY,
    status               TEXT NOT NULL,
    embedding_model      TEXT,
    -- Live publication this job was started for.
    publication_id       TEXT,
    -- Cacheless HEAD the job indexed; a completed job only counts if this still matches.
    bundle_hash          TEXT,
    total_count          INTEGER,
    processed_count      INTEGER NOT NULL DEFAULT 0,
    skipped_count        INTEGER NOT NULL DEFAULT 0,
    started_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    -- Heartbeat: a job with no progress for long enough may be reclaimed by another instance.
    last_progress_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finished_at          TIMESTAMPTZ,
    duration_ms          BIGINT,
    error                TEXT,
    -- Set when a replacement reindex should start once this job releases the claim.
    restart_after_cancel BOOLEAN NOT NULL DEFAULT FALSE,
    restart_force        BOOLEAN NOT NULL DEFAULT FALSE
);

-- One in-flight reindex across replicas, including CANCELLING until the current document finishes.
CREATE UNIQUE INDEX idx_metis_search_reindex_job_inflight
    ON metis_search_reindex_job ((true))
    WHERE status IN ('RUNNING', 'CANCELLING');
