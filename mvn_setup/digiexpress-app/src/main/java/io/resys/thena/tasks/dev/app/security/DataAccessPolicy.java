package io.resys.thena.tasks.dev.app.security;

import java.util.Collections;
import java.util.List;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import io.resys.thena.tasks.client.api.actions.TaskActions.TaskAccess;
import io.resys.thena.tasks.client.api.actions.TaskActions.TaskAccessEvaluator;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.thenamission.support.TaskAccessException;
import io.resys.thena.tasks.dev.app.user.CurrentUser;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;


@Singleton
public class DataAccessPolicy {
  @Inject PrincipalCache principals;
  private static final DigiExpTaskAccessGranted GRANTED = new DigiExpTaskAccessGranted();
  
  @ServerExceptionMapper(value = TaskAccessException.class)
  public RestResponse<TaskAccess> mapException(TaskAccessException x) {
      return RestResponse.status(Response.Status.FORBIDDEN, x.getAccess());
  }
  
  public Uni<TaskAccessEvaluator> getTaskAccessEvaluator(CurrentUser currentUser) {
    return principals.getPrincipalPermissions(currentUser.getUserId(), currentUser.getEmail())
        .onItem().transform(principal -> {
          return new TaskAccessEvaluator() {
            @Override
            public TaskAccess getReadAccess(Task task) {
              if(task.getAssigneeIds().contains(principal.getId())) {
                return GRANTED;
              }
              
              if(task.getRoles().isEmpty()) {
                return GRANTED;
              }
              
              final var rolesOverlap = !Collections.disjoint(task.getRoles(), principal.getRoles());
              if(rolesOverlap) {
                return GRANTED;
              } 
              
              return new DigiExpTaskAccessDenied("Task requires one of the following roles: " + task.getRoles() + "!", task.getRoles());
            }
            @Override
            public TaskAccess getCreateAccess(Task task) {
              return GRANTED;
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
    private final List<String> required;
    @Override public boolean isAccessGranted() { return false; }
    @Override public String getMessage() { return msg; }
    @Override public List<String> getRequired() { return required; } 
  }
  public static class DigiExpTaskAccessGranted implements TaskAccess {
    @Override public boolean isAccessGranted() { return true; }
    @Override public String getMessage() { return ""; } 
    @Override public List<String> getRequired() { return Collections.emptyList(); } 
  }
}
