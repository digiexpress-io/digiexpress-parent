package io.resys.thena.projects.client.rest;

import java.util.List;

import io.resys.thena.projects.client.api.model.Project;
import io.resys.thena.projects.client.api.model.ProjectCommand.CreateProject;
import io.resys.thena.projects.client.api.model.ProjectCommand.ProjectUpdateCommand;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


public interface ProjectRestApi {
  
  @GET @Path("projects") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Project>> findProjects();
  
  @POST @Path("projects") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Project>> createProjects(List<CreateProject> commands);
  
  @PUT @Path("projects") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Project>> updateProjects(List<ProjectUpdateCommand> commands);
  
  @DELETE @Path("projects") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Project>> deleteProjects(List<ProjectUpdateCommand> commands);

  @PUT @Path("projects/{projectId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Project> updateOneProject(@PathParam("projectId") String projectId, List<ProjectUpdateCommand> commands);
}
