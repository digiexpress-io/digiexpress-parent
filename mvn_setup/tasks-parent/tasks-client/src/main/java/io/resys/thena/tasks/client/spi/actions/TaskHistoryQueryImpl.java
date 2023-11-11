package io.resys.thena.tasks.client.spi.actions;

import io.resys.thena.docdb.support.RepoAssert;
import io.resys.thena.tasks.client.api.actions.TaskActions;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import io.resys.thena.tasks.client.spi.visitors.GetTaskHistoryVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskHistoryQueryImpl implements TaskActions.TaskHistoryQuery {

  private final DocumentStore ctx;

  @Override
  public Uni<Task.TaskHistory> get(String taskId) {
    RepoAssert.notNull(taskId, () -> "taskId can't be null!");
    return ctx.getConfig().accept(new GetTaskHistoryVisitor(taskId))
        .onItem().transform(items -> items.get(0));

  }
}
