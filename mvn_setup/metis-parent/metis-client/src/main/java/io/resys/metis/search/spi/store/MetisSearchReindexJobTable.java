package io.resys.metis.search.spi.store;

/*-
 * #%L
 * metis-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.resys.metis.search.api.ImmutableMetisSearchIndexStatus;
import io.resys.metis.search.api.MetisSearchIndexStatus;
import io.resys.metis.search.api.MetisSearchIndexStatus.JobState;
import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
    name = "metis_search_reindex_job",
    order = 1,
    ddl = """
      CREATE TABLE IF NOT EXISTS {metis_search_reindex_job} (
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
    """,
    drop = """
      DROP TABLE IF EXISTS {metis_search_reindex_job};
    """
)
public interface MetisSearchReindexJobTable {

  @TenantSql.Find(
      optional = true,
      sql = """
        INSERT INTO {metis_search_reindex_job} (status, embedding_model, publication_id)
        SELECT 'RUNNING', $1, $2
        WHERE NOT EXISTS (
            SELECT 1 FROM {metis_search_reindex_job} WHERE status IN ('RUNNING', 'CANCELLING'))
        RETURNING id
        """,
      rowMapper = LongMapper.class
  )
  SqlTuple tryStart(String embeddingModel, String publicationId);

  @TenantSql.Find(
      optional = true,
      sql = """
        SELECT id, status, embedding_model, total_count, processed_count, skipped_count,
               started_at, finished_at, duration_ms, error, publication_id, bundle_hash
        FROM {metis_search_reindex_job}
        ORDER BY id DESC
        LIMIT 1
        """,
      rowMapper = StatusMapper.class
  )
  Sql findLatest();

  @TenantSql.Find(
      optional = true,
      sql = """
        SELECT id, status, embedding_model, total_count, processed_count, skipped_count,
               started_at, finished_at, duration_ms, error, publication_id, bundle_hash
        FROM {metis_search_reindex_job}
        WHERE id = $1
        """,
      rowMapper = StatusMapper.class
  )
  SqlTuple findById(long jobId);

  @TenantSql.Find(
      optional = true,
      sql = "SELECT status FROM {metis_search_reindex_job} WHERE id = $1",
      rowMapper = StringMapper.class
  )
  SqlTuple findStatus(long jobId);

  @TenantSql.Find(
      optional = false,
      sql = """
        SELECT COUNT(*) AS count FROM {metis_search_reindex_job}
        WHERE publication_id = $1 AND status IN ('RUNNING', 'CANCELLING', 'COMPLETED')
        """,
      rowMapper = CountMapper.class
  )
  SqlTuple countByPublication(String publicationId);

  @TenantSql.Find(
      optional = false,
      sql = """
        SELECT COUNT(*) AS count FROM {metis_search_reindex_job}
        WHERE publication_id = $1 AND status IN ('RUNNING', 'CANCELLING', 'COMPLETED')
          AND bundle_hash = $2
        """,
      rowMapper = CountMapper.class
  )
  SqlTuple countByPublicationAndBundle(String publicationId, String bundleHash);

  @TenantSql.Find(
      optional = false,
      sql = "SELECT COUNT(*) AS count FROM {metis_search_reindex_job} WHERE status IN ('RUNNING', 'CANCELLING')",
      rowMapper = CountMapper.class
  )
  Sql countInflight();

  @TenantSql.FindAll(
      sql = """
        UPDATE {metis_search_reindex_job}
        SET status = 'CANCELLING',
            restart_after_cancel = $1,
            restart_force = $2,
            last_progress_at = NOW()
        WHERE status IN ('RUNNING', 'CANCELLING')
        RETURNING id
        """,
      rowMapper = LongMapper.class
  )
  SqlTuple requestCancel(boolean restart, boolean force);

  @TenantSql.FindAll(
      sql = """
        UPDATE {metis_search_reindex_job}
        SET status = 'FAILED', error = 'Abandoned, no progress reported', finished_at = NOW(),
            restart_after_cancel = FALSE
        WHERE status IN ('RUNNING', 'CANCELLING')
          AND last_progress_at < NOW() - ($1::bigint * interval '1 second')
        RETURNING id
        """,
      rowMapper = LongMapper.class
  )
  SqlTuple failAbandoned(long seconds);

  @TenantSql.Find(
      optional = true,
      sql = """
        SELECT restart_force
        FROM {metis_search_reindex_job}
        WHERE id = $1 AND status = 'CANCELLED' AND restart_after_cancel = TRUE
        """,
      rowMapper = BooleanMapper.class
  )
  SqlTuple findRestartForce(long jobId);

  @TenantSql.FindAll(
      sql = "UPDATE {metis_search_reindex_job} SET restart_after_cancel = FALSE WHERE id = $1 RETURNING id",
      rowMapper = LongMapper.class
  )
  SqlTuple clearRestart(long jobId);

  @TenantSql.FindAll(
      sql = "UPDATE {metis_search_reindex_job} SET total_count = $1, last_progress_at = NOW() WHERE id = $2 RETURNING id",
      rowMapper = LongMapper.class
  )
  SqlTuple setTotal(int total, long jobId);

  @TenantSql.FindAll(
      sql = "UPDATE {metis_search_reindex_job} SET bundle_hash = $1, last_progress_at = NOW() WHERE id = $2 RETURNING id",
      rowMapper = LongMapper.class
  )
  SqlTuple setBundleHash(String bundleHash, long jobId);

  @TenantSql.FindAll(
      sql = "UPDATE {metis_search_reindex_job} SET skipped_count = $1, last_progress_at = NOW() WHERE id = $2 RETURNING id",
      rowMapper = LongMapper.class
  )
  SqlTuple setSkipped(int skipped, long jobId);

  @TenantSql.FindAll(
      sql = """
        UPDATE {metis_search_reindex_job}
        SET processed_count = GREATEST(processed_count, $1), last_progress_at = NOW()
        WHERE id = $2
        RETURNING id
        """,
      rowMapper = LongMapper.class
  )
  SqlTuple updateProgress(int processed, long jobId);

  @TenantSql.FindAll(
      sql = """
        UPDATE {metis_search_reindex_job}
        SET status = 'COMPLETED', processed_count = $1, skipped_count = $2,
            duration_ms = $3, finished_at = NOW()
        WHERE id = $4 AND status = 'RUNNING'
        RETURNING id
        """,
      rowMapper = LongMapper.class
  )
  SqlTuple markCompleted(int processed, int skipped, long durationMs, long jobId);

  @TenantSql.FindAll(
      sql = """
        UPDATE {metis_search_reindex_job}
        SET status = 'FAILED', error = $1, finished_at = NOW(), restart_after_cancel = FALSE
        WHERE id = $2 AND status IN ('RUNNING', 'CANCELLING')
        RETURNING id
        """,
      rowMapper = LongMapper.class
  )
  SqlTuple markFailed(String error, long jobId);

  @TenantSql.FindAll(
      sql = """
        UPDATE {metis_search_reindex_job}
        SET status = 'CANCELLED', error = 'Cancelled by operator',
            processed_count = $1, skipped_count = $2, duration_ms = $3, finished_at = NOW()
        WHERE id = $4 AND status IN ('RUNNING', 'CANCELLING')
        RETURNING id
        """,
      rowMapper = LongMapper.class
  )
  SqlTuple markCancelled(int processed, int skipped, long durationMs, long jobId);

  @TenantSql.FindAll(
      sql = "DELETE FROM {metis_search_reindex_job} WHERE $1::boolean RETURNING id",
      rowMapper = LongMapper.class
  )
  SqlTuple deleteAll(Boolean unused);

  class LongMapper implements TenantSql.RowMapper<Long> {
    @Override
    public Long apply(Row row) {
      return row.getLong(0);
    }
  }

  class StringMapper implements TenantSql.RowMapper<String> {
    @Override
    public String apply(Row row) {
      return row.getString(0);
    }
  }

  class BooleanMapper implements TenantSql.RowMapper<Boolean> {
    @Override
    public Boolean apply(Row row) {
      return row.getBoolean(0);
    }
  }

  class CountMapper implements TenantSql.RowMapper<Long> {
    @Override
    public Long apply(Row row) {
      final var count = row.getLong("count");
      return count == null ? 0L : count;
    }
  }

  class StatusMapper implements TenantSql.RowMapper<MetisSearchIndexStatus> {
    @Override
    public MetisSearchIndexStatus apply(Row row) {
      return ImmutableMetisSearchIndexStatus.builder()
          .jobId(row.getLong("id"))
          .state(JobState.valueOf(row.getString("status")))
          .embeddingModel(row.getString("embedding_model"))
          .totalCount(row.getInteger("total_count"))
          .processedCount(nz(row.getInteger("processed_count")))
          .skippedCount(nz(row.getInteger("skipped_count")))
          .startedAt(row.getOffsetDateTime("started_at"))
          .finishedAt(row.getOffsetDateTime("finished_at"))
          .durationMs(row.getLong("duration_ms"))
          .error(row.getString("error"))
          .publicationId(row.getString("publication_id"))
          .bundleHash(row.getString("bundle_hash"))
          .build();
    }

    private static int nz(Integer value) {
      return value == null ? 0 : value;
    }
  }
}
