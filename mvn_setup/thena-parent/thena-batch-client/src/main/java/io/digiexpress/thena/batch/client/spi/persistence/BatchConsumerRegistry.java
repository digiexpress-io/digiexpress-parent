package io.digiexpress.thena.batch.client.spi.persistence;

/*-
 * #%L
 * thena-batch-client
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
import java.util.function.Function;

import io.digiexpress.thena.batch.client.api.entities.BatchConsumer;
import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "batch_consumers",
  ddl = """
    CREATE TABLE IF NOT EXISTS {batch_consumers}
    (
      id                           VARCHAR(40) PRIMARY KEY,
      batch_name                   TEXT NOT NULL,
      app_id                       TEXT NOT NULL,
      
      consumer_name                TEXT NOT NULL,
      consumer_qualified_java_name TEXT NOT NULL,
      consumer_comment             TEXT NOT NULL,
      consumer_status              VARCHAR(100) NOT NULL,
      
      consumer_created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
      consumer_created_by          TEXT NOT NULL,
      
      consumer_updated_at          TIMESTAMP WITH TIME ZONE,
      consumer_updated_by          TEXT,
      
      UNIQUE(batch_name, app_id, consumer_name)
    );
    
    CREATE INDEX IF NOT EXISTS {batch_consumers}_APP_INDEX
      ON {batch_consumers} (app_id);
    
    CREATE INDEX IF NOT EXISTS {batch_consumers}_BATCH_NAME_INDEX
      ON {batch_consumers} (batch_name);
    
    CREATE INDEX IF NOT EXISTS {batch_consumers}_NAME_INDEX
      ON {batch_consumers} (consumer_name);
  """,
  constraints = """
    --- constraints for {batch_consumers}
  """,
  drop = """
    DROP TABLE {batch_consumers};
  """
)
public interface BatchConsumerRegistry {
  
  @TenantSql.Query(
    sql = "SELECT * FROM {batch_consumers}",
    rowMapper = BatchConsumerMapper.class
  )
  SqlTuple findAll();
  
  @TenantSql.Query(
    sql = """
      SELECT *
        FROM {batch_consumers}
        WHERE id = $1
    """,
    rowMapper = BatchConsumerMapper.class
  )
  Sql getById(String id);
  
  @TenantSql.Query(
    sql = "SELECT * FROM {batch_consumers} WHERE consumer_status = 'ENABLED'",
    rowMapper = BatchConsumerMapper.class
  )
  SqlTuple findAllEnabled();
  
  @TenantSql.Query(
    sql = """
      SELECT * FROM {batch_consumers}
      WHERE
        consumer_status = 'ENABLED'
        AND app_id = $1
    """,
    rowMapper = BatchConsumerMapper.class
  )
  SqlTuple findAllEnabledByAppId(String appId);
  
  @TenantSql.Insert(
    sql = """
      INSERT INTO {batch_consumers}
      (id, app_id, batch_name,
       consumer_name, consumer_qualified_java_name, consumer_status, consumer_comment,
       consumer_created_at, consumer_created_by,
       consumer_updated_at, consumer_updated_by)
       VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)
    """,
    propsMapper = BatchConsumerInsertMapper.class
  )
  SqlTupleList insertMany(List<BatchConsumer> users);
  
  @TenantSql.Update(
    sql = """
      UPDATE {batch_consumers}
       SET consumer_qualified_java_name = $1, consumer_status = $2, batch_name = $3,
           consumer_updated_at = $4, consumer_updated_by = $5, consumer_comment = $6
       WHERE id = $7
    """,
    propsMapper = BatchConsumerUpdateMapper.class
  )
  SqlTupleList updateMany(List<BatchConsumer> users);
  
  @TenantSql.Query(
    sql = """
      SELECT consumers.*
        FROM {batch_consumers} AS consumers
        WHERE consumers.app_id = $1
        FOR UPDATE
    """,
    rowMapper = BatchConsumerMapper.class
  )
  SqlTuple findAllByAppId(String appId, boolean lockForUpdate);
  
  @TenantSql.Delete(
    sql = "DELETE FROM {batch_consumers} WHERE id = $1",
    propsMapper = BatchConsumerIdMapper.class
  )
  SqlTuple deleteById(String id);
  
  Function<Row, BatchConsumer> defaultMapper();
  
  
  // Mapper classes needed:
  public static class BatchConsumerMapper implements TenantSql.RowMapper<BatchConsumer> {
    @Override
    public BatchConsumer apply(Row row) {
      // Implementation from defaultMapper()
      return null;
    }
  }
  
  public static class BatchConsumerInsertMapper implements TenantSql.PropsMapper<BatchConsumer> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(BatchConsumer doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{ 
        doc.getId(), 
        doc.getAppId(),
        doc.getBatchName(),
        doc.getConsumerName(),
        doc.getQualifiedJavaName(), 
        doc.getStatus().name(), 
        doc.getComment(),
        doc.getCreatedAt(),
        doc.getCreatedBy(),
        doc.getUpdatedAt().orElse(null),
        doc.getUpdatedBy().orElse(null)
      });
    }
  }
  
  public static class BatchConsumerUpdateMapper implements TenantSql.PropsMapper<BatchConsumer> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(BatchConsumer doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{ 
        doc.getQualifiedJavaName(),
        doc.getStatus().name(),
        doc.getBatchName(), 
        doc.getUpdatedAt().orElse(null), 
        doc.getUpdatedBy().orElse(null),
        doc.getComment(),
        doc.getId()
      });
    }
  }
  
  class BatchConsumerIdMapper implements TenantSql.PropsMapper<String> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(String id) {
      return io.vertx.mutiny.sqlclient.Tuple.of(id);
    }
  }
}
