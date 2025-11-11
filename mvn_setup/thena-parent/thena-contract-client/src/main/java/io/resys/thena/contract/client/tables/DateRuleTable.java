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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.contract.client.entities.DateRule;
import io.resys.thena.contract.client.entities.ImmutableDateRule;
import io.resys.thena.contract.client.entities.ImmutableDateRuleTransitives;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "date_rule",
  order = 99,
  ddl = """
    CREATE TABLE IF NOT EXISTS {date_rule}
    (
      id                      UUID PRIMARY KEY,
      contract_id             UUID NOT NULL,
      
      commit_id               UUID NOT NULL,
      created_commit_id       UUID NOT NULL,
      
      inv_plan_id             UUID,
      coverage_id             UUID,
      party_id                UUID,
      payment_plan_id         UUID,
      
      date_rule_entity        VARCHAR(50) NOT NULL,
      date_rule_entity_field  VARCHAR(100) NOT NULL,
      date_rule_type          VARCHAR(50) NOT NULL,
      date_rule_period        INTERVAL,
      date_rule_name          VARCHAR(100),
      date_rule_description   TEXT
    );


    CREATE INDEX IF NOT EXISTS {date_rule}_CONTRACT_INDEX
      ON {date_rule} (contract_id);
    CREATE INDEX IF NOT EXISTS {date_rule}_ENTITY_INDEX
      ON {date_rule} (date_rule_entity);
    CREATE INDEX IF NOT EXISTS {date_rule}_INV_PLAN_INDEX
      ON {date_rule} (inv_plan_id);
    CREATE INDEX IF NOT EXISTS {date_rule}_COVERAGE_INDEX
      ON {date_rule} (coverage_id);
    CREATE INDEX IF NOT EXISTS {date_rule}_PARTY_INDEX
      ON {date_rule} (party_id);
    CREATE INDEX IF NOT EXISTS {date_rule}_PAYMENT_PLAN_INDEX
      ON {date_rule} (payment_plan_id);
  """,
  constraints = """

    ALTER TABLE {date_rule} ADD CONSTRAINT fk_date_rule_contract
      FOREIGN KEY (contract_id) REFERENCES {contract}(id);
    ALTER TABLE {date_rule} ADD CONSTRAINT fk_date_rule_inv_plan
      FOREIGN KEY (inv_plan_id) REFERENCES {inv_plan}(id);
    ALTER TABLE {date_rule} ADD CONSTRAINT fk_date_rule_coverage
      FOREIGN KEY (coverage_id) REFERENCES {coverage}(id);
    ALTER TABLE {date_rule} ADD CONSTRAINT fk_date_rule_party
      FOREIGN KEY (party_id) REFERENCES {party}(id);
    ALTER TABLE {date_rule} ADD CONSTRAINT fk_date_rule_payment_plan
      FOREIGN KEY (payment_plan_id) REFERENCES {payment_plan}(id);
    
    ALTER TABLE {date_rule} ADD CONSTRAINT check_single_entity CHECK (
      (inv_plan_id IS NOT NULL)::int + 
      (coverage_id IS NOT NULL)::int + 
      (party_id IS NOT NULL)::int + 
      (payment_plan_id IS NOT NULL)::int = 1
    );
    
    ALTER TABLE {date_rule} ADD CONSTRAINT check_entity_type_consistency CHECK (
      (date_rule_entity = 'INV_PLAN' AND inv_plan_id IS NOT NULL) OR
      (date_rule_entity = 'COVERAGE' AND coverage_id IS NOT NULL) OR
      (date_rule_entity = 'PARTY' AND party_id IS NOT NULL) OR
      (date_rule_entity = 'PAYMENT_PLAN' AND payment_plan_id IS NOT NULL)
    );
  """,
  drop = """
    DROP TABLE {date_rule};
  """
)
public interface DateRuleTable {

