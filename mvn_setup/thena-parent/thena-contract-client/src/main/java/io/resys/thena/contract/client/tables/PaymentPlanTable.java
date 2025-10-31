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
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.contract.client.entities.ImmutablePaymentPlan;
import io.resys.thena.contract.client.entities.ImmutablePaymentPlanTransitives;
import io.resys.thena.contract.client.entities.PaymentPlan;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "payment_plan",
  order = 6,
  ddl = """
    CREATE TABLE IF NOT EXISTS {payment_plan}
    (
      id                              UUID PRIMARY KEY,
      contract_id                     UUID NOT NULL,

      party_id                        UUID,
      commit_id                       UUID NOT NULL,
      created_commit_id               UUID NOT NULL,

      payment_plan_status             VARCHAR(100) NOT NULL,
      payment_plan_frequency          VARCHAR(100) NOT NULL,
      payment_plan_amount             DECIMAL(15,2) NOT NULL,

      payment_plan_start_date         DATE NOT NULL,
      payment_plan_start_date_interval INTERVAL NOT NULL,
      payment_plan_start_date_type    VARCHAR(100) NOT NULL,

      payment_plan_end_date           DATE,
      payment_plan_end_date_interval  INTERVAL,
      payment_plan_end_date_type      VARCHAR(100)
    );

    CREATE INDEX IF NOT EXISTS {payment_plan}_STATUS_INDEX
      ON {payment_plan} (payment_plan_status);
    CREATE INDEX IF NOT EXISTS {payment_plan}_FREQUENCY_INDEX
      ON {payment_plan} (payment_plan_frequency);
    CREATE INDEX IF NOT EXISTS {payment_plan}_CONTRACT_INDEX
      ON {payment_plan} (contract_id);
    CREATE INDEX IF NOT EXISTS {payment_plan}_PARTY_INDEX
      ON {payment_plan} (party_id);
    CREATE INDEX IF NOT EXISTS {payment_plan}_COMMIT_INDEX
      ON {payment_plan} (commit_id);
    CREATE INDEX IF NOT EXISTS {payment_plan}_CREATED_COMMIT_INDEX
      ON {payment_plan} (created_commit_id);
  """,
  constraints = """
    ALTER TABLE {payment_plan} ADD CONSTRAINT fk_payment_plan_contract 
      FOREIGN KEY (contract_id) REFERENCES {contract}(id);
    ALTER TABLE {payment_plan} ADD CONSTRAINT fk_payment_plan_party 
      FOREIGN KEY (party_id) REFERENCES {party}(id);
  """,
  drop = """
    DROP TABLE {payment_plan};
  """
)
public interface PaymentPlanTable {

