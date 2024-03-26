package io.resys.thena.tasks.client.spi.visitors;

import io.resys.thena.api.actions.GitPullActions;
import io.resys.thena.api.actions.GitPullActions.PullObject;
import io.resys.thena.api.actions.GitPullActions.PullObjectsQuery;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
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
  public GitPullActions.PullObject visitEnvelope(DocumentConfig config, QueryEnvelope<GitPullActions.PullObject> envelope) {
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
  public Task end(DocumentConfig config, GitPullActions.PullObject blob) {
    return blob.accept((JsonObject json) -> json.mapTo(ImmutableTask.class));
  }
}
