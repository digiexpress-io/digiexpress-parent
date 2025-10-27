package io.resys.thena.contract.client.tables;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.contract.client.entities.ImmutableInvPlan;
import io.resys.thena.contract.client.entities.ImmutableInvPlanTransitives;
import io.resys.thena.contract.client.entities.InvPlan;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "inv_plan",
  order = 7,
  ddl = """
    CREATE TABLE IF NOT EXISTS {inv_plan}
    (
      id                              UUID PRIMARY KEY,
      contract_id                     UUID NOT NULL,

      external_id                     VARCHAR(255) NOT NULL,
      commit_id                       UUID NOT NULL,
      created_commit_id               UUID NOT NULL,

      inv_plan_status                 VARCHAR(100) NOT NULL,
      inv_plan_code                   VARCHAR(100) NOT NULL,
      inv_plan_name                   VARCHAR(255) NOT NULL,

      inv_plan_start_date             DATE NOT NULL,
      inv_plan_start_date_interval    INTERVAL NOT NULL,
      inv_plan_start_date_type        VARCHAR(100) NOT NULL,

      inv_plan_end_date               DATE,
      inv_plan_end_date_interval      INTERVAL,
      inv_plan_end_date_type          VARCHAR(100)
    );

    CREATE INDEX IF NOT EXISTS {inv_plan}_STATUS_INDEX
      ON {inv_plan} (inv_plan_status);
    CREATE INDEX IF NOT EXISTS {inv_plan}_CODE_INDEX
      ON {inv_plan} (inv_plan_code);
    CREATE INDEX IF NOT EXISTS {inv_plan}_CONTRACT_INDEX
      ON {inv_plan} (contract_id);
    CREATE INDEX IF NOT EXISTS {inv_plan}_COMMIT_INDEX
      ON {inv_plan} (commit_id);
    CREATE INDEX IF NOT EXISTS {inv_plan}_CREATED_COMMIT_INDEX
      ON {inv_plan} (created_commit_id);
  """,
  constraints = """
    ALTER TABLE {inv_plan} ADD CONSTRAINT fk_inv_plan_contract 
      FOREIGN KEY (contract_id) REFERENCES {contract}(id);
  """,
  drop = """
    DROP TABLE {inv_plan};
  """
)
public interface InvPlanTable {

  @TenantSql.FindAll(
    sql = """
      SELECT i.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {inv_plan} i
      LEFT JOIN {commit} updated_commit ON i.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON i.created_commit_id = created_commit.id
    """,
    rowMapper = InvPlanMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT i.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {inv_plan} i
      LEFT JOIN {commit} updated_commit ON i.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON i.created_commit_id = created_commit.id
      WHERE i.contract_id = $1
    """,
    rowMapper = InvPlanMapper.class
  )
  SqlTuple findAllByContractId(UUID contractId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT i.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {inv_plan} i
      LEFT JOIN {commit} updated_commit ON i.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON i.created_commit_id = created_commit.id
      WHERE i.id = $1
    """,
    rowMapper = InvPlanMapper.class
  )
  SqlTuple getById(UUID id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {inv_plan}
      (id, contract_id, external_id, commit_id, created_commit_id,
       inv_plan_status, inv_plan_code, inv_plan_name,
       inv_plan_start_date, inv_plan_start_date_interval, inv_plan_start_date_type,
       inv_plan_end_date, inv_plan_end_date_interval, inv_plan_end_date_type)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
    """,
    propsMapper = InvPlanInsertMapper.class
  )
  SqlTupleList insertMany(List<InvPlan> invPlans);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {inv_plan}
       SET contract_id = $1, external_id = $2, commit_id = $3,
           inv_plan_status = $4, inv_plan_code = $5, inv_plan_name = $6,
           inv_plan_start_date = $7, inv_plan_start_date_interval = $8, inv_plan_start_date_type = $9,
           inv_plan_end_date = $10, inv_plan_end_date_interval = $11, inv_plan_end_date_type = $12
       WHERE id = $13
    """,
    propsMapper = InvPlanUpdateMapper.class
  )
  SqlTupleList updateMany(List<InvPlan> invPlans);

  // Mapper classes
  class InvPlanMapper implements TenantSql.RowMapper<InvPlan> {
    @Override
    public InvPlan apply(Row row) {
      final LocalDate inv_plan_end_date = row.getLocalDate("inv_plan_end_date");
      final Duration inv_plan_end_date_interval = row.get(Duration.class, "inv_plan_end_date_interval");
      final String inv_plan_end_date_type = row.getString("inv_plan_end_date_type");

      return ImmutableInvPlan.builder()
          .id(UUID.fromString(row.getString("id")))
          .contractId(UUID.fromString(row.getString("contract_id")))

          .externalId(row.getString("external_id"))
          .commitId(UUID.fromString(row.getString("commit_id")))
          .createdCommitId(UUID.fromString(row.getString("created_commit_id")))

          // Transitive data from joins
          .transitives(ImmutableInvPlanTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .updatedAt(row.getOffsetDateTime("updated_at"))
              .build())

          .invPlanStatus(row.getString("inv_plan_status"))
          .invPlanCode(row.getString("inv_plan_code"))
          .invPlanName(row.getString("inv_plan_name"))

          // Business dates (expanded)
          .invPlanStartDate(row.getLocalDate("inv_plan_start_date"))
          .invPlanStartDateInterval(row.get(Duration.class, "inv_plan_start_date_interval"))
          .invPlanStartDateType(row.getString("inv_plan_start_date_type"))

          .invPlanEndDate(Optional.ofNullable(inv_plan_end_date))
          .invPlanEndDateInterval(Optional.ofNullable(inv_plan_end_date_interval))
          .invPlanEndDateType(Optional.ofNullable(inv_plan_end_date_type))

          .build();
    }
  }

  class InvPlanInsertMapper implements TenantSql.PropsMapper<InvPlan> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(InvPlan doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getId(),
        doc.getContractId(),
        doc.getExternalId(),
        doc.getCommitId(),
        doc.getCreatedCommitId(),
        doc.getInvPlanStatus(),
        doc.getInvPlanCode(),
        doc.getInvPlanName(),
        doc.getInvPlanStartDate(),
        doc.getInvPlanStartDateInterval(),
        doc.getInvPlanStartDateType(),
        doc.getInvPlanEndDate().orElse(null),
        doc.getInvPlanEndDateInterval().orElse(null),
        doc.getInvPlanEndDateType().orElse(null)
      });
    }
  }

  class InvPlanUpdateMapper implements TenantSql.PropsMapper<InvPlan> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(InvPlan doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getContractId(),
        doc.getExternalId(),
        doc.getCommitId(),
        doc.getInvPlanStatus(),
        doc.getInvPlanCode(),
        doc.getInvPlanName(),
        doc.getInvPlanStartDate(),
        doc.getInvPlanStartDateInterval(),
        doc.getInvPlanStartDateType(),
        doc.getInvPlanEndDate().orElse(null),
        doc.getInvPlanEndDateInterval().orElse(null),
        doc.getInvPlanEndDateType().orElse(null),
        doc.getId()
      });
    }
  }
}