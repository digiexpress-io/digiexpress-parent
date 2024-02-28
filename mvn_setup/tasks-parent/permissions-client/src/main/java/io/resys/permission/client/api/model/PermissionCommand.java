package io.resys.permission.client.api.model;

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

import io.resys.permission.client.api.model.Principal.ActorStatus;
import io.resys.permission.client.api.model.Principal.Role;



@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableCreatePermission.class, name = "CreatePermission"), 
  @Type(value = ImmutableChangePermissionName.class, name = "ChangePermissionName"), 
  @Type(value = ImmutableChangePermissionDescription.class, name = "ChangePermissionDescription"), 
  @Type(value = ImmutableChangePermissionStatus.class, name = "ChangePermissionStatus"), 
})

public interface PermissionCommand extends Serializable {
  String getId();
  @Nullable String getUserId();
  @Nullable Instant getTargetDate();
  PermissionCommandType getCommandType();
  String getComment(); // for auditing purposes, user who made changes must describe why, in a comment.
  
  
  PermissionCommand withUserId(String userId);
  PermissionCommand withTargetDate(Instant targetDate);
  
  enum PermissionCommandType {
    CreatePermission,
    ChangePermissionName,
    ChangePermissionDescription,
    ChangePermissionStatus,
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreatePermission.class) @JsonDeserialize(as = ImmutableCreatePermission.class)
  interface CreatePermission extends PermissionCommand {
    String getName();
    String getDescription();
    List<Role> getRoles();
    
    @Value.Default
    @Override default PermissionCommandType getCommandType() { return PermissionCommandType.CreatePermission; }
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    @Type(value = ImmutableChangePermissionName.class, name = "ChangePermissionName"), 
    @Type(value = ImmutableChangePermissionDescription.class, name = "ChangePermissionDescription"), 
    @Type(value = ImmutableChangePermissionStatus.class, name = "ChangePermissionStatus"), 
  })
  
  interface PermissionUpdateCommand extends PermissionCommand {
    PermissionUpdateCommand withUserId(String userId);
    PermissionUpdateCommand withTargetDate(Instant targetDate);
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangePermissionName.class) @JsonDeserialize(as = ImmutableChangePermissionName.class)
  interface ChangePermissionName extends PermissionUpdateCommand {
    String getName();
    
    @Override default PermissionCommandType getCommandType() { return PermissionCommandType.ChangePermissionName; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangePermissionDescription.class) @JsonDeserialize(as = ImmutableChangePermissionDescription.class)
  interface ChangePermissionDescription extends PermissionUpdateCommand {
    String getDescription();
    
    @Override default PermissionCommandType getCommandType() { return PermissionCommandType.ChangePermissionDescription; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangePermissionStatus.class) @JsonDeserialize(as = ImmutableChangePermissionStatus.class)
  interface ChangePermissionStatus extends PermissionUpdateCommand {
    ActorStatus getStatus();
    
    @Override default PermissionCommandType getCommandType() { return PermissionCommandType.ChangePermissionStatus; }
  }
  
}