  @TenantSql.FindAll(
    sql = """
      SELECT p.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {payment_plan} p
      LEFT JOIN {commit} updated_commit ON p.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON p.created_commit_id = created_commit.id
    """,
    rowMapper = PaymentPlanMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT p.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {payment_plan} p
      LEFT JOIN {commit} updated_commit ON p.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON p.created_commit_id = created_commit.id
      WHERE p.contract_id = $1
    """,
    rowMapper = PaymentPlanMapper.class
  )
  SqlTuple findAllByContractId(UUID contractId);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT p.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {payment_plan} p
      LEFT JOIN {commit} updated_commit ON p.commit_id = updated_commit.id
      LEFT JOIN {commit} created_commit ON p.created_commit_id = created_commit.id
      WHERE p.id = $1
    """,
    rowMapper = PaymentPlanMapper.class
  )
  SqlTuple getById(UUID id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {payment_plan}
      (id, contract_id, party_id, commit_id, created_commit_id,
       payment_plan_status, payment_plan_frequency, payment_plan_amount,
       payment_plan_start_date, payment_plan_start_date_interval, payment_plan_start_date_type,
       payment_plan_end_date, payment_plan_end_date_interval, payment_plan_end_date_type)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
    """,
    propsMapper = PaymentPlanInsertMapper.class
  )
  SqlTupleList insertMany(List<PaymentPlan> paymentPlans);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {payment_plan}
       SET contract_id = $1, party_id = $2, commit_id = $3,
           payment_plan_status = $4, payment_plan_frequency = $5, payment_plan_amount = $6,
           payment_plan_start_date = $7, payment_plan_start_date_interval = $8, payment_plan_start_date_type = $9,
           payment_plan_end_date = $10, payment_plan_end_date_interval = $11, payment_plan_end_date_type = $12
       WHERE id = $13
    """,
    propsMapper = PaymentPlanUpdateMapper.class
  )
  SqlTupleList updateMany(List<PaymentPlan> paymentPlans);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {payment_plan} WHERE id = $1",
    propsMapper = PaymentPlanDeleteMapper.class
  )
  SqlTupleList deleteAll(Collection<PaymentPlan> paymentPlans);

  // Mapper classes
  class PaymentPlanMapper implements TenantSql.RowMapper<PaymentPlan> {
    @Override
    public PaymentPlan apply(Row row) {
      final String party_id = row.getString("party_id");
      final LocalDate payment_plan_end_date = row.getLocalDate("payment_plan_end_date");
      final Duration payment_plan_end_date_interval = row.get(Duration.class, "payment_plan_end_date_interval");
      final String payment_plan_end_date_type = row.getString("payment_plan_end_date_type");

      return ImmutablePaymentPlan.builder()
          .id(row.getString("id"))
          .contractId(row.getString("contract_id"))

          .partyId(Optional.ofNullable(party_id))
          .commitId(row.getString("commit_id"))
          .createdCommitId(row.getString("created_commit_id"))

          // Transitive data from joins
          .transitives(ImmutablePaymentPlanTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .updatedAt(row.getOffsetDateTime("updated_at"))
              .build())

          .paymentPlanStatus(row.getString("payment_plan_status"))
          .paymentPlanFrequency(row.getString("payment_plan_frequency"))
          .paymentPlanAmount(row.get(BigDecimal.class, "payment_plan_amount"))

          // Business dates (expanded)
          .paymentPlanStartDate(row.getLocalDate("payment_plan_start_date"))
          .paymentPlanStartDateInterval(row.get(Duration.class, "payment_plan_start_date_interval"))
          .paymentPlanStartDateType(row.getString("payment_plan_start_date_type"))

          .paymentPlanEndDate(Optional.ofNullable(payment_plan_end_date))
          .paymentPlanEndDateInterval(Optional.ofNullable(payment_plan_end_date_interval))
          .paymentPlanEndDateType(Optional.ofNullable(payment_plan_end_date_type))

          .build();
    }
  }

  class PaymentPlanInsertMapper implements TenantSql.PropsMapper<PaymentPlan> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(PaymentPlan doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getId(),
        doc.getContractId(),
        doc.getPartyId().orElse(null),
        doc.getCommitId(),
        doc.getCreatedCommitId(),
        doc.getPaymentPlanStatus(),
        doc.getPaymentPlanFrequency(),
        doc.getPaymentPlanAmount(),
        doc.getPaymentPlanStartDate(),
        doc.getPaymentPlanStartDateInterval(),
        doc.getPaymentPlanStartDateType(),
        doc.getPaymentPlanEndDate().orElse(null),
        doc.getPaymentPlanEndDateInterval().orElse(null),
        doc.getPaymentPlanEndDateType().orElse(null)
      });
    }
  }

  class PaymentPlanUpdateMapper implements TenantSql.PropsMapper<PaymentPlan> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(PaymentPlan doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getContractId(),
        doc.getPartyId().orElse(null),
        doc.getCommitId(),
        doc.getPaymentPlanStatus(),
        doc.getPaymentPlanFrequency(),
        doc.getPaymentPlanAmount(),
        doc.getPaymentPlanStartDate(),
        doc.getPaymentPlanStartDateInterval(),
        doc.getPaymentPlanStartDateType(),
        doc.getPaymentPlanEndDate().orElse(null),
        doc.getPaymentPlanEndDateInterval().orElse(null),
        doc.getPaymentPlanEndDateType().orElse(null),
        doc.getId()
      });
    }
  }

  class PaymentPlanDeleteMapper implements TenantSql.PropsMapper<PaymentPlan> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(PaymentPlan paymentPlan) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[] {
        paymentPlan.getId()
      });
    }
  }
}
