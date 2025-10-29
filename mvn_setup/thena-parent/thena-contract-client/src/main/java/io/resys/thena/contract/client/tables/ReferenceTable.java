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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.contract.client.entities.ContractEntity.ContractOneOfRelations;
import io.resys.thena.contract.client.entities.ContractEntity.ContractRelationType;
import io.resys.thena.contract.client.entities.ImmutableContractOneOfRelations;
import io.resys.thena.contract.client.entities.ImmutableReference;
import io.resys.thena.contract.client.entities.ImmutableReferenceTransitives;
import io.resys.thena.contract.client.entities.Reference;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "reference",
  order = 4,
  ddl = """
    CREATE TABLE IF NOT EXISTS {reference}
    (
      id                              UUID PRIMARY KEY,
      contract_id                     UUID NOT NULL,

      inv_plan_id                     UUID,
      inv_plan_alloc_id               UUID,
      coverage_id                     UUID,
      party_id                        UUID,

      commit_id                       UUID NOT NULL,
      created_commit_id               UUID NOT NULL,

      reference_value                 VARCHAR(255) NOT NULL,
      reference_type                  VARCHAR(100) NOT NULL,
      reference_body                  JSONB
    );

    CREATE INDEX IF NOT EXISTS {reference}_TYPE_INDEX
      ON {reference} (reference_type);
    CREATE INDEX IF NOT EXISTS {reference}_CONTRACT_INDEX
      ON {reference} (contract_id);
    CREATE INDEX IF NOT EXISTS {reference}_INV_PLAN_INDEX
      ON {reference} (inv_plan_id);
    CREATE INDEX IF NOT EXISTS {reference}_INV_PLAN_ALLOC_INDEX
      ON {reference} (inv_plan_alloc_id);
    CREATE INDEX IF NOT EXISTS {reference}_COVERAGE_INDEX
      ON {reference} (coverage_id);
    CREATE INDEX IF NOT EXISTS {reference}_PARTY_INDEX
      ON {reference} (party_id);
    CREATE INDEX IF NOT EXISTS {reference}_COMMIT_INDEX
      ON {reference} (commit_id);
    CREATE INDEX IF NOT EXISTS {reference}_CREATED_COMMIT_INDEX
      ON {reference} (created_commit_id);
  """,
  constraints = """
    ALTER TABLE {reference} ADD CONSTRAINT fk_reference_contract 
      FOREIGN KEY (contract_id) REFERENCES {contract}(id);
    ALTER TABLE {reference} ADD CONSTRAINT fk_reference_inv_plan 
      FOREIGN KEY (inv_plan_id) REFERENCES {inv_plan}(id);
    ALTER TABLE {reference} ADD CONSTRAINT fk_reference_inv_plan_alloc 
      FOREIGN KEY (inv_plan_alloc_id) REFERENCES {inv_plan_alloc}(id);
    ALTER TABLE {reference} ADD CONSTRAINT fk_reference_coverage 
      FOREIGN KEY (coverage_id) REFERENCES {coverage}(id);
    ALTER TABLE {reference} ADD CONSTRAINT fk_reference_party 
      FOREIGN KEY (party_id) REFERENCES {party}(id);
  """,
  drop = """
    DROP TABLE {reference};
  """
)
public interface ReferenceTable {

