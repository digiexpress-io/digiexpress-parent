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
import io.resys.thena.ledger.client.entities.ImmutableProjectionDetail;
import io.resys.thena.ledger.client.entities.ImmutableProjectionDetailTransitives;
import io.resys.thena.ledger.client.entities.ProjectionDetail;
import io.resys.thena.support.TableUtils;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "projection_detail",
  order = 1000,
  ddl = """
    CREATE TABLE IF NOT EXISTS {projection_detail}
    (
      id UUID PRIMARY KEY,
      
      projection_id UUID NOT NULL,
      external_id VARCHAR(255) NOT NULL,
      target_id VARCHAR(255),
            
      detail_type VARCHAR(100) NOT NULL,
      detail_sub_type VARCHAR(100),
      detail_description TEXT,
      detail_start_date DATE NOT NULL,
      detail_end_date DATE NOT NULL,
      detail_amount DECIMAL(15,2) NOT NULL,
      detail_formula VARCHAR(255),
      detail_body JSONB,

      created_commit_id UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {projection_detail}_PROJECTION_INDEX
      ON {projection_detail} (projection_id);
    CREATE INDEX IF NOT EXISTS {projection_detail}_EXTERNAL_INDEX
      ON {projection_detail} (external_id);
    CREATE INDEX IF NOT EXISTS {projection_detail}_TARGET_INDEX
      ON {projection_detail} (target_id);
    CREATE INDEX IF NOT EXISTS {projection_detail}_DATE_RANGE_INDEX
      ON {projection_detail} (detail_start_date, detail_end_date);
  """,
  constraints = """
    ALTER TABLE {projection_detail} ADD CONSTRAINT fk_projection_detail_projection 
      FOREIGN KEY (projection_id) REFERENCES {projection}(id);
    ALTER TABLE {projection_detail} ADD CONSTRAINT fk_projection_detail_created_commit 
      FOREIGN KEY (created_commit_id) REFERENCES {commit}(commit_id);
  """,
  drop = """
    DROP TABLE {projection_detail};
  """
)
public interface ProjectionDetailTable {

  @TenantSql.FindAll(
    sql = """
      SELECT projection_detail.*,
             created_commit.created_at as created_at
      FROM {projection_detail} projection_detail
      LEFT JOIN {commit} created_commit ON projection_detail.created_commit_id = created_commit.commit_id
      LEFT JOIN {projection} projection ON projection_detail.projection_id = projection.id
      LEFT JOIN {ledger} ledger ON projection.ledger_id = ledger.id
    """,
    rowMapper = ProjectionDetailMapper.class,
    sqlBuilder = LedgerTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(LedgerTableFilter filter);

  @TenantSql.FindAll(
    sql = """
      SELECT projection_detail.*,
             created_commit.created_at as created_at
      FROM {projection_detail} projection_detail
      LEFT JOIN {commit} created_commit ON projection_detail.created_commit_id = created_commit.commit_id
      ORDER BY detail_start_date DESC
    """,
    rowMapper = ProjectionDetailMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT projection_detail.*,
             created_commit.created_at as created_at
      FROM {projection_detail} projection_detail
      LEFT JOIN {commit} created_commit ON projection_detail.created_commit_id = created_commit.commit_id
      WHERE projection_id = $1
      ORDER BY detail_start_date ASC
    """,
    rowMapper = ProjectionDetailMapper.class
  )
  SqlTuple findAllByProjectionId(String projectionId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT projection_detail.*,
             created_commit.created_at as created_at
      FROM {projection_detail} projection_detail
      LEFT JOIN {commit} created_commit ON projection_detail.created_commit_id = created_commit.commit_id
      WHERE id = $1
    """,
    rowMapper = ProjectionDetailMapper.class
  )
  SqlTuple getById(String detailId);

  @TenantSql.Find(
    optional = true,
    sql = """
      SELECT projection_detail.*,
             created_commit.created_at as created_at
      FROM {projection_detail} projection_detail
      LEFT JOIN {commit} created_commit ON projection_detail.created_commit_id = created_commit.commit_id
      WHERE external_id = $1
    """,
    rowMapper = ProjectionDetailMapper.class
  )
  SqlTuple findByExternalId(String externalId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {projection_detail}
      (id, projection_id, external_id, detail_type, detail_sub_type, 
       detail_description, target_id, detail_start_date, detail_end_date, 
       detail_amount, detail_formula, detail_body, created_commit_id)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13)
    """,
    propsMapper = ProjectionDetailInsertMapper.class
  )
  SqlTupleList insertMany(List<ProjectionDetail> details);

  // Mapper classes
  class ProjectionDetailMapper implements TenantSql.RowMapper<ProjectionDetail> {
    @Override
    public ProjectionDetail apply(Row row) {
      final JsonObject detail_body = row.getJsonObject("detail_body");

      return ImmutableProjectionDetail.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .projectionId(TableUtils.toStringUUID(row, "projection_id"))
          .externalId(row.getString("external_id"))
          .detailType(row.getString("detail_type"))
          .detailSubType(Optional.ofNullable(row.getString("detail_sub_type")))
          .detailDescription(Optional.ofNullable(row.getString("detail_description")))
          .targetId(Optional.ofNullable(row.getString("target_id")))
          .detailStartDate(row.getLocalDate("detail_start_date"))
          .detailEndDate(row.getLocalDate("detail_end_date"))
          .detailAmount(row.getBigDecimal("detail_amount"))
          .detailFormula(Optional.ofNullable(row.getString("detail_formula")))
          .detailBody(Optional.ofNullable(detail_body))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))
          .transitives(ImmutableProjectionDetailTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .build())
          .build();
    }
  }

  class ProjectionDetailInsertMapper implements TenantSql.PropsMapper<ProjectionDetail> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(ProjectionDetail doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getProjectionId()),
        doc.getExternalId(),
        doc.getDetailType(),
        doc.getDetailSubType().orElse(null),
        doc.getDetailDescription().orElse(null),
        doc.getTargetId().orElse(null),
        doc.getDetailStartDate(),
        doc.getDetailEndDate(),
        doc.getDetailAmount(),
        doc.getDetailFormula().orElse(null),
        doc.getDetailBody().orElse(null),
        TableUtils.toUuid(doc.getCreatedCommitId())
      });
    }
  }
}