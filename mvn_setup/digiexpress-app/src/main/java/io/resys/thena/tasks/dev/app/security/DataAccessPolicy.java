package io.resys.thena.tasks.dev.app.security;

import java.util.Collections;

import io.resys.thena.tasks.client.api.actions.TaskActions.TaskAccess;
import io.resys.thena.tasks.client.api.actions.TaskActions.TaskAccessEvaluator;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;


@ApplicationScoped
public class DataAccessPolicy {
  @Inject PrincipalCache principals;
  private static final DigiExpTaskAccessGranted GRANTED = new DigiExpTaskAccessGranted();
  
  
  public Uni<TaskAccessEvaluator> getTaskAccessEvaluator(CurrentUser currentUser) {
    return principals.getPrincipalPermissions(currentUser.getUserId(), currentUser.getEmail())
        .onItem().transform(principal -> {
          return new TaskAccessEvaluator() {
            @Override
            public TaskAccess getReadAccess(Task task) {
              if(task.getAssigneeIds().contains(principal.getId())) {
                return GRANTED;
              }
              
              final var rolesOverlap = !Collections.disjoint(task.getRoles(), principal.getRoles());
              if(rolesOverlap) {
                return GRANTED;
              } 
              
              return new DigiExpTaskAccessDenied("Task requires one of the following roles: " + task.getRoles() + "!");
            }
            @Override
            public TaskAccess getCreateAccess(Task task) {
              return getReadAccess(task);
            }
            @Override
            public TaskAccess getUpdatedAccess(Task task) {
              return getReadAccess(task);
            }
            @Override
            public TaskAccess getDeleteAccess(Task task) {
              return getReadAccess(task);
            }
          };
        });
  }

  
  @RequiredArgsConstructor
  public static class DigiExpTaskAccessDenied implements TaskAccess {
    private final String msg;
    @Override public boolean isAccessGranted() { return false; }
    @Override public String getMessage() { return msg; } 
  }
  public static class DigiExpTaskAccessGranted implements TaskAccess {
    @Override public boolean isAccessGranted() { return true; }
    @Override public String getMessage() { return ""; } 
  }
}
