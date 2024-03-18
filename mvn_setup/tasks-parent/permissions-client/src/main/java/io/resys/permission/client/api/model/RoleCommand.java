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

import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;



@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableCreateRole.class, name = "CreateRole"),  
  @Type(value = ImmutableChangeRoleName.class, name = "ChangeRoleName"),  
  @Type(value = ImmutableChangeRoleDescription.class, name = "ChangeRoleDescription"),  
  @Type(value = ImmutableChangeRoleStatus.class, name = "ChangeRoleStatus"), 
  @Type(value = ImmutableChangeRolePermissions.class, name = "ChangeRolePermissions"), 
})

public interface RoleCommand extends Serializable {
  String getId();
  @Nullable String getUserId();
  @Nullable Instant getTargetDate();
  RoleCommandType getCommandType();
  String getComment(); // for auditing purposes, user who made changes must describe why, in a comment.
  
  
  RoleCommand withUserId(String userId);
  RoleCommand withTargetDate(Instant targetDate);
  
  enum RoleCommandType {
    CreateRole, 
    ChangeRoleName, 
    ChangeRoleDescription,
    ChangeRoleStatus,
    ChangeRolePermissions,
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateRole.class) @JsonDeserialize(as = ImmutableCreateRole.class)
  interface CreateRole extends RoleCommand {
    String getName();
    String getDescription();
    List<Permission> getPermissions();
    
    @Value.Default
    @Override default RoleCommandType getCommandType() { return RoleCommandType.CreateRole; }
  }
  
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    @Type(value = ImmutableChangeRoleName.class, name = "ChangeRoleName"), 
    @Type(value = ImmutableChangeRoleDescription.class, name = "ChangeRoleDescription"),  
    @Type(value = ImmutableChangeRoleStatus.class, name = "ChangeRoleStatus"), 
    @Type(value = ImmutableChangeRolePermissions.class, name = "ChangeRolePermissions"), 
  })

  
  interface RoleUpdateCommand extends RoleCommand {
    RoleUpdateCommand withUserId(String userId);
    RoleUpdateCommand withTargetDate(Instant targetDate);
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeRoleName.class) @JsonDeserialize(as = ImmutableChangeRoleName.class)
  interface ChangeRoleName extends RoleUpdateCommand {
    String getName();
    
    @Override default RoleCommandType getCommandType() { return RoleCommandType.ChangeRoleName; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeRoleDescription.class) @JsonDeserialize(as = ImmutableChangeRoleDescription.class)
  interface ChangeRoleDescription extends RoleUpdateCommand {
    String getName();
    
    @Override default RoleCommandType getCommandType() { return RoleCommandType.ChangeRoleDescription; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeRoleStatus.class) @JsonDeserialize(as = ImmutableChangeRoleStatus.class)
  interface ChangeRoleStatus extends RoleUpdateCommand {
    OrgActorStatusType getStatus();
    
    @Override default RoleCommandType getCommandType() { return RoleCommandType.ChangeRoleStatus; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeRolePermissions.class) @JsonDeserialize(as = ImmutableChangeRolePermissions.class)
  interface ChangeRolePermissions extends RoleUpdateCommand {
    List<Permission> getPermissions();
    
    @Override default RoleCommandType getCommandType() { return RoleCommandType.ChangeRolePermissions; }
  }
  
}













