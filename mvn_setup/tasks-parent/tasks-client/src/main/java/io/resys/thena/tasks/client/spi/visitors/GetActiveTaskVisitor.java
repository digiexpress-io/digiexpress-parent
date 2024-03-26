package io.resys.thena.tasks.client.spi.visitors;

import io.resys.thena.api.actions.PullActions.PullObjectsQuery;
import io.resys.thena.api.entities.git.ThenaGitObjects.PullObject;
import io.resys.thena.api.models.QueryEnvelope;
import io.resys.thena.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.tasks.client.api.model.ImmutableTask;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentConfig;
import io.resys.thena.tasks.client.spi.store.DocumentConfig.DocPullObjectVisitor;
import io.resys.thena.tasks.client.spi.store.DocumentStoreException;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class GetActiveTaskVisitor implements DocPullObjectVisitor<Task> {
  private final String id;
  
  @Override
  public PullObjectsQuery start(DocumentConfig config, PullObjectsQuery query) {
    return query.docId(id);
  }

  @Override
  public PullObject visitEnvelope(DocumentConfig config, QueryEnvelope<PullObject> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("GET_TASK_BY_ID_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocumentStoreException.builder("GET_TASK_BY_ID_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(id))
        .build();
    }
    return result;
  }

  @Override
  public Task end(DocumentConfig config, PullObject blob) {
    return blob.accept((JsonObject json) -> json.mapTo(ImmutableTask.class));
  }
}
