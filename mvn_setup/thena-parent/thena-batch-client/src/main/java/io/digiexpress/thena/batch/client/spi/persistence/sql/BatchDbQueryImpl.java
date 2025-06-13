package io.digiexpress.thena.batch.client.spi.persistence.sql;

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
import java.util.Optional;

import io.digiexpress.thena.batch.client.api.BatchLogConstants;
import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchConsumer;
import io.digiexpress.thena.batch.client.api.entities.BatchContainers.BatchTenantContainer;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeMetric;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStepRow;
import io.digiexpress.thena.batch.client.api.persistence.BatchDbQuery;
import io.digiexpress.thena.batch.client.spi.persistence.BatchTenantRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = BatchLogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class BatchDbQueryImpl implements BatchDbQuery {

  private final ThenaSqlDataSource dataSource;
  private final BatchTenantRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  
  public BatchDbQueryImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = new BatchTenantRegistryImpl(dataSource.getRegistry());
    this.errorHandler = dataSource.getErrorHandler();
  }
  
  @Override
  public Uni<BatchTenantContainer> findAll() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public BatchDbBatchQuery queryBatches() {
    return new BatchDbBatchQuery() {
      
      @Override
      public Multi<Batch> findAll() {
        final var sql = registry.getBatches().findAll();
        
        if(log.isDebugEnabled()) {
          log.debug("BatchDbQueryImpl.queryBatches.findAll query, with props: {} \r\n{}", 
              "", 
              sql.getValue());
        }
        
        return dataSource.getClient().preparedQuery(sql.getValue())
            .mapping(registry.getBatches().defaultMapper())
            .execute()
            .onItem()
            .transformToMulti((RowSet<Batch> rowset) -> Multi.createFrom().iterable(rowset))
            .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'BATCHES'-s!", sql, e)));
      }
      
      
      @Override
      public Uni<List<Batch>> findAllByAppId(String appId, boolean lockForUpdate) {
        final var sql = registry.getBatches().findAllByAppId(appId, lockForUpdate);
        
        if(log.isDebugEnabled()) {
          log.debug("BatchDbQueryImpl.queryBatches.findAllByAppId query, with props: {} \r\n{}", 
              sql.getProps().deepToString(), 
              sql.getValue());
        }
        
        return dataSource.getClient().preparedQuery(sql.getValue())
            .mapping(registry.getBatches().defaultMapper())
            .execute(sql.getProps())
            .onItem()
            .transformToUni((RowSet<Batch> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
            .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'BATCHES'-s by 'app_id' for update!", sql, e)));
      }

      @Override
      public Uni<Optional<Batch>> findOneByAppIdAndName(String appId, String batchName) {
        final var sql = registry.getBatches().findOneByAppIdAndName(appId, batchName);
        
        if(log.isDebugEnabled()) {
          log.debug("BatchDbQueryImpl.queryBatches.findOneByAppIdAndName query, with props: {} \r\n{}", 
              sql.getProps().deepToString(), 
              sql.getValue());
        }
        
        return dataSource.getClient().preparedQuery(sql.getValue())
            .mapping(registry.getBatches().defaultMapper())
            .execute(sql.getProps())
            .onItem()
            .transform((RowSet<Batch> rowset) -> {
              final var iterator = rowset.iterator();
              if(iterator.hasNext()) {
                return Optional.of(iterator.next());
              }
              return Optional.<Batch>empty();
            })
            .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'BATCHES'-s by 'app_id' and 'batch_name'!", sql, e)));
      }
    };
  }

  @Override
  public BatchDbBatchConsumerQuery queryBatchConsumers() {
    return new BatchDbBatchConsumerQuery() {
      @Override
      public Uni<List<BatchConsumer>> findAllByAppId(String appId, boolean lockForUpdate) {
        final var sql = registry.getBatchConsumers().findAllByAppId(appId, lockForUpdate);
        
        if(log.isDebugEnabled()) {
          log.debug("BatchDbQueryImpl.queryBatchConsumers.findAllByAppId query, with props: {} \r\n{}", 
              sql.getProps().deepToString(), 
              sql.getValue());
        }
        
        return dataSource.getClient().preparedQuery(sql.getValue())
            .mapping(registry.getBatchConsumers().defaultMapper())
            .execute(sql.getProps())
            .onItem()
            .transformToUni((RowSet<BatchConsumer> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
            .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'BATCH_CONSUMERS'-s by 'app_id' for update!", sql, e)));

      }
    };
  }



  @Override
  public BatchDbInstanceQuery queryInstances() {
    return new BatchDbInstanceQuery() {
      
      @Override
      public Uni<Long> nextSequence() {
        final var sql = registry.getRuntimeInstances().getNextRefSequence();
        if(log.isDebugEnabled()) {
          log.debug("BatchDbQueryImpl.queryInstances.nextSequence query, with props: {} \r\n{}", 
              "",
              sql.getValue());
        }
        return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(row -> row.getLong(0))
          .execute()
          .onItem()
          .transform(rowset -> {
            final var it = rowset.iterator();
            if(it.hasNext()) {
              return it.next();
            }
            return null;
          })
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find next 'RUNTIME_INSTANCE_REF' sequence!")));
      }

      @Override
      public Uni<RuntimeInstance> getById(String id, boolean lockForUpdate) {
        final var sql = registry.getRuntimeInstances().getById(id, lockForUpdate);
        if(log.isDebugEnabled()) {
          log.debug("BatchDbQueryImpl.queryInstances.getById query, with props: {} \r\n{}", 
              sql.getProps().deepToString(),
              sql.getValue());
        }
        return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.getRuntimeInstances().defaultMapper())
          .execute(sql.getProps())
          .onItem()
          .transform(rowset -> {
            final var it = rowset.iterator();
            if(it.hasNext()) {
              return it.next();
            }
            return null;
          })
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find next 'RUNTIME_INSTANCE' for update!")));
      }

      @Override
      public Uni<List<RuntimeInstance>> findAllByStatus(List<RuntimeStatus> status) {
        final var sql = registry.getRuntimeInstances().findAllByStatus(status);
        if(log.isDebugEnabled()) {
          log.debug("BatchDbQueryImpl.queryInstances.findAllByStatus query, with props: {} \r\n{}", 
              sql.getProps().deepToString(),
              sql.getValue());
        }
        return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.getRuntimeInstances().defaultMapper())
          .execute(sql.getProps())
          .onItem()
          .transformToUni((RowSet<RuntimeInstance> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find 'RUNTIME_INSTANCE' by status!")));

      }
      

    };
  }

  @Override
  public BatchDbStepQuery querySteps() {
    return new BatchDbStepQuery() {
      @Override
      public Uni<RuntimeStep> getById(String id, boolean lockForUpdate) {
        final var sql = registry.getRuntimeSteps().getById(id, lockForUpdate);
        if(log.isDebugEnabled()) {
          log.debug("BatchDbQueryImpl.querySteps.getById query, with props: {} \r\n{}", 
              sql.getProps().deepToString(),
              sql.getValue());
        }
        return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.getRuntimeSteps().defaultMapper())
          .execute(sql.getProps())
          .onItem()
          .transform(rowset -> {
            final var it = rowset.iterator();
            if(it.hasNext()) {
              return it.next();
            }
            return null;
          })
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find next 'RUNTIME_STEP' for update!")));
      }

      @Override
      public Uni<List<RuntimeStep>> findAllByInstanceId(String instanceId) {
        final var sql = registry.getRuntimeSteps().findAllByInstanceId(instanceId);
        if(log.isDebugEnabled()) {
          log.debug("BatchDbQueryImpl.querySteps.findAllByInstanceId query, with props: {} \r\n{}", 
              sql.getProps().deepToString(),
              sql.getValue());
        }
        return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.getRuntimeSteps().defaultMapper())
          .execute(sql.getProps())
          .onItem()
          .transformToUni((RowSet<RuntimeStep> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find next 'RUNTIME_STEP' by instance id!")));
      }

      @Override
      public Uni<List<RuntimeStep>> findAllByInstanceStatus(List<RuntimeStatus> status) {
        final var sql = registry.getRuntimeSteps().findAllByInstanceStatus(status);
        if(log.isDebugEnabled()) {
          log.debug("BatchDbQueryImpl.querySteps.findAllByInstanceStatus query, with props: {} \r\n{}", 
              sql.getProps().deepToString(),
              sql.getValue());
        }
        return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.getRuntimeSteps().defaultMapper())
          .execute(sql.getProps())
          .onItem()
          .transformToUni((RowSet<RuntimeStep> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find next 'RUNTIME_STEP' by instance status!")));

      }
    };
  }

  @Override
  public BatchDbStepRowQuery queryStepRows() {
    return new BatchDbStepRowQuery() {
      
      @Override
      public Uni<List<RuntimeStepRow>> findAllByInstanceStatus(List<RuntimeStatus> status) {
        
        final var sql = registry.getRuntimeStepRows().findAllByInstanceStatus(status);
        if(log.isDebugEnabled()) {
          log.debug("BatchDbQueryImpl.queryStepRows.findAllByInstanceStatus query, with props: {} \r\n{}", 
              sql.getProps().deepToString(),
              sql.getValue());
        }
        return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.getRuntimeStepRows().defaultMapper())
          .execute(sql.getProps())
          .onItem()
          .transformToUni((RowSet<RuntimeStepRow> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find next 'RUNTIME_STEP' by instance status!")));
      }
    };
  }

  @Override
  public BatchDbMetricQuery queryMetrics() {
    return new BatchDbMetricQuery() {
      
      @Override
      public Uni<List<RuntimeMetric>> findAllByInstanceStatus(List<RuntimeStatus> status) {
        
        final var sql = registry.getRuntimeMetrics().findAllByInstanceStatus(status);
        if(log.isDebugEnabled()) {
          log.debug("BatchDbQueryImpl.queryMetrics.findAllByInstanceStatus query, with props: {} \r\n{}", 
              sql.getProps().deepToString(),
              sql.getValue());
        }
        return dataSource.getClient().preparedQuery(sql.getValue())
          .mapping(registry.getRuntimeMetrics().defaultMapper())
          .execute(sql.getProps())
          .onItem()
          .transformToUni((RowSet<RuntimeMetric> rowset) -> Multi.createFrom().iterable(rowset).collect().asList())
          .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find next 'RUNTIME_METRIC' by instance status!")));
      }
    };
  }
}
