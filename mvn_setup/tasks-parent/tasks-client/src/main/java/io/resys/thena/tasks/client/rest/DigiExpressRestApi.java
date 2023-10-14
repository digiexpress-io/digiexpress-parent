package io.resys.thena.tasks.client.rest;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.LocalDate;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.resys.thena.tasks.client.api.model.Project;
import io.resys.thena.tasks.client.api.model.Task;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.TaskUpdateCommand;
import io.smallrye.mutiny.Uni;


public interface DigiExpressRestApi {
  
  public static final String PROJECT_ID = "Project-ID";
  
  @GET @Path("projects") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Project>> findProjects();
  
  @GET @Path("tasks") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Task>> findTasks(@HeaderParam(PROJECT_ID) String projectId);
 
  @POST @Path("tasks") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Task>> createTasks(@HeaderParam(PROJECT_ID) String projectId, List<CreateTask> commands);
  
  @PUT @Path("tasks") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Task>> updateTasks(@HeaderParam(PROJECT_ID) String projectId,  List<TaskUpdateCommand> commands);
  
  @DELETE @Path("tasks") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<List<Task>> deleteTasks(@HeaderParam(PROJECT_ID) String projectId, List<TaskUpdateCommand> commands);

  @PUT @Path("tasks/{taskId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Task> updateTask(@HeaderParam(PROJECT_ID) String projectId, @PathParam("taskId") String taskId, List<TaskUpdateCommand> commands);

  @GET @Path("archive/{fromCreatedOrUpdated}") @Produces(MediaType.APPLICATION_JSON)
  Uni<List<Task>> findArchivedTasks(
      @HeaderParam(PROJECT_ID) String projectId, 
      @PathParam("fromCreatedOrUpdated") LocalDate fromCreatedOrUpdated);
  
  @DELETE @Path("tasks/{taskId}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
  Uni<Task> deleteOneTask(@HeaderParam(PROJECT_ID) String projectId, @PathParam("taskId") String taskId, List<TaskUpdateCommand> command);
  
}
