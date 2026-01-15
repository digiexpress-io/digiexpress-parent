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

import io.digiexpress.thena.cockpit.client.api.entities.CockpitCommit;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitCommit;
import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "cockpit_commit",
  order = 100,
  ddl = """
    CREATE TABLE IF NOT EXISTS {cockpit_commit}
    (
      id UUID PRIMARY KEY,
      parent_id UUID,
      config_id UUID,
      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
      commit_author VARCHAR(255) NOT NULL,
      commit_message VARCHAR(255) NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {cockpit_commit}_PARENT_INDEX
      ON {cockpit_commit} (parent_id);
    CREATE INDEX IF NOT EXISTS {cockpit_commit}_AUTH_INDEX
      ON {cockpit_commit} (commit_author);
  """,
  constraints = """
    ALTER TABLE {cockpit_commit} ADD CONSTRAINT fk_cockpit_commit_parent 
      FOREIGN KEY (parent_id) REFERENCES {cockpit_commit}(id);
  """,
  drop = """
    DROP TABLE IF EXISTS {cockpit_commit} CASCADE;
  """
)
public interface CockpitCommitTable {

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {cockpit_commit}
      ORDER BY created_at DESC
    """,
    rowMapper = CockpitConfigCommitMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT * FROM {cockpit_commit}
      WHERE id = $1
    """,
    rowMapper = CockpitConfigCommitMapper.class
  )
  SqlTuple getById(String commitId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {cockpit_commit}
      (id, parent_id, config_id, created_at, commit_author, commit_message)
       VALUES($1, $2, $3, $4, $5, $6)
    """,
    propsMapper = CockpitCommitInsertMapper.class
  )
  SqlTupleList insertMany(List<CockpitCommit> commits);

  // Mapper classes
  class CockpitConfigCommitMapper implements TenantSql.RowMapper<CockpitCommit> {
    @Override
    public CockpitCommit apply(Row row) {
      final String parent_id = TableUtils.toStringUUID(row, "parent_id");

      return ImmutableCockpitCommit.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .configId(TableUtils.toStringUUID(row, "config_id"))
          .parentId(Optional.ofNullable(parent_id))
          .createdAt(row.getOffsetDateTime("created_at"))
          .commitAuthor(row.getString("commit_author"))
          .commitMessage(row.getString("commit_message"))
          .build();
    }
  }

  class CockpitCommitInsertMapper implements TenantSql.PropsMapper<CockpitCommit> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(CockpitCommit doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        doc.getParentId().map(TableUtils::toUuid).orElse(null),
        TableUtils.toUuid(doc.getConfigId()),
        doc.getCreatedAt(),
        doc.getCommitAuthor(),
        doc.getCommitMessage()
      });
    }
  }

}