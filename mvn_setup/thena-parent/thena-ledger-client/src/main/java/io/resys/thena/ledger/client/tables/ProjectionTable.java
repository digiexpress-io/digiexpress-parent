package io.resys.thena.ledger.client.tables;

/*-
 * #%L
 * thena-ledger-client
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
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.resys.thena.ledger.client.entities.ImmutableProjection;
import io.resys.thena.ledger.client.entities.ImmutableProjectionTransitives;
import io.resys.thena.ledger.client.entities.Projection;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "projection",
  order = 600,
  ddl = """
    CREATE TABLE IF NOT EXISTS {projection}
    (
      id UUID PRIMARY KEY,
      ledger_id UUID NOT NULL,
      external_id VARCHAR(255) NOT NULL,
      
      projection_type VARCHAR(100) NOT NULL,
      projection_sub_type VARCHAR(100),
      projection_description TEXT,
      projection_target_date DATE NOT NULL,
      projection_start_date DATE NOT NULL,
      projection_end_date DATE NOT NULL,
      projection_amount DECIMAL(15,2) NOT NULL,
      
      created_commit_id UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {projection}_LEDGER_INDEX
      ON {projection} (ledger_id);
    CREATE INDEX IF NOT EXISTS {projection}_EXTERNAL_INDEX
      ON {projection} (external_id);
    CREATE INDEX IF NOT EXISTS {projection}_TARGET_DATE_INDEX
      ON {projection} (projection_target_date);
    CREATE INDEX IF NOT EXISTS {projection}_DATE_RANGE_INDEX
      ON {projection} (projection_start_date, projection_end_date);
  """,
  constraints = """
    ALTER TABLE {projection} ADD CONSTRAINT fk_projection_ledger 
      FOREIGN KEY (ledger_id) REFERENCES {ledger}(id);
  """,
  drop = """
    DROP TABLE IF EXISTS {projection} CASCADE;
  """
)
public interface ProjectionTable {

  @TenantSql.FindAll(
    sql = """
      SELECT projection.*,
             created_commit.created_at as created_at
      FROM {projection} projection
      LEFT JOIN {commit} created_commit ON projection.created_commit_id = created_commit.commit_id
      LEFT JOIN {ledger} ledger ON projection.ledger_id = ledger.id
    """,
    rowMapper = ProjectionMapper.class,
    sqlBuilder = LedgerTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(LedgerTableFilter filter);

  @TenantSql.FindAll(
    sql = """
      SELECT projection.*,
             created_commit.created_at as created_at
      FROM {projection} projection
      LEFT JOIN {commit} created_commit ON projection.created_commit_id = created_commit.commit_id
      ORDER BY projection_target_date ASC
    """,
    rowMapper = ProjectionMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT projection.*,
             created_commit.created_at as created_at
      FROM {projection} projection
      LEFT JOIN {commit} created_commit ON projection.created_commit_id = created_commit.commit_id
      WHERE ledger_id = $1
      ORDER BY projection_target_date ASC
    """,
    rowMapper = ProjectionMapper.class
  )
  SqlTuple findAllByLedgerId(String ledgerId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT projection.*,
             created_commit.created_at as created_at
      FROM {projection} projection
      LEFT JOIN {commit} created_commit ON projection.created_commit_id = created_commit.commit_id
      WHERE id = $1
    """,
    rowMapper = ProjectionMapper.class
  )
  SqlTuple getById(String projectionId);

  @TenantSql.Find(
    optional = true,
    sql = """
      SELECT projection.*,
             created_commit.created_at as created_at
      FROM {projection} projection
      LEFT JOIN {commit} created_commit ON projection.created_commit_id = created_commit.commit_id
      WHERE external_id = $1
    """,
    rowMapper = ProjectionMapper.class
  )
  SqlTuple findByExternalId(String externalId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {projection}
      (id, ledger_id, external_id, projection_type, projection_sub_type, 
       projection_description, projection_target_date, projection_start_date, projection_end_date, 
       projection_amount, created_commit_id)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
    """,
    propsMapper = ProjectionInsertMapper.class
  )
  SqlTupleList insertMany(List<Projection> projections);

  // Mapper classes
  class ProjectionMapper implements TenantSql.RowMapper<Projection> {
    @Override
    public Projection apply(Row row) {
      return ImmutableProjection.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .ledgerId(TableUtils.toStringUUID(row, "ledger_id"))
          .externalId(row.getString("external_id"))
          .projectionType(row.getString("projection_type"))
          .projectionSubType(Optional.ofNullable(row.getString("projection_sub_type")))
          .projectionDescription(Optional.ofNullable(row.getString("projection_description")))
          .projectionTargetDate(row.getLocalDate("projection_target_date"))
          .projectionStartDate(row.getLocalDate("projection_start_date"))
          .projectionEndDate(row.getLocalDate("projection_end_date"))
          .projectionAmount(row.getBigDecimal("projection_amount"))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))
          .transitives(ImmutableProjectionTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .build())
          .build();
    }
  }

  class ProjectionInsertMapper implements TenantSql.PropsMapper<Projection> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Projection doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getLedgerId()),
        doc.getExternalId(),
        doc.getProjectionType(),
        doc.getProjectionSubType().orElse(null),
        doc.getProjectionDescription().orElse(null),
        doc.getProjectionTargetDate(),
        doc.getProjectionStartDate(),
        doc.getProjectionEndDate(),
        doc.getProjectionAmount(),
        TableUtils.toUuid(doc.getCreatedCommitId())
      });
    }
  }
}