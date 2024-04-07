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

import io.resys.thena.api.entities.org.OrgActorStatus;



@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableCreatePermission.class, name = "CREATE_PERMISSION"), 
  @Type(value = ImmutableChangePermissionName.class, name = "CHANGE_PERMISSION_NAME"), 
  @Type(value = ImmutableChangePermissionDescription.class, name = "CHANGE_PERMISSION_DESCRIPTION"), 
  @Type(value = ImmutableChangePermissionStatus.class, name = "CHANGE_PERMISSION_STATUS"), 
})

public interface PermissionCommand extends Serializable {
  PermissionCommandType getCommandType();
  String getComment(); // for auditing purposes, user who made changes must describe why, in a comment.
  
  enum PermissionCommandType {
    CREATE_PERMISSION,
    CHANGE_PERMISSION_NAME,
    CHANGE_PERMISSION_DESCRIPTION,
    CHANGE_PERMISSION_STATUS,
  }

  enum ChangeType {
    ADD, REMOVE, DISABLE
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableCreatePermission.class) @JsonDeserialize(as = ImmutableCreatePermission.class)
  interface CreatePermission extends PermissionCommand {
    String getName();
    String getComment();
    String getDescription();
    List<String> getPrincipals();   // user names
    List<String> getRoles();        // role name or id or external id
    
    @Value.Default
    @Override default PermissionCommandType getCommandType() { return PermissionCommandType.CREATE_PERMISSION; }
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    @Type(value = ImmutableChangePermissionName.class, name = "CHANGE_PERMISSION_NAME"), 
    @Type(value = ImmutableChangePermissionDescription.class, name = "CHANGE_PERMISSION_DESCRIPTION"), 
    @Type(value = ImmutableChangePermissionStatus.class, name = "CHANGE_PERMISSION_STATUS"), 
  })
  interface PermissionUpdateCommand extends PermissionCommand {
    String getId();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangePermissionName.class) @JsonDeserialize(as = ImmutableChangePermissionName.class)
  interface ChangePermissionName extends PermissionUpdateCommand {
    String getId();
    String getComment();
    String getName();
    
    @Value.Default
    @Override default PermissionCommandType getCommandType() { return PermissionCommandType.CHANGE_PERMISSION_NAME; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangePermissionDescription.class) @JsonDeserialize(as = ImmutableChangePermissionDescription.class)
  interface ChangePermissionDescription extends PermissionUpdateCommand {
    String getId();
    String getComment();
    String getDescription();
    
    @Value.Default
    @Override default PermissionCommandType getCommandType() { return PermissionCommandType.CHANGE_PERMISSION_DESCRIPTION; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangePermissionStatus.class) @JsonDeserialize(as = ImmutableChangePermissionStatus.class)
  interface ChangePermissionStatus extends PermissionUpdateCommand {
    String getId();
    String getComment();
    OrgActorStatus.OrgActorStatusType getStatus();
    
    @Value.Default
    @Override default PermissionCommandType getCommandType() { return PermissionCommandType.CHANGE_PERMISSION_STATUS; }
  }
  
}













