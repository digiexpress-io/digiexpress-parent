package io.resys.thena.projects.client.api.model;

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

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.projects.client.api.model.Project.RepoType;


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableCreateProject.class, name = "CreateProject"),  
  @Type(value = ImmutableArchiveProject.class, name = "ArchiveProject"),
  @Type(value = ImmutableAssignProjectUsers.class, name = "AssignProjectUsers"),
  @Type(value = ImmutableChangeProjectInfo.class, name = "ChangeProjectInfo"),

})
public interface ProjectCommand extends Serializable {
  @Nullable String getUserId();
  @Nullable Instant getTargetDate();
  ProjectCommandType getCommandType();
  
  
  ProjectCommand withUserId(String userId);
  ProjectCommand withTargetDate(Instant targetDate);
  
  enum ProjectCommandType {
    ArchiveProject, CreateProject, AssignProjectUsers, ChangeProjectInfo
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateProject.class) @JsonDeserialize(as = ImmutableCreateProject.class)
  interface CreateProject extends ProjectCommand {
    String getRepoId();
    RepoType getRepoType();
    
    String getTitle();
    String getDescription();
    List<String> getUsers();
    
    @Value.Default
    @Override default ProjectCommandType getCommandType() { return ProjectCommandType.CreateProject; }
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    
    @Type(value = ImmutableArchiveProject.class, name = "ArchiveProject"),
    @Type(value = ImmutableAssignProjectUsers.class, name = "AssignProjectUsers"),
    @Type(value = ImmutableChangeProjectInfo.class, name = "ChangeProjectInfo"),
    
  })
  interface ProjectUpdateCommand extends ProjectCommand {
    String getProjectId();
    ProjectUpdateCommand withUserId(String userId);
    ProjectUpdateCommand withTargetDate(Instant targetDate);
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableArchiveProject.class) @JsonDeserialize(as = ImmutableArchiveProject.class)
  interface ArchiveProject extends ProjectUpdateCommand {
    @Value.Default
    @Override default ProjectCommandType getCommandType() { return ProjectCommandType.ArchiveProject; }
  }

  @Value.Immutable @JsonSerialize(as = ImmutableAssignProjectUsers.class) @JsonDeserialize(as = ImmutableAssignProjectUsers.class)
  interface AssignProjectUsers extends ProjectUpdateCommand {
    List<String> getUsers();
    @Override default ProjectCommandType getCommandType() { return ProjectCommandType.AssignProjectUsers; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeProjectInfo.class) @JsonDeserialize(as = ImmutableChangeProjectInfo.class)
  interface ChangeProjectInfo extends ProjectUpdateCommand {
    String getTitle();
    String getDescription();
    @Override default ProjectCommandType getCommandType() { return ProjectCommandType.ChangeProjectInfo; }
  }

}
