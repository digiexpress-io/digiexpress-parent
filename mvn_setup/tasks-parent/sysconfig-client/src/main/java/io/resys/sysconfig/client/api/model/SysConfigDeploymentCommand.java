package io.resys.sysconfig.client.api.model;

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

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;



@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableCreateSysConfigDeployment.class, name = "CreateDeployment"),
  @Type(value = ImmutableUpdateSysConfigDeploymentLiveDate.class, name = "UpdateDeploymentLiveDate"),
  @Type(value = ImmutableUpdateSysConfigDeploymentDisabled.class, name = "UpdateDeploymentDisabled"),
})
public interface SysConfigDeploymentCommand extends Serializable {
  @Nullable String getUserId();
  @Nullable Instant getTargetDate();
  SysConfigDeploymentCommandType getCommandType();
  
  
  SysConfigDeploymentCommand withUserId(String userId);
  SysConfigDeploymentCommand withTargetDate(Instant targetDate);
  
  enum SysConfigDeploymentCommandType {
    CreateDeployment, UpdateDeploymentLiveDate, UpdateDeploymentDisabled
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateSysConfigDeployment.class) @JsonDeserialize(as = ImmutableCreateSysConfigDeployment.class)
  interface CreateSysConfigDeployment extends SysConfigDeploymentCommand {
    String getDeploymentId(); // user given unique id
    @Nullable Boolean getDisabled();
    Instant getLiveDate();
    SysConfigRelease getBody();
    
    @JsonIgnore @Value.Default @Override 
    default SysConfigDeploymentCommandType getCommandType() { return SysConfigDeploymentCommandType.CreateDeployment; }
  }

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    @Type(value = ImmutableUpdateSysConfigDeploymentLiveDate.class, name = "UpdateDeploymentLiveDate"),
    @Type(value = ImmutableUpdateSysConfigDeploymentDisabled.class, name = "UpdateDeploymentDisabled"),
  })
  interface SysConfigDeploymentUpdateCommand extends SysConfigDeploymentCommand {
    String getId();
    SysConfigDeploymentCommand withUserId(String userId);
    SysConfigDeploymentCommand withTargetDate(Instant targetDate);
  }

  @Value.Immutable @JsonSerialize(as = ImmutableUpdateSysConfigDeploymentLiveDate.class) @JsonDeserialize(as = ImmutableUpdateSysConfigDeploymentLiveDate.class)
  interface UpdateSysConfigDeploymentLiveDate extends SysConfigDeploymentUpdateCommand {
    Instant getLiveDate();
    
    @JsonIgnore @Value.Default @Override 
    default SysConfigDeploymentCommandType getCommandType() { return SysConfigDeploymentCommandType.UpdateDeploymentLiveDate; }
  }
  @Value.Immutable @JsonSerialize(as = ImmutableUpdateSysConfigDeploymentDisabled.class) @JsonDeserialize(as = ImmutableUpdateSysConfigDeploymentDisabled.class)
  interface UpdateSysConfigDeploymentDisabled extends SysConfigDeploymentUpdateCommand {
    Boolean getDisabled();
    
    @JsonIgnore @Value.Default @Override 
    default SysConfigDeploymentCommandType getCommandType() { return SysConfigDeploymentCommandType.UpdateDeploymentDisabled; }
  }
  
}
