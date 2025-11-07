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
import io.resys.thena.contract.client.entities.ImmutableNote;
import io.resys.thena.contract.client.entities.ImmutableNoteTransitives;
import io.resys.thena.contract.client.entities.Note;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "note",
  order = 5,
  ddl = """
    CREATE TABLE IF NOT EXISTS {note}
    (
      id                              UUID PRIMARY KEY,
      contract_id                     UUID NOT NULL,

      inv_plan_id                     UUID,
      inv_plan_alloc_id               UUID,
      coverage_id                     UUID,
      party_id                        UUID,

      commit_id                       UUID NOT NULL,
      created_commit_id               UUID NOT NULL,

      note_value                      TEXT NOT NULL,
      note_type                       VARCHAR(100) NOT NULL,
      note_body                       JSONB
    );

    CREATE INDEX IF NOT EXISTS {note}_TYPE_INDEX
      ON {note} (note_type);
    CREATE INDEX IF NOT EXISTS {note}_CONTRACT_INDEX
      ON {note} (contract_id);
    CREATE INDEX IF NOT EXISTS {note}_INV_PLAN_INDEX
      ON {note} (inv_plan_id);
    CREATE INDEX IF NOT EXISTS {note}_INV_PLAN_ALLOC_INDEX
      ON {note} (inv_plan_alloc_id);
    CREATE INDEX IF NOT EXISTS {note}_COVERAGE_INDEX
      ON {note} (coverage_id);
    CREATE INDEX IF NOT EXISTS {note}_PARTY_INDEX
      ON {note} (party_id);
    CREATE INDEX IF NOT EXISTS {note}_COMMIT_INDEX
      ON {note} (commit_id);
    CREATE INDEX IF NOT EXISTS {note}_CREATED_COMMIT_INDEX
      ON {note} (created_commit_id);
  """,
  constraints = """
    ALTER TABLE {note} ADD CONSTRAINT fk_note_contract 
      FOREIGN KEY (contract_id) REFERENCES {contract}(id);
    ALTER TABLE {note} ADD CONSTRAINT fk_note_inv_plan 
      FOREIGN KEY (inv_plan_id) REFERENCES {inv_plan}(id);
    ALTER TABLE {note} ADD CONSTRAINT fk_note_inv_plan_alloc 
      FOREIGN KEY (inv_plan_alloc_id) REFERENCES {inv_plan_alloc}(id);
    ALTER TABLE {note} ADD CONSTRAINT fk_note_coverage 
      FOREIGN KEY (coverage_id) REFERENCES {coverage}(id);
    ALTER TABLE {note} ADD CONSTRAINT fk_note_party 
      FOREIGN KEY (party_id) REFERENCES {party}(id);
  """,
  drop = """
    DROP TABLE {note};
  """
)
public interface NoteTable {

  @TenantSql.FindAll(
    sql = """
      SELECT n.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {note} n
      LEFT JOIN {commit} updated_commit ON n.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON n.created_commit_id = created_commit.commit_id
    """,
    rowMapper = NoteMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT n.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {note} n
      LEFT JOIN {commit} updated_commit ON n.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON n.created_commit_id = created_commit.commit_id
      WHERE n.contract_id = $1
    """,
    rowMapper = NoteMapper.class
  )
  SqlTuple findAllByContractId(UUID contractId);

  @TenantSql.FindAll(
    sql = """
      SELECT note.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {note} note
      LEFT JOIN {commit} updated_commit ON note.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON note.created_commit_id = created_commit.commit_id
      LEFT JOIN {contract} contract ON note.contract_id = contract.id
    """,
    rowMapper = NoteMapper.class,
    sqlBuilder = ContractTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(ContractTableFilter filter);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT n.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {note} n
      LEFT JOIN {commit} updated_commit ON n.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON n.created_commit_id = created_commit.commit_id
      WHERE n.id = $1
    """,
    rowMapper = NoteMapper.class
  )
  SqlTuple getById(UUID id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {note}
      (id, contract_id, inv_plan_id, inv_plan_alloc_id, coverage_id, party_id, commit_id, created_commit_id,
       note_value, note_type, note_body)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
    """,
    propsMapper = NoteInsertMapper.class
  )
  SqlTupleList insertMany(List<Note> notes);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {note}
       SET contract_id = $1, inv_plan_id = $2, inv_plan_alloc_id = $3, coverage_id = $4, party_id = $5, commit_id = $6,
           note_value = $7, note_type = $8, note_body = $9
       WHERE id = $10
    """,
    propsMapper = NoteUpdateMapper.class
  )
  SqlTupleList updateMany(List<Note> notes);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {note} WHERE id = $1",
    propsMapper = NoteDeleteMapper.class
  )
  SqlTupleList deleteAll(Collection<Note> notes);

  // Mapper classes
  class NoteMapper implements TenantSql.RowMapper<Note> {
    @Override
    public Note apply(Row row) {
      final var inv_plan_id = TableUtils.toStringUUID(row, "inv_plan_id");
      final var inv_plan_alloc_id = TableUtils.toStringUUID(row, "inv_plan_alloc_id");
      final var coverage_id = TableUtils.toStringUUID(row, "coverage_id");
      final var party_id = TableUtils.toStringUUID(row, "party_id");
      final var note_body = row.getJsonObject("note_body");

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

      return ImmutableNote.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .contractId(TableUtils.toStringUUID(row, "contract_id"))

          .relations(relations)
          .commitId(TableUtils.toStringUUID(row, "commit_id"))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))

          // Transitive data from joins
          .transitives(ImmutableNoteTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .updatedAt(row.getOffsetDateTime("updated_at"))
              .build())

          .noteValue(row.getString("note_value"))
          .noteType(row.getString("note_type"))
          .noteBody(Optional.ofNullable(note_body))

          .build();
    }
  }

  class NoteInsertMapper implements TenantSql.PropsMapper<Note> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Note doc) {
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
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getContractId()),
        inv_plan_id != null ? TableUtils.toUuid(inv_plan_id) : null,
        inv_plan_alloc_id != null ? TableUtils.toUuid(inv_plan_alloc_id) : null,
        coverage_id != null ? TableUtils.toUuid(coverage_id) : null,
        party_id != null ? TableUtils.toUuid(party_id) : null,
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getCreatedCommitId()),
        doc.getNoteValue(),
        doc.getNoteType(),
        doc.getNoteBody().orElse(null)
      });
    }
  }

  class NoteUpdateMapper implements TenantSql.PropsMapper<Note> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Note doc) {
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
        TableUtils.toUuid(doc.getContractId()),
        inv_plan_id != null ? TableUtils.toUuid(inv_plan_id) : null,
        inv_plan_alloc_id != null ? TableUtils.toUuid(inv_plan_alloc_id) : null,
        coverage_id != null ? TableUtils.toUuid(coverage_id) : null,
        party_id != null ? TableUtils.toUuid(party_id) : null,
        TableUtils.toUuid(doc.getCommitId()),
        doc.getNoteValue(),
        doc.getNoteType(),
        doc.getNoteBody().orElse(null),
        TableUtils.toUuid(doc.getId())
      });
    }
  }

  class NoteDeleteMapper implements TenantSql.PropsMapper<Note> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Note note) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[] {
        TableUtils.toUuid(note.getId())
      });
    }
  }
}
