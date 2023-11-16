package io.resys.thena.tasks.dev.app;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;

import io.resys.thena.projects.client.api.TenantConfigClient;
import io.resys.thena.projects.client.api.model.ImmutableCreateTenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfig.TenantRepoConfigType;
import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.model.ImmutableCreateTask;
import io.resys.thena.tasks.client.api.model.TaskCommand.CreateTask;
import io.resys.thena.tasks.dev.app.BeanFactory.CurrentTenant;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;

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

@Path("q/demo/api/")
public class DemoResource {
  @Inject Vertx vertx;
  @Inject TaskClient taskClient;
  @Inject TenantConfigClient tenantClient;
  @Inject CurrentTenant currentTenant;
  

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("populate/tasks/{totalTasks}")
  public Uni<TenantConfig> populate(@PathParam("totalTasks") String totalTasks) {
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

    
    return tenantClient.tenantConfig().queryActiveTenantConfig().get(currentTenant.getTenantId())
    .onItem().transformToUni(config -> {
      final var taskConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.TASKS).findFirst().get();
      return taskClient.withRepoId(taskConfig.getRepoId()).tasks().createTask().createMany(bulk).onItem().transform((data) -> config);
    });
    
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("clear/tasks")
  public Uni<TenantConfig> clear() {
    return tenantClient.tenantConfig().queryActiveTenantConfig().get(currentTenant.getTenantId())
    .onItem().transformToUni(config -> {
        final var taskConfig = config.getRepoConfigs().stream().filter(entry -> entry.getRepoType() == TenantRepoConfigType.TASKS).findFirst().get();
        return taskClient.withRepoId(taskConfig.getRepoId()).tasks().queryActiveTasks().deleteAll("", Instant.now())
            .onItem().transform(tasks -> config);
    });
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("reinit")
  public Uni<TenantConfig> reinit() {
    return tenantClient.repo().query().deleteAll()
        .onItem().transformToUni(junk -> init());
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("kill9")
  public Uni<Void> kill9() {
    return tenantClient.repo().query().deleteAll()
        .onItem().transformToUni(junk -> Uni.createFrom().voidItem());
  }
  private Uni<TenantConfig> init() {
    return tenantClient.repo().query().repoName(currentTenant.getTenantsStoreId(), TenantRepoConfigType.TENANT).createIfNot()
        .onItem().transformToUni(created -> {
          return tenantClient.tenantConfig().createTenantConfig().createOne(ImmutableCreateTenantConfig.builder()
              .name(currentTenant.getTenantId())
              .repoId(currentTenant.getTenantsStoreId())
              .targetDate(Instant.now())
              .build());
        });
  }

}
