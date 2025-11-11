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

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.contract.client.entities.Command;
import io.resys.thena.contract.client.entities.ImmutableCommand;
import io.resys.thena.contract.client.entities.ImmutableCommandTransitives;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "command",
  order = 7,
  ddl = """
    CREATE TABLE IF NOT EXISTS {command}
    (
      id                              UUID PRIMARY KEY,
      contract_id                     UUID NOT NULL,

      external_id                     VARCHAR(255),
      commit_id                       UUID NOT NULL,
      created_commit_id               UUID NOT NULL,

      command_body                    JSONB NOT NULL,
      command_status                  VARCHAR(100) NOT NULL,
      command_type                    VARCHAR(100) NOT NULL,
      command_target_date             DATE,
      command_description             TEXT,
      command_error                   JSONB
    );

    CREATE INDEX IF NOT EXISTS {command}_STATUS_INDEX
      ON {command} (command_status);
    CREATE INDEX IF NOT EXISTS {command}_TYPE_INDEX
      ON {command} (command_type);
    CREATE INDEX IF NOT EXISTS {command}_TARGET_DATE_INDEX
      ON {command} (command_target_date);
    CREATE INDEX IF NOT EXISTS {command}_CONTRACT_INDEX
      ON {command} (contract_id);
    CREATE INDEX IF NOT EXISTS {command}_EXTERNAL_INDEX
      ON {command} (external_id);
    CREATE INDEX IF NOT EXISTS {command}_COMMIT_INDEX
      ON {command} (commit_id);
    CREATE INDEX IF NOT EXISTS {command}_CREATED_COMMIT_INDEX
      ON {command} (created_commit_id);
  """,
  constraints = """
    ALTER TABLE {command} ADD CONSTRAINT fk_command_contract 
      FOREIGN KEY (contract_id) REFERENCES {contract}(id);
  """,
  drop = """
    DROP TABLE {command};
  """
)
public interface CommandTable {

  @TenantSql.FindAll(
    sql = """
      SELECT c.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {command} c
      LEFT JOIN {commit} updated_commit ON c.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON c.created_commit_id = created_commit.commit_id
    """,
    rowMapper = CommandMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT c.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {command} c
      LEFT JOIN {commit} updated_commit ON c.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON c.created_commit_id = created_commit.commit_id
      WHERE c.contract_id = $1
    """,
    rowMapper = CommandMapper.class
  )
  SqlTuple findAllByContractId(UUID contractId);

  @TenantSql.FindAll(
    sql = """
      SELECT command.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {command} command
      LEFT JOIN {commit} updated_commit ON command.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON command.created_commit_id = created_commit.commit_id
      LEFT JOIN {contract} contract ON command.contract_id = contract.id
    """,
    rowMapper = CommandMapper.class,
    sqlBuilder = ContractTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(ContractTableFilter filter);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT c.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {command} c
      LEFT JOIN {commit} updated_commit ON c.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON c.created_commit_id = created_commit.commit_id
      WHERE c.id = $1
    """,
    rowMapper = CommandMapper.class
  )
  SqlTuple getById(UUID id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {command}
      (id, contract_id, external_id, commit_id, created_commit_id,
       command_body, command_status, command_type, command_target_date, command_description, command_error)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
    """,
    propsMapper = CommandInsertMapper.class
  )
  SqlTupleList insertMany(List<Command> commands);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {command}
       SET contract_id = $1, external_id = $2, commit_id = $3,
           command_body = $4, command_status = $5, command_type = $6,
           command_target_date = $7, command_description = $8, command_error = $9
       WHERE id = $10
    """,
    propsMapper = CommandUpdateMapper.class
  )
  SqlTupleList updateMany(List<Command> commands);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {command} WHERE id = $1",
    propsMapper = CommandDeleteMapper.class
  )
  SqlTupleList deleteAll(Collection<Command> commands);

  // Mapper classes
  class CommandMapper implements TenantSql.RowMapper<Command> {
    @Override
    public Command apply(Row row) {
      final String external_id = row.getString("external_id");
      final LocalDate command_target_date = row.getLocalDate("command_target_date");
      final String command_description = row.getString("command_description");
      final String command_error_json = row.getString("command_error");

      return ImmutableCommand.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .contractId(TableUtils.toStringUUID(row, "contract_id"))

          .externalId(Optional.ofNullable(external_id))
          .commitId(TableUtils.toStringUUID(row, "commit_id"))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))

          // Transitive data from joins
          .transitives(ImmutableCommandTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .updatedAt(row.getOffsetDateTime("updated_at"))
              .build())

          .commandBody(new JsonObject(row.getString("command_body")))
          .commandStatus(row.getString("command_status"))
          .commandType(row.getString("command_type"))
          .commandTargetDate(Optional.ofNullable(command_target_date))
          .commandDescription(Optional.ofNullable(command_description))
          .commandError(Optional.ofNullable(command_error_json).map(JsonObject::new))

          .build();
    }
  }

  class CommandInsertMapper implements TenantSql.PropsMapper<Command> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Command doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getContractId()),
        doc.getExternalId().orElse(null),
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getCreatedCommitId()),
        doc.getCommandBody().toString(),
        doc.getCommandStatus(),
        doc.getCommandType(),
        doc.getCommandTargetDate().orElse(null),
        doc.getCommandDescription().orElse(null),
        doc.getCommandError().map(JsonObject::toString).orElse(null)
      });
    }
  }

  class CommandUpdateMapper implements TenantSql.PropsMapper<Command> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Command doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getContractId()),
        doc.getExternalId().orElse(null),
        TableUtils.toUuid(doc.getCommitId()),
        doc.getCommandBody().toString(),
        doc.getCommandStatus(),
        doc.getCommandType(),
        doc.getCommandTargetDate().orElse(null),
        doc.getCommandDescription().orElse(null),
        doc.getCommandError().map(JsonObject::toString).orElse(null),
        TableUtils.toUuid(doc.getId())
      });
    }
  }

  class CommandDeleteMapper implements TenantSql.PropsMapper<Command> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Command command) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[] {
        TableUtils.toUuid(command.getId())
      });
    }
  }
}