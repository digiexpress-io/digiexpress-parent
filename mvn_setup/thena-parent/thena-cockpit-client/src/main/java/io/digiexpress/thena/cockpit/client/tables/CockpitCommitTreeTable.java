package io.digiexpress.thena.cockpit.client.tables;

/*-
 * #%L
 * thena-cockpit-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.util.List;
import java.util.Optional;

import io.digiexpress.thena.cockpit.client.api.entities.CockpitCommitTree;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitCommitTree.CockpitConfigCommitTreeOperation;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitCommitTree;
import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.support.TableUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "cockpit_commit_tree",
  order = 101,
  ddl = """
    CREATE TABLE IF NOT EXISTS {cockpit_commit_tree}
    (
      id UUID PRIMARY KEY,
      commit_id UUID NOT NULL,
      operation_type VARCHAR(40),
      body_after JSONB,
      body_before JSONB
    );

    CREATE INDEX IF NOT EXISTS {cockpit_commit_tree}_COMMIT_INDEX
      ON {cockpit_commit_tree} (commit_id);
  """,
  constraints = """
    ALTER TABLE {cockpit_commit_tree} ADD CONSTRAINT fk_cockpit_commit_tree_commit 
      FOREIGN KEY (commit_id) REFERENCES {cockpit_commit}(id);
  """,
  drop = """
    DROP TABLE IF EXISTS {cockpit_commit_tree} CASCADE;
  """
)
public interface CockpitCommitTreeTable {

  @TenantSql.FindAll(
    sql = """
      SELECT ct.*
      FROM {cockpit_commit_tree} ct
      LEFT JOIN {cockpit_commit} c ON ct.commit_id = c.id
      ORDER BY c.created_at DESC
    """,
    rowMapper = CockpitConfigCommitTreeMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {cockpit_commit_tree}
      WHERE commit_id = $1
    """,
    rowMapper = CockpitConfigCommitTreeMapper.class
  )
  SqlTuple findAllByCommitId(String commitId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT * FROM {cockpit_commit_tree}
      WHERE id = $1
    """,
    rowMapper = CockpitConfigCommitTreeMapper.class
  )
  SqlTuple getById(String id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {cockpit_commit_tree}
      (id, commit_id, operation_type, body_after, body_before)
       VALUES($1, $2, $3, $4, $5)
    """,
    propsMapper = CockpitCommitTreeInsertMapper.class
  )
  SqlTupleList insertMany(List<CockpitCommitTree> commitTrees);

  // Mapper classes
  class CockpitConfigCommitTreeMapper implements TenantSql.RowMapper<CockpitCommitTree> {
    @Override
    public CockpitCommitTree apply(Row row) {
      final String operation_type = row.getString("operation_type");
      final JsonObject body_after = row.getJsonObject("body_after");
      final JsonObject body_before = row.getJsonObject("body_before");

      return ImmutableCockpitCommitTree.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .commitId(TableUtils.toStringUUID(row, "commit_id"))
          .operationType(CockpitConfigCommitTreeOperation.valueOf(operation_type))
          .bodyAfter(Optional.ofNullable(body_after))
          .bodyBefore(Optional.ofNullable(body_before))
          .build();
    }
  }

  class CockpitCommitTreeInsertMapper implements TenantSql.PropsMapper<CockpitCommitTree> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(CockpitCommitTree doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getCommitId()),
        doc.getOperationType().name(),
        doc.getBodyAfter().orElse(null),
        doc.getBodyBefore().orElse(null)
      });
    }
  }
}