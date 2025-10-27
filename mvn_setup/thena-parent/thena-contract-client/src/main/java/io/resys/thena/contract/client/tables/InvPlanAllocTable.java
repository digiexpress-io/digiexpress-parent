package io.resys.thena.contract.client.tables;

/*-
 * #%L
 * thena-contract-client
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.contract.client.entities.ImmutableInvPlanAlloc;
import io.resys.thena.contract.client.entities.ImmutableInvPlanAllocTransitives;
import io.resys.thena.contract.client.entities.InvPlanAlloc;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "inv_plan_alloc",
  order = 8,
  ddl = """
    CREATE TABLE IF NOT EXISTS {inv_plan_alloc}
    (
      id                              UUID PRIMARY KEY,
      inv_plan_id                     UUID NOT NULL,

      commit_id                       UUID NOT NULL,
      created_commit_id               UUID NOT NULL,

      inv_plan_alloc_code             VARCHAR(100) NOT NULL,
      inv_plan_alloc_name             VARCHAR(255) NOT NULL,
      inv_plan_alloc_percentage       DECIMAL(10,6) NOT NULL,
      inv_plan_alloc_status           VARCHAR(100) NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {inv_plan_alloc}_STATUS_INDEX
      ON {inv_plan_alloc} (inv_plan_alloc_status);
    CREATE INDEX IF NOT EXISTS {inv_plan_alloc}_CODE_INDEX
      ON {inv_plan_alloc} (inv_plan_alloc_code);
    CREATE INDEX IF NOT EXISTS {inv_plan_alloc}_INV_PLAN_INDEX
      ON {inv_plan_alloc} (inv_plan_id);
    CREATE INDEX IF NOT EXISTS {inv_plan_alloc}_COMMIT_INDEX
      ON {inv_plan_alloc} (commit_id);
    CREATE INDEX IF NOT EXISTS {inv_plan_alloc}_CREATED_COMMIT_INDEX
      ON {inv_plan_alloc} (created_commit_id);
  """,
  constraints = """
    ALTER TABLE {inv_plan_alloc} ADD CONSTRAINT fk_inv_plan_alloc_inv_plan 
      FOREIGN KEY (inv_plan_id) REFERENCES {inv_plan}(id);
  """,
  drop = """
    DROP TABLE {inv_plan_alloc};
  """
)
public interface InvPlanAllocTable {

  @TenantSql.FindAll(
    sql = """
      SELECT ia.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at,
             ip.contract_id as contract_id
      FROM {inv_plan_alloc} ia
      LEFT JOIN {commit} updated_commit ON ia.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON ia.created_commit_id = created_commit.id
      LEFT JOIN {inv_plan} ip ON ia.inv_plan_id = ip.id
    """,
    rowMapper = InvPlanAllocMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT ia.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at,
             ip.contract_id as contract_id
      FROM {inv_plan_alloc} ia
      LEFT JOIN {commit} updated_commit ON ia.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON ia.created_commit_id = created_commit.id
      LEFT JOIN {inv_plan} ip ON ia.inv_plan_id = ip.id
      WHERE ip.contract_id = $1
    """,
    rowMapper = InvPlanAllocMapper.class
  )
  SqlTuple findAllByContractId(UUID contractId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT ia.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at,
             ip.contract_id as contract_id
      FROM {inv_plan_alloc} ia
      LEFT JOIN {commit} updated_commit ON ia.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON ia.created_commit_id = created_commit.id
      LEFT JOIN {inv_plan} ip ON ia.inv_plan_id = ip.id
      WHERE ia.id = $1
    """,
    rowMapper = InvPlanAllocMapper.class
  )
  SqlTuple getById(UUID id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {inv_plan_alloc}
      (id, inv_plan_id, commit_id, created_commit_id,
       inv_plan_alloc_code, inv_plan_alloc_name, inv_plan_alloc_percentage, inv_plan_alloc_status)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8)
    """,
    propsMapper = InvPlanAllocInsertMapper.class
  )
  SqlTupleList insertMany(List<InvPlanAlloc> invPlanAllocs);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {inv_plan_alloc}
       SET inv_plan_id = $1, commit_id = $2,
           inv_plan_alloc_code = $3, inv_plan_alloc_name = $4, inv_plan_alloc_percentage = $5, inv_plan_alloc_status = $6
       WHERE id = $7
    """,
    propsMapper = InvPlanAllocUpdateMapper.class
  )
  SqlTupleList updateMany(List<InvPlanAlloc> invPlanAllocs);

  // Mapper classes
  class InvPlanAllocMapper implements TenantSql.RowMapper<InvPlanAlloc> {
    @Override
    public InvPlanAlloc apply(Row row) {
      return ImmutableInvPlanAlloc.builder()
          .id(UUID.fromString(row.getString("id")))
          .invPlanId(UUID.fromString(row.getString("inv_plan_id")))

          .commitId(UUID.fromString(row.getString("commit_id")))
          .createdCommitId(UUID.fromString(row.getString("created_commit_id")))

          // Transitive data from joins (including virtual contract_id)
          .transitives(ImmutableInvPlanAllocTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .updatedAt(row.getOffsetDateTime("updated_at"))
              .contractId(UUID.fromString(row.getString("contract_id")))
              .build())

          .invPlanAllocCode(row.getString("inv_plan_alloc_code"))
          .invPlanAllocName(row.getString("inv_plan_alloc_name"))
          .invPlanAllocPercentage(row.get(BigDecimal.class, "inv_plan_alloc_percentage"))
          .invPlanAllocStatus(row.getString("inv_plan_alloc_status"))

          .build();
    }
  }

  class InvPlanAllocInsertMapper implements TenantSql.PropsMapper<InvPlanAlloc> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(InvPlanAlloc doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getId(),
        doc.getInvPlanId(),
        doc.getCommitId(),
        doc.getCreatedCommitId(),
        doc.getInvPlanAllocCode(),
        doc.getInvPlanAllocName(),
        doc.getInvPlanAllocPercentage(),
        doc.getInvPlanAllocStatus()
      });
    }
  }

  class InvPlanAllocUpdateMapper implements TenantSql.PropsMapper<InvPlanAlloc> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(InvPlanAlloc doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getInvPlanId(),
        doc.getCommitId(),
        doc.getInvPlanAllocCode(),
        doc.getInvPlanAllocName(),
        doc.getInvPlanAllocPercentage(),
        doc.getInvPlanAllocStatus(),
        doc.getId()
      });
    }
  }
}
