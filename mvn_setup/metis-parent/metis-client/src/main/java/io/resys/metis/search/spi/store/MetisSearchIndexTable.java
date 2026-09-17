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

import java.util.ArrayList;

import io.resys.metis.search.api.ImmutableMetisSearchResult;
import io.resys.metis.search.api.MetisSearchResult;
import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.api.annotations.TenantSql.SqlBuilder;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;

@TenantSql.Table(
    name = "metis_search_index",
    order = 0,
    ddl = """
      CREATE TABLE IF NOT EXISTS {metis_search_index} (
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
    """,
    drop = """
      DROP TABLE IF EXISTS {metis_search_index};
    """
)
public interface MetisSearchIndexTable {

  @TenantSql.FindAll(
      sql = "SELECT workflow_id, locale, content_hash FROM {metis_search_index}",
      rowMapper = HashMapper.class
  )
  Sql findContentHashes();

  @TenantSql.Find(
      optional = false,
      sql = "SELECT COUNT(*) AS count FROM {metis_search_index}",
      rowMapper = CountMapper.class
  )
  Sql countDocuments();

  @TenantSql.Find(
      optional = false,
      sql = """
        SELECT format_type(atttypid, atttypmod) AS column_type
        FROM pg_attribute
        WHERE attrelid = '{metis_search_index}'::regclass
          AND attname = 'embedding'
          AND NOT attisdropped
        """,
      rowMapper = ColumnTypeMapper.class
  )
  Sql embeddingColumnType();

  @TenantSql.Find(
      optional = true,
      sql = """
        INSERT INTO {metis_search_index} (
            workflow_id, topic_id, locale, title, category, search_text, ai_metadata,
            search_vector, embedding, content_hash, indexed_at
        ) VALUES (
            $1, $2, $3, $4, $5, $6, $7,
            setweight(to_tsvector($8::regconfig, $9), 'A') ||
            setweight(to_tsvector($8::regconfig, coalesce($10, '')), 'B') ||
            setweight(to_tsvector($8::regconfig, coalesce($11, '')), 'C') ||
            setweight(to_tsvector($8::regconfig, coalesce($12, '')), 'D'),
            $13::vector, $14, NOW()
        )
        ON CONFLICT (workflow_id, locale) DO UPDATE SET
            topic_id = EXCLUDED.topic_id,
            title = EXCLUDED.title,
            category = EXCLUDED.category,
            search_text = EXCLUDED.search_text,
            ai_metadata = EXCLUDED.ai_metadata,
            search_vector = EXCLUDED.search_vector,
            embedding = EXCLUDED.embedding,
            content_hash = EXCLUDED.content_hash,
            indexed_at = EXCLUDED.indexed_at
        RETURNING workflow_id
        """,
      rowMapper = StringMapper.class
  )
  SqlTuple upsert(
      String workflowId,
      String topicId,
      String locale,
      String title,
      String category,
      String searchText,
      String aiMetadata,
      String tsConfig,
      String titleText,
      String description,
      String metadataText,
      String supplementalText,
      String embeddingLiteral,
      String contentHash);

  @TenantSql.FindAll(
      sql = """
        SELECT w.workflow_id, w.topic_id, w.title, w.category, w.locale,
               ts_rank_cd(w.search_vector, websearch_to_tsquery($1::regconfig, $2)) AS rank
        FROM {metis_search_index} w
        WHERE w.locale = $3
          AND w.search_vector @@ websearch_to_tsquery($1::regconfig, $2)
        ORDER BY rank DESC
        LIMIT $4
        """,
      rowMapper = FtsResultMapper.class
  )
  SqlTuple findFtsPrimary(String tsConfig, String query, String locale, int limit);

  @TenantSql.FindAll(
      sql = """
        WITH orq AS (
            SELECT NULLIF(replace(plainto_tsquery($1::regconfig, $2)::text, ' & ', ' | '), '') AS qt
        )
        SELECT w.workflow_id, w.topic_id, w.title, w.category, w.locale,
               GREATEST(
                   CASE WHEN (SELECT qt FROM orq) IS NOT NULL
                        THEN ts_rank_cd(w.search_vector, to_tsquery($1::regconfig, (SELECT qt FROM orq)))
                        ELSE 0 END,
                   word_similarity(unaccent($2), unaccent(w.search_text))
               ) AS rank
        FROM {metis_search_index} w
        WHERE w.locale = $3
          AND (
                ( (SELECT qt FROM orq) IS NOT NULL
                  AND w.search_vector @@ to_tsquery($1::regconfig, (SELECT qt FROM orq)) )
                OR word_similarity(unaccent($2), unaccent(w.search_text)) >= $4
              )
        ORDER BY rank DESC
        LIMIT $5
        """,
      rowMapper = FtsResultMapper.class
  )
  SqlTuple findFtsFallback(String tsConfig, String query, String locale, double threshold, int limit);

