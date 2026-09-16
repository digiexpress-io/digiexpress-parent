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
    content_hash    TEXT,
    indexed_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (workflow_id, locale)
);

CREATE INDEX idx_metis_search_fts ON metis_search_index USING GIN (search_vector);

COMMENT ON TABLE metis_search_index IS 'Semantic site search documents, one row per workflow link per locale. Unused when eveli.metis.search.enabled is false. The embedding column and HNSW/trigram indexes are added when search is enabled (pgvector required then).';
COMMENT ON COLUMN metis_search_index.id IS 'Surrogate key for the indexed document';
COMMENT ON COLUMN metis_search_index.workflow_id IS 'Limaone workflow id this document was built from';
COMMENT ON COLUMN metis_search_index.topic_id IS 'Owning topic id when the workflow is reached through a topic page';
COMMENT ON COLUMN metis_search_index.locale IS 'Document locale (en, fi, sv). Selects the PostgreSQL stemmer';
COMMENT ON COLUMN metis_search_index.title IS 'Workflow title shown in search results';
COMMENT ON COLUMN metis_search_index.category IS 'Topic title used as the result category';
COMMENT ON COLUMN metis_search_index.search_text IS 'Indexed page body after markdown stripping and generic-topic filtering';
COMMENT ON COLUMN metis_search_index.ai_metadata IS 'Chat-model summary generated at index time, ranked below title and category';
COMMENT ON COLUMN metis_search_index.search_vector IS 'Weighted tsvector: title A, category B, ai_metadata C, search_text D';
COMMENT ON COLUMN metis_search_index.content_hash IS 'SHA-256 of locale, topic, title, search text, embedding model id and prompt version. Unchanged hashes are skipped on incremental reindex';
COMMENT ON COLUMN metis_search_index.indexed_at IS 'When this row was last written';
COMMENT ON INDEX idx_metis_search_fts IS 'GIN index for full-text search over search_vector';

CREATE TABLE metis_search_reindex_job (
    id                   BIGSERIAL PRIMARY KEY,
    status               TEXT NOT NULL,
    embedding_model      TEXT,
    publication_id       TEXT,
    bundle_hash          TEXT,
    total_count          INTEGER,
    processed_count      INTEGER NOT NULL DEFAULT 0,
    skipped_count        INTEGER NOT NULL DEFAULT 0,
    started_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_progress_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    finished_at          TIMESTAMPTZ,
    duration_ms          BIGINT,
    error                TEXT,
    restart_after_cancel BOOLEAN NOT NULL DEFAULT FALSE,
    restart_force        BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX idx_metis_search_reindex_job_inflight
    ON metis_search_reindex_job ((true))
    WHERE status IN ('RUNNING', 'CANCELLING');

COMMENT ON TABLE metis_search_reindex_job IS 'Cluster-wide reindex job. At most one row may be RUNNING or CANCELLING at a time, including across Kubernetes replicas.';
COMMENT ON COLUMN metis_search_reindex_job.id IS 'Job id, also returned to the worker API';
COMMENT ON COLUMN metis_search_reindex_job.status IS 'RUNNING, CANCELLING, CANCELLED, COMPLETED or FAILED';
COMMENT ON COLUMN metis_search_reindex_job.embedding_model IS 'Embedding model id recorded when the job was claimed';
COMMENT ON COLUMN metis_search_reindex_job.publication_id IS 'Live publication this job was started for';
COMMENT ON COLUMN metis_search_reindex_job.bundle_hash IS 'Cacheless HEAD of the bundle the job indexed. A completed job only counts for that publication while this still matches';
COMMENT ON COLUMN metis_search_reindex_job.total_count IS 'Documents discovered for this job, set after the bundle is read';
COMMENT ON COLUMN metis_search_reindex_job.processed_count IS 'Documents written (embedded and upserted)';
COMMENT ON COLUMN metis_search_reindex_job.skipped_count IS 'Documents skipped because the content hash already matched';
COMMENT ON COLUMN metis_search_reindex_job.started_at IS 'When the job row was inserted';
COMMENT ON COLUMN metis_search_reindex_job.last_progress_at IS 'Heartbeat. A job with no progress for abandoned-after-seconds may be reclaimed by another instance';
COMMENT ON COLUMN metis_search_reindex_job.finished_at IS 'When the job reached COMPLETED, FAILED or CANCELLED';
COMMENT ON COLUMN metis_search_reindex_job.duration_ms IS 'Wall time from start to finish';
COMMENT ON COLUMN metis_search_reindex_job.error IS 'Failure or cancel reason when the job did not complete';
COMMENT ON COLUMN metis_search_reindex_job.restart_after_cancel IS 'When true, a replacement reindex starts once this job releases the claim';
COMMENT ON COLUMN metis_search_reindex_job.restart_force IS 'force flag to apply to the replacement reindex after cancel';
COMMENT ON INDEX idx_metis_search_reindex_job_inflight IS 'Enforces one in-flight reindex across replicas, including CANCELLING until the current document finishes';
