package io.resys.thena.tasks.dev.app;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

/*-
 * #%L
 * thena-quarkus-dev-app
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

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentProject;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Path("q/demo/api/")
public class DemoResource {
  @Inject Vertx vertx;
  @Inject TaskClient client;
  @Inject CurrentProject currentProject;
  
  //http://localhost:8080/portal/active/tasks
  @Jacksonized @Data @Builder
  public static class HeadState {
    private Boolean created;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("populate/tasks/{totalTasks}")
  public Uni<HeadState> populate(@PathParam("totalTasks") String totalTasks) {
    final int count = totalTasks == null ? 50 : Integer.parseInt(totalTasks);

    
    final var provider =  new RandomDataProvider();
    final var bulk = new ArrayList<CreateTask>();
    final var targetDate = Instant.now();
    
    for(int index = 0; index < count; index++) {
      final var startAndDueDate = provider.getStartDateAndDueDate(LocalDate.ofInstant(targetDate, ZoneId.of("UTC")));
      final var newTask = ImmutableCreateTask.builder()
      .startDate(startAndDueDate.getStartDate())
      .dueDate(startAndDueDate.getDueDate())
      .targetDate(targetDate.minus(10, java.time.temporal.ChronoUnit.DAYS))
      .checklist(provider.getChecklists(LocalDate.ofInstant(targetDate, ZoneId.of("UTC"))))
      .title(provider.getTitle())
      .description(provider.getDescription())
      .priority(provider.getPriority())
      .roles(provider.getRoles())
      .assigneeIds(provider.getAssigneeIds())
      .reporterId(provider.getReporterId())
      .status(provider.getStatus())
      .userId("demo-gen-1")
      .addAllExtensions(provider.getExtensions())
      .comments(provider.getComments())
      .build();
      bulk.add(newTask);
    }
    final var response = HeadState.builder().created(true).build();
    return client.repo().query().repoName(currentProject.getProjectId()).headName(currentProject.getHead()).createIfNot()
        .onItem().transformToUni(created -> {
          return client.tasks().createTask().createMany(bulk).onItem().transform((data) -> response);
        });
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("clear/tasks")
  public Uni<HeadState> clear() {
    return client.repo().query().repoName(currentProject.getProjectId()).headName(currentProject.getHead()).createIfNot()
        .onItem().transformToUni(created -> {
          
            return client.tasks().queryActiveTasks().deleteAll("", Instant.now())
                .onItem().transform(tasks -> HeadState.builder().created(true).build());
          
        });
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("reinit/tasks")
  public Uni<HeadState> reinit() {
    return client.repo().query().repoName(currentProject.getProjectId()).headName(currentProject.getHead()).delete()
        .onItem().transformToUni(junk -> init());
  }

  private Uni<HeadState> init() {
    return client.repo().query().repoName(currentProject.getProjectId()).headName(currentProject.getHead()).createIfNot()
        .onItem().transform(created -> HeadState.builder().created(true).build());
  }

}
