package io.resys.thena.projects.client.api.actions;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import io.resys.thena.projects.client.api.model.Project;
import io.resys.thena.projects.client.api.model.ProjectCommand.CreateProject;
import io.resys.thena.projects.client.api.model.ProjectCommand.ProjectUpdateCommand;
import io.smallrye.mutiny.Uni;


public interface ProjectsActions {

  CreateProjectAction createProject();
  UpdateProjectAction updateProject();
  ActiveProjectsQuery queryActiveProjects();

  interface CreateProjectAction {
    Uni<Project> createOne(CreateProject command);
    Uni<List<Project>> createMany(List<? extends CreateProject> commands);
  }

  interface UpdateProjectAction {
    Uni<Project> updateOne(ProjectUpdateCommand command);
    Uni<Project> updateOne(List<ProjectUpdateCommand> commands);
    Uni<List<Project>> updateMany(List<ProjectUpdateCommand> commands);
  }

  interface ActiveProjectsQuery {
    Uni<List<Project>> findAll();
    Uni<List<Project>> findByProjectIds(Collection<String> taskIds);
    Uni<Project> get(String id);
    Uni<List<Project>> deleteAll(String userId, Instant targetDate);
  }
}
