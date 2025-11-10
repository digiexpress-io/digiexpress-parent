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
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.contract.client.entities.Coverage;
import io.resys.thena.contract.client.entities.ImmutableCoverage;
import io.resys.thena.contract.client.entities.ImmutableCoverageTransitives;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "coverage",
  order = 2,
  ddl = """
    CREATE TABLE IF NOT EXISTS {coverage}
    (
      id                              UUID PRIMARY KEY,
      contract_id                     UUID NOT NULL,
      insured_id                      UUID NOT NULL,

      external_id                     VARCHAR(255) NOT NULL,
      commit_id                       UUID NOT NULL,
      created_commit_id               UUID NOT NULL,

      coverage_type                   VARCHAR(100) NOT NULL,
      coverage_code                   VARCHAR(100) NOT NULL,
      coverage_sum_insured            DECIMAL(15,2),
      coverage_rate                   DECIMAL(10,6),
      coverage_rate_type              VARCHAR(100),
      coverage_status                 VARCHAR(100) NOT NULL,
      coverage_effective_from         DATE NOT NULL,
      coverage_effective_to           DATE,

      coverage_term_start_date        DATE NOT NULL,
      coverage_term_start_date_interval INTERVAL NOT NULL,
      coverage_term_start_date_type   VARCHAR(100) NOT NULL,

      coverage_term_end_date          DATE,
      coverage_term_end_date_interval INTERVAL,
      coverage_term_end_date_type     VARCHAR(100)
    );

    CREATE INDEX IF NOT EXISTS {coverage}_TYPE_INDEX
      ON {coverage} (coverage_type);
    CREATE INDEX IF NOT EXISTS {coverage}_STATUS_INDEX
      ON {coverage} (coverage_status);
    CREATE INDEX IF NOT EXISTS {coverage}_CONTRACT_INDEX
      ON {coverage} (contract_id);
    CREATE INDEX IF NOT EXISTS {coverage}_INSURED_INDEX
      ON {coverage} (insured_id);
    CREATE INDEX IF NOT EXISTS {coverage}_COMMIT_INDEX
      ON {coverage} (commit_id);
    CREATE INDEX IF NOT EXISTS {coverage}_CREATED_COMMIT_INDEX
      ON {coverage} (created_commit_id);
  """,
  constraints = """
    ALTER TABLE {coverage} ADD CONSTRAINT fk_coverage_contract 
      FOREIGN KEY (contract_id) REFERENCES {contract}(id);
    ALTER TABLE {coverage} ADD CONSTRAINT fk_coverage_insured 
      FOREIGN KEY (insured_id) REFERENCES {party}(id);
  """,
  drop = """
    DROP TABLE {coverage};
  """
)
public interface CoverageTable {

