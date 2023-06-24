package io.resys.thena.tasks.client.spi.visitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.PullActions.PullObjectsQuery;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaObjects.PullObjects;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.DocPullObjectsVisitor;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetActiveTasksByIdsVisitor implements DocPullObjectsVisitor<Task> {
  private final Collection<String> taskIds;
  
  @Override
  public PullObjectsQuery start(DocumentConfig config, PullObjectsQuery builder) {
    return builder.docId(new ArrayList<>(taskIds));
  }

  @Override
  public PullObjects visitEnvelope(DocumentConfig config, QueryEnvelope<PullObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("GET_TASKS_BY_IDS_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(taskIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocumentStoreException.builder("GET_TASKS_BY_IDS_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(taskIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    return result;
  }

  @Override
  public List<Task> end(DocumentConfig config, PullObjects blob) {
    return blob.accept((JsonObject json) -> json.mapTo(ImmutableTask.class));
  }
}
