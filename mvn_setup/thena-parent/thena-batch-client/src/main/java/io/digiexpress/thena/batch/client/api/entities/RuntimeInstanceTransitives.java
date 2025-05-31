package io.digiexpress.thena.batch.client.api.entities;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableRuntimeInstanceTransitives.class)
@JsonDeserialize(as = ImmutableRuntimeInstanceTransitives.class)
public interface RuntimeInstanceTransitives {
  List<RuntimeMetric> getMetrics();
  List<RuntimeLog> getLogs(); // only of status is ERROR
  
  List<RuntimeStep> getSteps();
  List<RuntimeStepRow> getStepRows();
}