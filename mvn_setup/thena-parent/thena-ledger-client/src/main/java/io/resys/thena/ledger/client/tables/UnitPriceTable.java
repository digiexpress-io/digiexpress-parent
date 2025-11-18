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
import io.resys.thena.ledger.client.entities.ImmutableUnitPrice;
import io.resys.thena.ledger.client.entities.ImmutableUnitPriceTransitives;
import io.resys.thena.ledger.client.entities.UnitPrice;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "unit_price",
  order = 700,
  ddl = """
    CREATE TABLE IF NOT EXISTS {unit_price}
    (
      id UUID PRIMARY KEY,
      external_id VARCHAR(255) NOT NULL,
      
      unit_price_type VARCHAR(100) NOT NULL,
      unit_price_sub_type VARCHAR(100),
      unit_price_description TEXT,
      unit_price_date DATE NOT NULL,
      unit_price_value DECIMAL(15,8) NOT NULL,
      
      created_commit_id UUID NOT NULL
    );

    CREATE INDEX IF NOT EXISTS {unit_price}_EXTERNAL_INDEX
      ON {unit_price} (external_id);
    CREATE INDEX IF NOT EXISTS {unit_price}_TYPE_INDEX
      ON {unit_price} (unit_price_type);
    CREATE INDEX IF NOT EXISTS {unit_price}_DATE_INDEX
      ON {unit_price} (unit_price_date);
  """,
  constraints = """
    ALTER TABLE {unit_price} ADD CONSTRAINT fk_unit_price_created_commit 
      FOREIGN KEY (created_commit_id) REFERENCES {commit}(commit_id);
  """,
  drop = """
    DROP TABLE {unit_price};
  """
)
public interface UnitPriceTable {

  @TenantSql.FindAll(
    sql = """
      SELECT unit_price.*,
             created_commit.created_at as created_at
      FROM {unit_price} unit_price
      LEFT JOIN {commit} created_commit ON unit_price.created_commit_id = created_commit.commit_id
    """,
    rowMapper = UnitPriceMapper.class,
    sqlBuilder = LedgerTableFilter.SQL.class
  )
  SqlTuple findAllByFilter(LedgerTableFilter filter);

  @TenantSql.FindAll(
    sql = """
      SELECT unit_price.*,
             created_commit.created_at as created_at
      FROM {unit_price} unit_price
      LEFT JOIN {commit} created_commit ON unit_price.created_commit_id = created_commit.commit_id
      ORDER BY unit_price_date DESC
    """,
    rowMapper = UnitPriceMapper.class
  )
  Sql findAll();

  @TenantSql.FindAll(
    sql = """
      SELECT unit_price.*,
             created_commit.created_at as created_at
      FROM {unit_price} unit_price
      LEFT JOIN {commit} created_commit ON unit_price.created_commit_id = created_commit.commit_id
      WHERE unit_price_type = $1
      ORDER BY unit_price_date DESC
    """,
    rowMapper = UnitPriceMapper.class
  )
  SqlTuple findAllByType(String type);

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT unit_price.*,
             created_commit.created_at as created_at
      FROM {unit_price} unit_price
      LEFT JOIN {commit} created_commit ON unit_price.created_commit_id = created_commit.commit_id
      WHERE id = $1
    """,
    rowMapper = UnitPriceMapper.class
  )
  SqlTuple getById(String unitPriceId);

  @TenantSql.Find(
    optional = true,
    sql = """
      SELECT unit_price.*,
             created_commit.created_at as created_at
      FROM {unit_price} unit_price
      LEFT JOIN {commit} created_commit ON unit_price.created_commit_id = created_commit.commit_id
      WHERE external_id = $1
    """,
    rowMapper = UnitPriceMapper.class
  )
  SqlTuple findByExternalId(String externalId);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {unit_price}
      (id, external_id, unit_price_type, unit_price_sub_type, 
       unit_price_description, unit_price_date, unit_price_value, created_commit_id)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8)
    """,
    propsMapper = UnitPriceInsertMapper.class
  )
  SqlTupleList insertMany(List<UnitPrice> unitPrices);

  // Mapper classes
  class UnitPriceMapper implements TenantSql.RowMapper<UnitPrice> {
    @Override
    public UnitPrice apply(Row row) {
      return ImmutableUnitPrice.builder()
          .id(TableUtils.toStringUUID(row, "id"))
          .externalId(row.getString("external_id"))
          .unitType(row.getString("unit_price_type"))
          .unitSubType(Optional.ofNullable(row.getString("unit_price_sub_type")))
          .unitDescription(Optional.ofNullable(row.getString("unit_price_description")))
          .unitDate(row.getLocalDate("unit_price_date"))
          .unitValue(row.getBigDecimal("unit_price_value"))
          .createdCommitId(TableUtils.toStringUUID(row, "created_commit_id"))
          .transitives(ImmutableUnitPriceTransitives.builder()
              .createdAt(row.getOffsetDateTime("created_at"))
              .build())
          .build();
    }
  }

  class UnitPriceInsertMapper implements TenantSql.PropsMapper<UnitPrice> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(UnitPrice doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        TableUtils.toUuid(doc.getId()),
        doc.getExternalId(),
        doc.getUnitType(),
        doc.getUnitSubType().orElse(null),
        doc.getUnitDescription().orElse(null),
        doc.getUnitDate(),
        doc.getUnitValue(),
        TableUtils.toUuid(doc.getCreatedCommitId())
      });
    }
  }
}