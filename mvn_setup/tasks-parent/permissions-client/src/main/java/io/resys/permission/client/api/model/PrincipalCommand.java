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

import io.resys.permission.client.api.model.Principal.Role;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatusType;



@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableChangePrincipalRoles.class, name = "ChangePrincipalRoles"), 
  @Type(value = ImmutableChangePrincipalStatus.class, name = "ChangePrincipalStatus"), 
})

public interface PrincipalCommand extends Serializable {
  String getId();
  @Nullable String getUserId();
  @Nullable Instant getTargetDate();
  PrincipalCommandType getCommandType();
  String getComment(); // for auditing purposes, user who made changes must describe why, in a comment.
  
  
  PrincipalCommand withUserId(String userId);
  PrincipalCommand withTargetDate(Instant targetDate);
  
  enum PrincipalCommandType {
    ChangePrincipalRoles,
    ChangePrincipalStatus
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    @Type(value = ImmutableChangePrincipalRoles.class, name = "ChangePrincipalRoles"), 
    @Type(value = ImmutableChangePrincipalStatus.class, name = "ChangePrincipalStatus"), 
  })
  
  interface PrincipalUpdateCommand extends PrincipalCommand {
    PrincipalUpdateCommand withUserId(String userId);
    PrincipalUpdateCommand withTargetDate(Instant targetDate);
  }

  @Value.Immutable @JsonSerialize(as = ImmutableChangePrincipalRoles.class) @JsonDeserialize(as = ImmutableChangePrincipalRoles.class)
  interface ChangePrincipalRoles extends PrincipalUpdateCommand {
    List<Role> getRoles();
    
    @Override default PrincipalCommandType getCommandType() { return PrincipalCommandType.ChangePrincipalRoles; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangePrincipalStatus.class) @JsonDeserialize(as = ImmutableChangePrincipalStatus.class)
  interface ChangePrincipalStatus extends PrincipalUpdateCommand {
    OrgActorStatusType getStatus();
    
    @Override default PrincipalCommandType getCommandType() { return PrincipalCommandType.ChangePrincipalStatus; }
  }

}













