package io.digiexpress.thena.batch.client.spi.persistence;

public interface BatchTenantRegistry {

  BatchRegistry getBatches();
  BatchConsumerRegistry getBatchConsumers();

  RuntimeInstanceRegistry getRuntimeInstances();
  RuntimeLogRegistry getRuntimeLogs();
  RuntimeMetricRegistry getRuntimeMetrics();
  RuntimeParamsRegistry getRuntimeParams();
  RuntimeStepRegistry getRuntimeSteps();
  RuntimeStepRowRegistry getRuntimeStepRows();
}
