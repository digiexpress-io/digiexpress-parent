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
  @Type(value = ImmutableCreateSysConfig.class, name = "CreateSysConfig"),  

})
public interface SysConfigCommand extends Serializable {
  @Nullable String getUserId();
  @Nullable Instant getTargetDate();
  SysConfigCommandType getCommandType();
  
  
  SysConfigCommand withUserId(String userId);
  SysConfigCommand withTargetDate(Instant targetDate);
  
  enum SysConfigCommandType {
    CreateSysConfig
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateSysConfig.class) @JsonDeserialize(as = ImmutableCreateSysConfig.class)
  interface CreateSysConfig extends SysConfigCommand {
    
    @Value.Default
    @Override default SysConfigCommandType getCommandType() { return SysConfigCommandType.CreateSysConfig; }
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    //@Type(value = .class, name = ),
  })
  interface SysConfigUpdateCommand extends SysConfigCommand {
    String getId();
    SysConfigUpdateCommand withUserId(String userId);
    SysConfigUpdateCommand withTargetDate(Instant targetDate);
  }
  
}