  @TenantSql.FindAll(
    sql = """
      SELECT r.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {reference} r
      LEFT JOIN {commit} updated_commit ON r.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON r.created_commit_id = created_commit.id
    """,
    rowMapper = ReferenceMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT r.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {reference} r
      LEFT JOIN {commit} updated_commit ON r.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON r.created_commit_id = created_commit.id
      WHERE r.contract_id = $1
    """,
    rowMapper = ReferenceMapper.class
  )
  SqlTuple findAllByContractId(UUID contractId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT r.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {reference} r
      LEFT JOIN {commit} updated_commit ON r.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON r.created_commit_id = created_commit.id
      WHERE r.id = $1
    """,
    rowMapper = ReferenceMapper.class
  )
  SqlTuple getById(UUID id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {reference}
      (id, contract_id, inv_plan_id, inv_plan_alloc_id, coverage_id, party_id, commit_id, created_commit_id,
       reference_value, reference_type, reference_body)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
    """,
    propsMapper = ReferenceInsertMapper.class
  )
  SqlTupleList insertMany(List<Reference> references);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {reference}
       SET contract_id = $1, inv_plan_id = $2, inv_plan_alloc_id = $3, coverage_id = $4, party_id = $5, commit_id = $6,
           reference_value = $7, reference_type = $8, reference_body = $9
       WHERE id = $10
    """,
    propsMapper = ReferenceUpdateMapper.class
  )
  SqlTupleList updateMany(List<Reference> references);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {reference} WHERE id = $1",
    propsMapper = ReferenceDeleteMapper.class
  )
  SqlTupleList deleteAll(Collection<Reference> references);

  // Mapper classes
  class ReferenceMapper implements TenantSql.RowMapper<Reference> {
    @Override
    public Reference apply(Row row) {
      final var inv_plan_id = row.getString("inv_plan_id");
      final var inv_plan_alloc_id = row.getString("inv_plan_alloc_id");
      final var coverage_id = row.getString("coverage_id");
      final var party_id = row.getString("party_id");
      final var reference_body = row.getJsonObject("reference_body");

      ContractOneOfRelations relations = null;
      if (inv_plan_id != null) {
        relations = ImmutableContractOneOfRelations.builder()
            .relationType(ContractRelationType.INV_PLAN)
            .invPlanId(inv_plan_id)
            .build();
      } else if (inv_plan_alloc_id != null) {
        relations = ImmutableContractOneOfRelations.builder()
            .relationType(ContractRelationType.INV_PLAN_ALLOC)
            .invPlanAllocId(inv_plan_alloc_id)
            .build();
      } else if (coverage_id != null) {
        relations = ImmutableContractOneOfRelations.builder()
            .relationType(ContractRelationType.COVERAGE)
            .coverageId(coverage_id)
            .build();
      } else if (party_id != null) {
        relations = ImmutableContractOneOfRelations.builder()
            .relationType(ContractRelationType.PARTY)
            .partyId(party_id)
            .build();
      }

      return ImmutableReference.builder()
          .id(row.getString("id"))
          .contractId(row.getString("contract_id"))

          .relations(relations)
          .commitId(row.getString("commit_id"))
          .createdCommitId(row.getString("created_commit_id"))

          // Transitive data from joins
          .transitives(ImmutableReferenceTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .updatedAt(row.getOffsetDateTime("updated_at"))
              .build())

          .referenceValue(row.getString("reference_value"))
          .referenceType(row.getString("reference_type"))
          .referenceBody(Optional.ofNullable(reference_body))

          .build();
    }
  }

  class ReferenceInsertMapper implements TenantSql.PropsMapper<Reference> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Reference doc) {
      final ContractOneOfRelations relations = doc.getRelations();
      String inv_plan_id = null;
      String inv_plan_alloc_id = null;
      String coverage_id = null;
      String party_id = null;

      if (relations != null) {
        switch (relations.getRelationType()) {
          case INV_PLAN -> inv_plan_id = relations.getInvPlanId();
          case INV_PLAN_ALLOC -> inv_plan_alloc_id = relations.getInvPlanAllocId();
          case COVERAGE -> coverage_id = relations.getCoverageId();
          case PARTY -> party_id = relations.getPartyId();
        }
      }

      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getId(),
        doc.getContractId(),
        inv_plan_id,
        inv_plan_alloc_id,
        coverage_id,
        party_id,
        doc.getCommitId(),
        doc.getCreatedCommitId(),
        doc.getReferenceValue(),
        doc.getReferenceType(),
        doc.getReferenceBody().orElse(null)
      });
    }
  }

  class ReferenceUpdateMapper implements TenantSql.PropsMapper<Reference> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Reference doc) {
      final ContractOneOfRelations relations = doc.getRelations();
      String inv_plan_id = null;
      String inv_plan_alloc_id = null;
      String coverage_id = null;
      String party_id = null;

      if (relations != null) {
        switch (relations.getRelationType()) {
          case INV_PLAN -> inv_plan_id = relations.getInvPlanId();
          case INV_PLAN_ALLOC -> inv_plan_alloc_id = relations.getInvPlanAllocId();
          case COVERAGE -> coverage_id = relations.getCoverageId();
          case PARTY -> party_id = relations.getPartyId();
        }
      }

      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getContractId(),
        inv_plan_id,
        inv_plan_alloc_id,
        coverage_id,
        party_id,
        doc.getCommitId(),
        doc.getReferenceValue(),
        doc.getReferenceType(),
        doc.getReferenceBody().orElse(null),
        doc.getId()
      });
    }
  }

  class ReferenceDeleteMapper implements TenantSql.PropsMapper<Reference> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Reference reference) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[] {
        reference.getId()
      });
    }
  }
}
