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

import java.time.Duration;

import io.resys.metis.search.spi.store.spi.MetisSearchDbImpl;
import io.resys.thena.storesql.PgErrors;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;

public final class MetisSearchSql {

  private static final Duration TIMEOUT = Duration.ofSeconds(30);

  private MetisSearchSql() {}

  public static MetisSearchDb create(Pool pgPool) {
    return MetisSearchDbImpl.create()
        .client(pgPool)
        .errorHandler(new PgErrors())
        .build();
  }

  /**
   * Adds pgvector-dependent objects the first time search is enabled. Flyway {@code V4_1}
   * only creates built-in-type tables so vanilla PostgreSQL still migrates.
   */
  public static void ensureSearchExtensions(Pool pgPool, int embeddingDimension) {
    if (embeddingDimension < 1) {
      throw new IllegalArgumentException("embeddingDimension must be positive");
    }
    try {
      exec(pgPool, "CREATE EXTENSION IF NOT EXISTS vector");
      exec(pgPool, "CREATE EXTENSION IF NOT EXISTS pg_trgm");
      exec(pgPool, "CREATE EXTENSION IF NOT EXISTS unaccent");
      exec(pgPool, "ALTER TABLE metis_search_index ADD COLUMN IF NOT EXISTS embedding VECTOR("
          + embeddingDimension + ")");
      exec(pgPool, """
          CREATE INDEX IF NOT EXISTS idx_metis_search_embedding
            ON metis_search_index USING hnsw (embedding vector_cosine_ops)
          """);
      exec(pgPool, """
          CREATE INDEX IF NOT EXISTS idx_metis_search_trgm
            ON metis_search_index USING GIN (search_text gin_trgm_ops)
          """);
      exec(pgPool, "COMMENT ON COLUMN metis_search_index.embedding IS "
          + "'Embedding vector written at index time. Width matches the embedding model (1024 for bge-m3)'");
      exec(pgPool, "COMMENT ON INDEX idx_metis_search_embedding IS "
          + "'HNSW cosine index for nearest-neighbour vector search'");
      exec(pgPool, "COMMENT ON INDEX idx_metis_search_trgm IS "
          + "'Trigram GIN index for the typo / compound-word fallback over search_text'");
    } catch (RuntimeException e) {
      throw new IllegalStateException(
          "eveli.metis.search.enabled is true but PostgreSQL does not have pgvector "
              + "(extension \"vector\") or the app role cannot CREATE EXTENSION. "
              + "Use image pgvector/pgvector:pg17 and keep the existing volume "
              + "(do not delete PGDATA), or have a DBA run: "
              + "CREATE EXTENSION vector; CREATE EXTENSION pg_trgm; CREATE EXTENSION unaccent. "
              + "Cause: " + e.getMessage(),
          e);
    }
  }

  private static void exec(Pool pgPool, String sql) {
    await(pgPool.query(sql).execute());
  }

  public static void truncate(MetisSearchDb db) {
    await(db.query().queryMetisSearchIndex().deleteAll(true));
    await(db.query().queryMetisSearchReindexJob().deleteAll(true));
  }

  public static <T> T await(Uni<T> uni) {
    return uni.await().atMost(TIMEOUT);
  }

  public static String toVectorLiteral(float[] values) {
    if (values == null) {
      return null;
    }
    final var text = new StringBuilder(values.length * 8).append('[');
    for (int i = 0; i < values.length; i++) {
      if (i > 0) {
        text.append(',');
      }
      text.append(values[i]);
    }
    return text.append(']').toString();
  }

  public static boolean isDuplicate(Throwable error) {
    final var handler = new PgErrors();
    for (var cause = error; cause != null; cause = cause.getCause()) {
      if (handler.duplicate(cause)) {
        return true;
      }
    }
    return false;
  }
}
