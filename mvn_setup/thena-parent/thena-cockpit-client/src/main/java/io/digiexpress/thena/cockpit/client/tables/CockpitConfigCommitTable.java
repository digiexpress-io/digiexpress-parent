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

import io.resys.thena.api.annotations.TenantSql;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfigCommit;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitConfigCommit;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "cockpit_config_commit",
  order = 100,
  ddl = """
    CREATE TABLE IF NOT EXISTS {cockpit_config_commit}
    (
      id UUID PRIMARY KEY,
      parent_id UUID,
      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
      commit_author VARCHAR(255) NOT NULL,
      commit_message VARCHAR(255) NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {cockpit_config_commit}_PARENT_INDEX
      ON {cockpit_config_commit} (parent_id);
    CREATE INDEX IF NOT EXISTS {cockpit_config_commit}_AUTH_INDEX
      ON {cockpit_config_commit} (commit_author);
  """,
  constraints = """
    ALTER TABLE {cockpit_config_commit} ADD CONSTRAINT fk_cockpit_config_commit_parent 
      FOREIGN KEY (parent_id) REFERENCES {cockpit_config_commit}(id);
  """,
  drop = """
    DROP TABLE IF EXISTS {cockpit_config_commit} CASCADE;
  """
)
public interface CockpitConfigCommitTable {

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {cockpit_config_commit}
      ORDER BY created_at DESC
    """,
    rowMapper = CockpitConfigCommitMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT * FROM {cockpit_config_commit}
      WHERE id = $1
    """,
    rowMapper = CockpitConfigCommitMapper.class
  )
  SqlTuple getById(String commitId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {cockpit_config_commit}
      (id, parent_id, created_at, commit_author, commit_message)
       VALUES($1, $2, $3, $4, $5)
    """,
    propsMapper = CockpitConfigCommitInsertMapper.class
  )
  SqlTupleList insertMany(List<CockpitConfigCommit> commits);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {cockpit_config_commit}
       SET parent_id = $1, created_at = $2, commit_author = $3, commit_message = $4
       WHERE id = $5
    """,
    propsMapper = CockpitConfigCommitUpdateMapper.class
  )
  SqlTupleList updateMany(List<CockpitConfigCommit> commits);

  // Mapper classes
  class CockpitConfigCommitMapper implements TenantSql.RowMapper<CockpitConfigCommit> {
    @Override
    public CockpitConfigCommit apply(Row row) {
      final String parent_id = TableUtils.toStringUUID(row, "parent_id");

      return ImmutableCockpitConfigCommit.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .parentId(Optional.ofNullable(parent_id))
          .createdAt(row.getOffsetDateTime("created_at"))
          .commitAuthor(row.getString("commit_author"))
          .commitMessage(row.getString("commit_message"))
          .build();
    }
  }

  class CockpitConfigCommitInsertMapper implements TenantSql.PropsMapper<CockpitConfigCommit> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(CockpitConfigCommit doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        doc.getParentId().map(TableUtils::toUuid).orElse(null),
        doc.getCreatedAt(),
        doc.getCommitAuthor(),
        doc.getCommitMessage()
      });
    }
  }

  class CockpitConfigCommitUpdateMapper implements TenantSql.PropsMapper<CockpitConfigCommit> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(CockpitConfigCommit doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getParentId().map(TableUtils::toUuid).orElse(null),
        doc.getCreatedAt(),
        doc.getCommitAuthor(),
        doc.getCommitMessage(),
        TableUtils.toUuid(doc.getId())
      });
    }
  }
}