  @TenantSql.FindAll(
    sql = """
      SELECT c.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {coverage} c
      LEFT JOIN {commit} updated_commit ON c.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON c.created_commit_id = created_commit.commit_id
    """,
    rowMapper = CoverageMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT c.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {coverage} c
      LEFT JOIN {commit} updated_commit ON c.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON c.created_commit_id = created_commit.commit_id
      WHERE c.contract_id = $1
    """,
    rowMapper = CoverageMapper.class
  )
  SqlTuple findAllByContractId(UUID contractId);

  @TenantSql.FindAll(
    sql = """
      SELECT coverage.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {coverage} coverage
      LEFT JOIN {commit} updated_commit ON coverage.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON coverage.created_commit_id = created_commit.commit_id
      LEFT JOIN {contract} contract ON coverage.contract_id = contract.id
    """,
    rowMapper = CoverageMapper.class,
    sqlBuilder = ContractTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(ContractTableFilter filter);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT c.*, 
             updated_commit.created_at as updated_at,
             created_commit.created_at as created_at
      FROM {coverage} c
      LEFT JOIN {commit} updated_commit ON c.commit_id = updated_commit.commit_id
      LEFT JOIN {commit} created_commit ON c.created_commit_id = created_commit.commit_id
      WHERE c.id = $1
    """,
    rowMapper = CoverageMapper.class
  )
  SqlTuple getById(UUID id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {coverage}
      (id, contract_id, insured_id, external_id, commit_id, created_commit_id,
       coverage_type, coverage_code, coverage_sum_insured, coverage_rate, coverage_rate_type,
       coverage_status, coverage_effective_from, coverage_effective_to,
       coverage_term_start_date, coverage_term_start_date_interval, coverage_term_start_date_type,
       coverage_term_end_date, coverage_term_end_date_interval, coverage_term_end_date_type)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18, $19, $20)
    """,
    propsMapper = CoverageInsertMapper.class
  )
  SqlTupleList insertMany(List<Coverage> coverages);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {coverage}
       SET contract_id = $1, insured_id = $2, external_id = $3, commit_id = $4,
           coverage_type = $5, coverage_code = $6, coverage_sum_insured = $7, coverage_rate = $8, coverage_rate_type = $9,
           coverage_status = $10, coverage_effective_from = $11, coverage_effective_to = $12,
           coverage_term_start_date = $13, coverage_term_start_date_interval = $14, coverage_term_start_date_type = $15,
           coverage_term_end_date = $16, coverage_term_end_date_interval = $17, coverage_term_end_date_type = $18
       WHERE id = $19
    """,
    propsMapper = CoverageUpdateMapper.class
  )
  SqlTupleList updateMany(List<Coverage> coverages);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {coverage} WHERE id = $1",
    propsMapper = CoverageDeleteMapper.class
  )
  SqlTupleList deleteAll(Collection<Coverage> coverages);

  // Mapper classes
  class CoverageMapper implements TenantSql.RowMapper<Coverage> {
    @Override
    public Coverage apply(Row row) {
      final BigDecimal coverage_sum_insured = row.getBigDecimal("coverage_sum_insured");
      final BigDecimal coverage_rate = row.getBigDecimal("coverage_rate");
      final String coverage_rate_type = row.getString("coverage_rate_type");
      final LocalDate coverage_effective_to = row.getLocalDate("coverage_effective_to");
      final LocalDate coverage_term_end_date = row.getLocalDate("coverage_term_end_date");
      final var coverage_term_end_date_interval = TableUtils.toDuration(row, "coverage_term_end_date_interval");
      final String coverage_term_end_date_type = row.getString("coverage_term_end_date_type");

      return ImmutableCoverage.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .contractId(TableUtils.toStringUUID(row, "contract_id"))
          .insuredId(TableUtils.toStringUUID(row, "insured_id"))

          .externalId(row.getString("external_id"))
          .commitId(TableUtils.toStringUUID(row, "commit_id"))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))

          // Transitive data from joins
          .transitives(ImmutableCoverageTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .updatedAt(row.getOffsetDateTime("updated_at"))
              .build())

          .coverageType(row.getString("coverage_type"))
          .coverageCode(row.getString("coverage_code"))
          .coverageSumInsured(Optional.ofNullable(coverage_sum_insured))
          .coverageRate(Optional.ofNullable(coverage_rate))
          .coverageRateType(Optional.ofNullable(coverage_rate_type))
          .coverageStatus(row.getString("coverage_status"))
          .coverageEffectiveFrom(row.getLocalDate("coverage_effective_from"))
          .coverageEffectiveTo(Optional.ofNullable(coverage_effective_to))

          // Business dates (expanded)
          .coverageTermStartDate(row.getLocalDate("coverage_term_start_date"))
          .coverageTermStartDateInterval(TableUtils.toDuration(row, "coverage_term_start_date_interval"))
          .coverageTermStartDateType(row.getString("coverage_term_start_date_type"))

          .coverageTermEndDate(Optional.ofNullable(coverage_term_end_date))
          .coverageTermEndDateInterval(Optional.ofNullable(coverage_term_end_date_interval))
          .coverageTermEndDateType(Optional.ofNullable(coverage_term_end_date_type))

          .build();
    }
  }

  class CoverageInsertMapper implements TenantSql.PropsMapper<Coverage> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Coverage doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        TableUtils.toUuid(doc.getContractId()),
        TableUtils.toUuid(doc.getInsuredId()),
        doc.getExternalId(),
        TableUtils.toUuid(doc.getCommitId()),
        TableUtils.toUuid(doc.getCreatedCommitId()),
        doc.getCoverageType(),
        doc.getCoverageCode(),
        doc.getCoverageSumInsured().orElse(null),
        doc.getCoverageRate().orElse(null),
        doc.getCoverageRateType().orElse(null),
        doc.getCoverageStatus(),
        doc.getCoverageEffectiveFrom(),
        doc.getCoverageEffectiveTo().orElse(null),
        doc.getCoverageTermStartDate(),
        TableUtils.toInterval(doc.getCoverageTermStartDateInterval()),
        doc.getCoverageTermStartDateType(),
        doc.getCoverageTermEndDate().orElse(null),
        TableUtils.toIntervalOptional(doc.getCoverageTermEndDateInterval()),
        doc.getCoverageTermEndDateType().orElse(null)
      });
    }
  }

  class CoverageUpdateMapper implements TenantSql.PropsMapper<Coverage> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Coverage doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getContractId()),
        TableUtils.toUuid(doc.getInsuredId()),
        doc.getExternalId(),
        TableUtils.toUuid(doc.getCommitId()),
        doc.getCoverageType(),
        doc.getCoverageCode(),
        doc.getCoverageSumInsured().orElse(null),
        doc.getCoverageRate().orElse(null),
        doc.getCoverageRateType().orElse(null),
        doc.getCoverageStatus(),
        doc.getCoverageEffectiveFrom(),
        doc.getCoverageEffectiveTo().orElse(null),
        doc.getCoverageTermStartDate(),
        TableUtils.toInterval(doc.getCoverageTermStartDateInterval()),
        doc.getCoverageTermStartDateType(),
        doc.getCoverageTermEndDate().orElse(null),
        TableUtils.toIntervalOptional(doc.getCoverageTermEndDateInterval()),
        doc.getCoverageTermEndDateType().orElse(null),
        TableUtils.toUuid(doc.getId())
      });
    }
  }

  class CoverageDeleteMapper implements TenantSql.PropsMapper<Coverage> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Coverage coverage) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[] {
        TableUtils.toUuid(coverage.getId())
      });
    }
  }
}