  @TenantSql.FindAll(
      sql = """
        SELECT workflow_id, topic_id, title, category, locale,
               1 - (embedding <=> $1::vector) AS score
        FROM {metis_search_index}
        WHERE locale = $2 AND embedding IS NOT NULL
        ORDER BY embedding <=> $1::vector
        LIMIT $3
        """,
      rowMapper = VectorResultMapper.class
  )
  SqlTuple findNearest(String embeddingLiteral, String locale, int limit);

  @TenantSql.FindAll(
      sql = "DELETE FROM {metis_search_index} WHERE $1::boolean RETURNING workflow_id",
      rowMapper = StringMapper.class
  )
  SqlTuple deleteAll(Boolean unused);

  @TenantSql.FindAll(
      sql = "DELETE FROM {metis_search_index} WHERE (workflow_id, locale) NOT IN ",
      rowMapper = StringMapper.class,
      sqlBuilder = DeleteStaleSql.class
  )
  SqlTuple deleteStale(MetisSearchKeepKeys keep);

  @TenantSql.FindAll(
      sql = """
        UPDATE {metis_search_index}
        SET embedding = $3::vector
        WHERE workflow_id = $1 AND locale = $2
        RETURNING workflow_id
        """,
      rowMapper = StringMapper.class
  )
  SqlTuple setEmbedding(String workflowId, String locale, String embeddingLiteral);

  class HashMapper implements TenantSql.RowMapper<MetisSearchContentHash> {
    @Override
    public MetisSearchContentHash apply(Row row) {
      return new MetisSearchContentHash(
          row.getString("workflow_id"),
          row.getString("locale"),
          row.getString("content_hash"));
    }
  }

  class CountMapper implements TenantSql.RowMapper<Long> {
    @Override
    public Long apply(Row row) {
      final var count = row.getLong("count");
      return count == null ? 0L : count;
    }
  }

  class ColumnTypeMapper implements TenantSql.RowMapper<String> {
    @Override
    public String apply(Row row) {
      return row.getString("column_type");
    }
  }

  class StringMapper implements TenantSql.RowMapper<String> {
    @Override
    public String apply(Row row) {
      return row.getString(0);
    }
  }

  class FtsResultMapper implements TenantSql.RowMapper<MetisSearchResult> {
    @Override
    public MetisSearchResult apply(Row row) {
      final var rank = row.getDouble("rank");
      return ImmutableMetisSearchResult.builder()
          .workflowId(row.getString("workflow_id"))
          .topicId(row.getString("topic_id"))
          .title(row.getString("title"))
          .category(row.getString("category"))
          .locale(row.getString("locale"))
          .score(rank)
          .ftsScore(rank)
          .build();
    }
  }

  class VectorResultMapper implements TenantSql.RowMapper<MetisSearchResult> {
    @Override
    public MetisSearchResult apply(Row row) {
      final var score = row.getDouble("score");
      return ImmutableMetisSearchResult.builder()
          .workflowId(row.getString("workflow_id"))
          .topicId(row.getString("topic_id"))
          .title(row.getString("title"))
          .category(row.getString("category"))
          .locale(row.getString("locale"))
          .score(score)
          .vectorScore(score)
          .build();
    }
  }

  class DeleteStaleSql implements SqlBuilder<MetisSearchKeepKeys> {
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, MetisSearchKeepKeys keep) {
      final var sql = new StringBuilder(baseline);
      final var params = new ArrayList<Object>();
      var index = 1;
      sql.append('(');
      for (int i = 0; i < keep.keys().size(); i++) {
        if (i > 0) {
          sql.append(',');
        }
        sql.append("($").append(index++).append(",$").append(index++).append(')');
        params.add(keep.keys().get(i).workflowId());
        params.add(keep.keys().get(i).locale());
      }
      sql.append(") RETURNING workflow_id");
      return ImmutableSqlTuple.builder()
          .value(sql.toString())
          .props(Tuple.from(params))
          .build();
    }
  }
}
