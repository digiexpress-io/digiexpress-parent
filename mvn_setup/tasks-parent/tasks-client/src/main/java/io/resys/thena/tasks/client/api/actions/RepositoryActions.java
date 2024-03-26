package io.resys.thena.tasks.client.api.actions;

import io.resys.thena.api.models.Repo;
import io.smallrye.mutiny.Uni;

public interface RepositoryActions {
  RepositoryQuery query();
  Uni<Repo> getRepo();
}
