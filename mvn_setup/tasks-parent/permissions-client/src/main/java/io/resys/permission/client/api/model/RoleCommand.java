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
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.permission.client.api.model.PermissionCommand.ChangeType;
import io.resys.permission.client.api.model.Principal.Permission;
import io.resys.thena.api.entities.org.OrgActorStatus;



@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableCreateRole.class, name = "CREATE_ROLE"),  
  @Type(value = ImmutableChangeRoleName.class, name = "CHANGE_ROLE_NAME"),  
  @Type(value = ImmutableChangeRoleDescription.class, name = "CHANGE_ROLE_DESCRIPTION"),  
  @Type(value = ImmutableChangeRoleStatus.class, name = "CHANGE_ROLE_STATUS"), 
  @Type(value = ImmutableChangeRolePermissions.class, name = "CHANGE_ROLE_PERMISSIONS"), 
})

public interface RoleCommand extends Serializable {
  RoleCommandType getCommandType();
  String getComment(); // for auditing purposes, user who made changes must describe why, in a comment.
  
  enum RoleCommandType {
    CREATE_ROLE, 
    CHANGE_ROLE_NAME, 
    CHANGE_ROLE_DESCRIPTION,
    CHANGE_ROLE_STATUS,
    CHANGE_ROLE_PERMISSIONS,
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateRole.class) @JsonDeserialize(as = ImmutableCreateRole.class)
  interface CreateRole extends RoleCommand {
    String getName();
    String getDescription();
    List<Permission> getPermissions();
    
    @Value.Default
    @Override default RoleCommandType getCommandType() { return RoleCommandType.CREATE_ROLE; }
  }
  
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    @Type(value = ImmutableChangeRoleName.class, name = "CHANGE_ROLE_NAME"), 
    @Type(value = ImmutableChangeRoleDescription.class, name = "CHANGE_ROLE_DESCRIPTION"),  
    @Type(value = ImmutableChangeRoleStatus.class, name = "CHANGE_ROLE_STATUS"), 
    @Type(value = ImmutableChangeRolePermissions.class, name = "CHANGE_ROLE_PERMISSIONS"), 
  })

  
  interface RoleUpdateCommand extends RoleCommand {
    String getId();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeRoleName.class) @JsonDeserialize(as = ImmutableChangeRoleName.class)
  interface ChangeRoleName extends RoleUpdateCommand {
    String getId();
    String getName();
    
    @Value.Default
    @Override default RoleCommandType getCommandType() { return RoleCommandType.CHANGE_ROLE_NAME; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeRoleDescription.class) @JsonDeserialize(as = ImmutableChangeRoleDescription.class)
  interface ChangeRoleDescription extends RoleUpdateCommand {
    String getDescription();
    
    @Value.Default
    @Override default RoleCommandType getCommandType() { return RoleCommandType.CHANGE_ROLE_DESCRIPTION; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeRoleStatus.class) @JsonDeserialize(as = ImmutableChangeRoleStatus.class)
  interface ChangeRoleStatus extends RoleUpdateCommand {
    OrgActorStatus.OrgActorStatusType getStatus();
    
    @Value.Default
    @Override default RoleCommandType getCommandType() { return RoleCommandType.CHANGE_ROLE_STATUS; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeRolePermissions.class) @JsonDeserialize(as = ImmutableChangeRolePermissions.class)
  interface ChangeRolePermissions extends RoleUpdateCommand {
    List<String> getPermissions();
    ChangeType getChangeType();
    
    @Value.Default
    @Override default RoleCommandType getCommandType() { return RoleCommandType.CHANGE_ROLE_PERMISSIONS; }
  }
  
}













