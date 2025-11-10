package io.resys.thena.contract.client.tables;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.contract.client.entities.ContractDateRelativity;
import io.resys.thena.contract.client.entities.ImmutableContractDateRelativity;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "contract_date_relativity",
  order = 99,
  ddl = """
    CREATE TABLE IF NOT EXISTS {contract_date_relativity}
    (
      id                    UUID PRIMARY KEY,
      contract_id          UUID NOT NULL,
      
      
      inv_plan_id          UUID,
      coverage_id          UUID,
      party_id             UUID,
      payment_plan_id      UUID,
      
      entity_type          VARCHAR(50) NOT NULL,
      field_name           VARCHAR(100) NOT NULL,
      

      relative_to_type     VARCHAR(50) NOT NULL,
      offset_interval      INTERVAL,
      calculation_rule     VARCHAR(100),
      
      description          TEXT,
      created_at           TIMESTAMP WITH TIME ZONE DEFAULT NOW()
    );


    CREATE INDEX IF NOT EXISTS {contract_date_relativity}_CONTRACT_INDEX
      ON {contract_date_relativity} (contract_id);
    CREATE INDEX IF NOT EXISTS {contract_date_relativity}_ENTITY_TYPE_INDEX
      ON {contract_date_relativity} (entity_type);
    CREATE INDEX IF NOT EXISTS {contract_date_relativity}_INV_PLAN_INDEX
      ON {contract_date_relativity} (inv_plan_id);
    CREATE INDEX IF NOT EXISTS {contract_date_relativity}_COVERAGE_INDEX
      ON {contract_date_relativity} (coverage_id);
    CREATE INDEX IF NOT EXISTS {contract_date_relativity}_PARTY_INDEX
      ON {contract_date_relativity} (party_id);
    CREATE INDEX IF NOT EXISTS {contract_date_relativity}_PAYMENT_PLAN_INDEX
      ON {contract_date_relativity} (payment_plan_id);
  """,
  constraints = """

    ALTER TABLE {contract_date_relativity} ADD CONSTRAINT fk_date_relativity_contract
      FOREIGN KEY (contract_id) REFERENCES {contract}(id);
    ALTER TABLE {contract_date_relativity} ADD CONSTRAINT fk_date_relativity_inv_plan
      FOREIGN KEY (inv_plan_id) REFERENCES {inv_plan}(id);
    ALTER TABLE {contract_date_relativity} ADD CONSTRAINT fk_date_relativity_coverage
      FOREIGN KEY (coverage_id) REFERENCES {coverage}(id);
    ALTER TABLE {contract_date_relativity} ADD CONSTRAINT fk_date_relativity_party
      FOREIGN KEY (party_id) REFERENCES {party}(id);
    ALTER TABLE {contract_date_relativity} ADD CONSTRAINT fk_date_relativity_payment_plan
      FOREIGN KEY (payment_plan_id) REFERENCES {payment_plan}(id);
    
    ALTER TABLE {contract_date_relativity} ADD CONSTRAINT check_single_entity CHECK (
      (inv_plan_id IS NOT NULL)::int + 
      (coverage_id IS NOT NULL)::int + 
      (party_id IS NOT NULL)::int + 
      (payment_plan_id IS NOT NULL)::int = 1
    );
    
    ALTER TABLE {contract_date_relativity} ADD CONSTRAINT check_entity_type_consistency CHECK (
      (entity_type = 'INV_PLAN' AND inv_plan_id IS NOT NULL) OR
      (entity_type = 'COVERAGE' AND coverage_id IS NOT NULL) OR
      (entity_type = 'PARTY' AND party_id IS NOT NULL) OR
      (entity_type = 'PAYMENT_PLAN' AND payment_plan_id IS NOT NULL)
    );
  """,
  drop = """
    DROP TABLE {contract_date_relativity};
  """
)
public interface ContractDateRelativityTable {

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {contract_date_relativity}
      ORDER BY created_at DESC
    """,
    rowMapper = ContractDateRelativityMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {contract_date_relativity}
      WHERE contract_id = $1
      ORDER BY created_at DESC
    """,
    rowMapper = ContractDateRelativityMapper.class
  )
  SqlTuple findAllByContractId(UUID contractId);

  @TenantSql.FindAll(
    sql = """
      SELECT * FROM {contract_date_relativity}
      WHERE entity_type = $1 AND (
        (entity_type = 'INV_PLAN' AND inv_plan_id = $2) OR
        (entity_type = 'COVERAGE' AND coverage_id = $2) OR
        (entity_type = 'PARTY' AND party_id = $2) OR
        (entity_type = 'PAYMENT_PLAN' AND payment_plan_id = $2)
      )
    """,
    rowMapper = ContractDateRelativityMapper.class
  )
  SqlTuple findAllByEntity(String entityType, UUID entityId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT * FROM {contract_date_relativity}
      WHERE id = $1
    """,
    rowMapper = ContractDateRelativityMapper.class
  )
  SqlTuple getById(UUID id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {contract_date_relativity}
      (id, contract_id, inv_plan_id, coverage_id, party_id, payment_plan_id,
       entity_type, field_name, relative_to_type, offset_interval, calculation_rule, description)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)
    """,
    propsMapper = ContractDateRelativityInsertMapper.class
  )
  SqlTupleList insertMany(List<ContractDateRelativity> relativityRules);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {contract_date_relativity}
       SET contract_id = $1, inv_plan_id = $2, coverage_id = $3, party_id = $4, payment_plan_id = $5,
           entity_type = $6, field_name = $7, relative_to_type = $8, offset_interval = $9, 
           calculation_rule = $10, description = $11
       WHERE id = $12
    """,
    propsMapper = ContractDateRelativityUpdateMapper.class
  )
  SqlTupleList updateMany(List<ContractDateRelativity> relativityRules);

  // Mapper classes
  class ContractDateRelativityMapper implements TenantSql.RowMapper<ContractDateRelativity> {
    @Override
    public ContractDateRelativity apply(Row row) {
      final String inv_plan_id = TableUtils.toStringUUID(row, "inv_plan_id");
      final String coverage_id = TableUtils.toStringUUID(row, "coverage_id");
      final String party_id = TableUtils.toStringUUID(row, "party_id");
      final String payment_plan_id = TableUtils.toStringUUID(row, "payment_plan_id");
      final var offset_interval = TableUtils.toDuration(row, "offset_interval");
      final String calculation_rule = row.getString("calculation_rule");
      final String description = row.getString("description");

      return ImmutableContractDateRelativity.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .contractId(TableUtils.toStringUUID(row, "contract_id"))
          
          .invPlanId(Optional.ofNullable(inv_plan_id))
          .coverageId(Optional.ofNullable(coverage_id))
          .partyId(Optional.ofNullable(party_id))
          .paymentPlanId(Optional.ofNullable(payment_plan_id))
          
          .entityType(row.getString("entity_type"))
          .fieldName(row.getString("field_name"))
          
          .relativeToType(row.getString("relative_to_type"))
          .offsetInterval(Optional.ofNullable(offset_interval))
          .calculationRule(Optional.ofNullable(calculation_rule))
          .description(Optional.ofNullable(description))
          .createdAt(row.getOffsetDateTime("created_at"))
          .build();
    }
  }

  class ContractDateRelativityInsertMapper implements TenantSql.PropsMapper<ContractDateRelativity> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(ContractDateRelativity doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getContractId()),
        doc.getInvPlanId().map(TableUtils::toUuid).orElse(null),
        doc.getCoverageId().map(TableUtils::toUuid).orElse(null),
        doc.getPartyId().map(TableUtils::toUuid).orElse(null),
        doc.getPaymentPlanId().map(TableUtils::toUuid).orElse(null),
        doc.getEntityType(),
        doc.getFieldName(),
        doc.getRelativeToType(),
        TableUtils.toIntervalOptional(doc.getOffsetInterval()),
        doc.getCalculationRule().orElse(null),
        doc.getDescription().orElse(null)
      });
    }
  }

  class ContractDateRelativityUpdateMapper implements TenantSql.PropsMapper<ContractDateRelativity> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(ContractDateRelativity doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getContractId()),
        doc.getInvPlanId().map(TableUtils::toUuid).orElse(null),
        doc.getCoverageId().map(TableUtils::toUuid).orElse(null),
        doc.getPartyId().map(TableUtils::toUuid).orElse(null),
        doc.getPaymentPlanId().map(TableUtils::toUuid).orElse(null),
        doc.getEntityType(),
        doc.getFieldName(),
        doc.getRelativeToType(),
        TableUtils.toIntervalOptional(doc.getOffsetInterval()),
        doc.getCalculationRule().orElse(null),
        doc.getDescription().orElse(null),
        TableUtils.toUuid(doc.getId())
      });
    }
  }
}