  @TenantSql.FindAll(
    sql = """
      SELECT dr.*, 
             created_commit.created_at,
             updated_commit.created_at as updated_at
      FROM {date_rule} dr
      LEFT JOIN {commit} updated_commit ON dr.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON dr.created_commit_id = created_commit.commit_id
      ORDER BY created_commit.created_at DESC
    """,
    rowMapper = DateRuleMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT dr.*, 
             created_commit.created_at,
             updated_commit.created_at as updated_at
      FROM {date_rule} dr
      LEFT JOIN {commit} updated_commit ON dr.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON dr.created_commit_id = created_commit.commit_id
      WHERE dr.contract_id = $1
      ORDER BY created_commit.created_at DESC
    """,
    rowMapper = DateRuleMapper.class
  )
  SqlTuple findAllByContractId(UUID contractId);

  @TenantSql.FindAll(
    sql = """
      SELECT dr.*, 
             created_commit.created_at,
             updated_commit.created_at as updated_at
      FROM {date_rule} dr
      LEFT JOIN {commit} updated_commit ON dr.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON dr.created_commit_id = created_commit.commit_id
      WHERE dr.date_rule_entity = $1 AND (
        (dr.date_rule_entity = 'INV_PLAN' AND dr.inv_plan_id = $2) OR
        (dr.date_rule_entity = 'COVERAGE' AND dr.coverage_id = $2) OR
        (dr.date_rule_entity = 'PARTY' AND dr.party_id = $2) OR
        (dr.date_rule_entity = 'PAYMENT_PLAN' AND dr.payment_plan_id = $2)
      )
    """,
    rowMapper = DateRuleMapper.class
  )
  SqlTuple findAllByEntity(String entityType, UUID entityId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT dr.*, 
             created_commit.created_at,
             updated_commit.created_at as updated_at
      FROM {date_rule} dr
      LEFT JOIN {commit} updated_commit ON dr.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON dr.created_commit_id = created_commit.commit_id
      WHERE dr.id = $1
    """,
    rowMapper = DateRuleMapper.class
  )
  SqlTuple getById(UUID id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {date_rule}
      (id, contract_id, commit_id, created_commit_id, inv_plan_id, coverage_id, party_id, payment_plan_id,
       date_rule_entity, date_rule_entity_field, date_rule_type, date_rule_period, date_rule_name, date_rule_description)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
    """,
    propsMapper = DateRuleInsertMapper.class
  )
  SqlTupleList insertMany(List<DateRule> dateRules);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {date_rule}
       SET contract_id = $1, commit_id = $2, inv_plan_id = $3, coverage_id = $4, party_id = $5, payment_plan_id = $6,
           date_rule_entity = $7, date_rule_entity_field = $8, date_rule_type = $9, date_rule_period = $10, 
           date_rule_name = $11, date_rule_description = $12
       WHERE id = $13
    """,
    propsMapper = DateRuleUpdateMapper.class
  )
  SqlTupleList updateMany(List<DateRule> dateRules);

  // Mapper classes
  class DateRuleMapper implements TenantSql.RowMapper<DateRule> {
    @Override
    public DateRule apply(Row row) {
      final String inv_plan_id = TableUtils.toStringUUID(row, "inv_plan_id");
      final String coverage_id = TableUtils.toStringUUID(row, "coverage_id");
      final String party_id = TableUtils.toStringUUID(row, "party_id");
      final String payment_plan_id = TableUtils.toStringUUID(row, "payment_plan_id");
      final var date_rule_period = TableUtils.toDuration(row, "date_rule_period");
      final String date_rule_name = row.getString("date_rule_name");
      final String date_rule_description = row.getString("date_rule_description");

      return ImmutableDateRule.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .contractId(TableUtils.toStringUUID(row, "contract_id"))
          .commitId(TableUtils.toStringUUID(row, "commit_id"))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))
          
          // Transitive data from joins
          .transitives(ImmutableDateRuleTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .updatedAt(row.getOffsetDateTime("updated_at"))
              .build())
          
          .invPlanId(Optional.ofNullable(inv_plan_id))
          .coverageId(Optional.ofNullable(coverage_id))
          .partyId(Optional.ofNullable(party_id))
          .paymentPlanId(Optional.ofNullable(payment_plan_id))
          
          .dateRuleEntity(row.getString("date_rule_entity"))
          .dateRuleEntityField(row.getString("date_rule_entity_field"))
          .dateRuleType(row.getString("date_rule_type"))
          .dateRulePeriod(Optional.ofNullable(date_rule_period))
          .dateRuleName(Optional.ofNullable(date_rule_name))
          .dateRuleDescription(Optional.ofNullable(date_rule_description))
          .build();
    }
  }

  class DateRuleInsertMapper implements TenantSql.PropsMapper<DateRule> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(DateRule doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getContractId()),
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getCreatedCommitId()),
        doc.getInvPlanId().map(TableUtils::toUuid).orElse(null),
        doc.getCoverageId().map(TableUtils::toUuid).orElse(null),
        doc.getPartyId().map(TableUtils::toUuid).orElse(null),
        doc.getPaymentPlanId().map(TableUtils::toUuid).orElse(null),
        doc.getDateRuleEntity(),
        doc.getDateRuleEntityField(),
        doc.getDateRuleType(),
        TableUtils.toIntervalOptional(doc.getDateRulePeriod()),
        doc.getDateRuleName().orElse(null),
        doc.getDateRuleDescription().orElse(null)
      });
    }
  }

  class DateRuleUpdateMapper implements TenantSql.PropsMapper<DateRule> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(DateRule doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getContractId()),
        TableUtils.toUuid(doc.getCommitId()),
        doc.getInvPlanId().map(TableUtils::toUuid).orElse(null),
        doc.getCoverageId().map(TableUtils::toUuid).orElse(null),
        doc.getPartyId().map(TableUtils::toUuid).orElse(null),
        doc.getPaymentPlanId().map(TableUtils::toUuid).orElse(null),
        doc.getDateRuleEntity(),
        doc.getDateRuleEntityField(),
        doc.getDateRuleType(),
        TableUtils.toIntervalOptional(doc.getDateRulePeriod()),
        doc.getDateRuleName().orElse(null),
        doc.getDateRuleDescription().orElse(null),
        TableUtils.toUuid(doc.getId())
      });
    }
  }
}