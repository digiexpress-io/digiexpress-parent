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
import io.resys.thena.api.entities.org.OrgActorStatus;



@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableCreatePrincipal.class, name = "CREATE_PRINCIPAL"), 
  @Type(value = ImmutableChangePrincipalRoles.class, name = "CHANGE_PRINCIPAL_ROLES"), 
  @Type(value = ImmutableChangePrincipalStatus.class, name = "CHANGE_PRINCIPAL_STATUS"), 
})

public interface PrincipalCommand extends Serializable {
  PrincipalCommandType getCommandType();
  String getComment(); // for auditing purposes, user who made changes must describe why, in a comment.
  
  enum PrincipalCommandType {
    CREATE_PRINCIPAL,
    CHANGE_PRINCIPAL_ROLES,
    CHANGE_PRINCIPAL_STATUS
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreatePrincipal.class) @JsonDeserialize( as = ImmutableCreatePrincipal.class)
  interface CreatePrincipal extends PrincipalCommand {
    String getName();
    String getEmail();
    List<String> getRoles();
    List<String> getPermissions();
    
    @Value.Default
    @Override default PrincipalCommandType getCommandType() { return PrincipalCommandType.CREATE_PRINCIPAL; } 
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    @Type(value = ImmutableChangePrincipalRoles.class, name = "CHANGE_PRINCIPAL_ROLES"), 
    @Type(value = ImmutableChangePrincipalStatus.class, name = "CHANGE_PRINCIPAL_STATUS"), 
  })
  
  interface PrincipalUpdateCommand extends PrincipalCommand {
    String getId();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableChangePrincipalRoles.class) @JsonDeserialize(as = ImmutableChangePrincipalRoles.class)
  interface ChangePrincipalRoles extends PrincipalUpdateCommand {
    List<String> getRoles(); // id/name/extId
    ChangeType getChangeType();
    
    @Value.Default
    @Override default PrincipalCommandType getCommandType() { return PrincipalCommandType.CHANGE_PRINCIPAL_ROLES; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangePrincipalStatus.class) @JsonDeserialize(as = ImmutableChangePrincipalStatus.class)
  interface ChangePrincipalStatus extends PrincipalUpdateCommand {
    OrgActorStatus.OrgActorStatusType getStatus();
    
    @Value.Default
    @Override default PrincipalCommandType getCommandType() { return PrincipalCommandType.CHANGE_PRINCIPAL_STATUS; }
  }

}













