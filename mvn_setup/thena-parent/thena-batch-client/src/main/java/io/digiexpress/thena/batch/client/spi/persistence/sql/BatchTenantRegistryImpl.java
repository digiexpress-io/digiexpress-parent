package io.digiexpress.thena.batch.client.spi.persistence.sql;

import io.digiexpress.thena.batch.client.spi.persistence.BatchConsumerRegistry;
import io.digiexpress.thena.batch.client.spi.persistence.BatchRegistry;
import io.digiexpress.thena.batch.client.spi.persistence.BatchTableNames;
import io.digiexpress.thena.batch.client.spi.persistence.BatchTenantRegistry;
import io.digiexpress.thena.batch.client.spi.persistence.RuntimeInstanceRegistry;
import io.digiexpress.thena.batch.client.spi.persistence.RuntimeLogRegistry;
import io.digiexpress.thena.batch.client.spi.persistence.RuntimeMetricRegistry;
import io.digiexpress.thena.batch.client.spi.persistence.RuntimeParamsRegistry;
import io.digiexpress.thena.batch.client.spi.persistence.RuntimeStepRegistry;
import io.digiexpress.thena.batch.client.spi.persistence.RuntimeStepRowRegistry;
import io.resys.thena.datasource.TenantContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public class BatchTenantRegistryImpl implements BatchTenantRegistry {
  private final BatchTableNames options;
  
  private final BatchRegistry batches;
  private final BatchConsumerRegistry batchConsumers;
  
  private final RuntimeInstanceRegistry runtimeInstances;
  private final RuntimeLogRegistry runtimeLogs;
  private final RuntimeMetricRegistry runtimeMetrics;
  private final RuntimeParamsRegistry runtimeParams;
  private final RuntimeStepRegistry runtimeSteps;
  private final RuntimeStepRowRegistry runtimeStepRows;
  
  public BatchTenantRegistryImpl(TenantContext tenant) {
    this.options = BatchTableNames.defaults().toRepo(tenant.getPrefix());
    this.batches = new BatchRegistrySql(this.options);
    this.batchConsumers = new BatchConsumerRegistrySql(this.options);

    this.runtimeInstances = new RuntimeInstanceRegistrySql(this.options);
    this.runtimeLogs = new RuntimeLogRegistrySql(this.options);
    this.runtimeMetrics = new RuntimeMetricRegistrySql(this.options);
    this.runtimeParams = new RuntimeParamsRegistrySql(this.options);
    this.runtimeSteps = new RuntimeStepRegistrySql(this.options);
    this.runtimeStepRows = new RuntimeStepRowRegistrySql(this.options);
  }
}
