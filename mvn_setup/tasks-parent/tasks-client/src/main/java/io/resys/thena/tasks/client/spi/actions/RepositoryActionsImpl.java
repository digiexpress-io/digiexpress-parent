package io.resys.thena.tasks.client.spi.actions;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.actions.RepositoryActions;
import io.resys.thena.tasks.client.api.actions.RepositoryQuery;
import io.resys.thena.tasks.client.spi.TaskClientImpl;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RepositoryActionsImpl implements RepositoryActions {
  private final DocumentStore ctx;
  @Override
  public Uni<Tenant> getRepo() {
    return ctx.getRepo();
  }
  @Override
  public RepositoryQuery query() {
    DocumentStore.DocumentRepositoryQuery repo = ctx.query();
    return new RepositoryQuery() {
      @Override public RepositoryQuery repoName(String repoName) { repo.repoName(repoName); return this; }
      @Override public RepositoryQuery headName(String headName) { repo.headName(headName); return this; }
      @Override public Uni<TaskClient> createIfNot() { return repo.createIfNot().onItem().transform(doc -> new TaskClientImpl(doc)); }
      @Override public Uni<TaskClient> create() { return repo.create().onItem().transform(doc -> new TaskClientImpl(doc)); }
      @Override public TaskClient build() { return new TaskClientImpl(repo.build()); }
      @Override public Uni<TaskClient> delete() { return repo.delete().onItem().transform(doc -> new TaskClientImpl(doc)); }
    };
  }
}
