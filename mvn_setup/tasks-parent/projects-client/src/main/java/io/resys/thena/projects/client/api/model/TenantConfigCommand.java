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
  @Type(value = ImmutableCreateTenantConfig.class, name = "CreateTenantConfig"),  
  @Type(value = ImmutableArchiveTenantConfig.class, name = "ArchiveTenantConfig"),
  @Type(value = ImmutableChangeTenantConfigInfo.class, name = "ChangeTenantConfigInfo"),

})
public interface TenantConfigCommand extends Serializable {
  @Nullable String getUserId();
  @Nullable Instant getTargetDate();
  TenantConfigCommandType getCommandType();
  
  
  TenantConfigCommand withUserId(String userId);
  TenantConfigCommand withTargetDate(Instant targetDate);
  
  enum TenantConfigCommandType {
    ArchiveTenantConfig, CreateTenantConfig, ChangeTenantConfigInfo
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateTenantConfig.class) @JsonDeserialize(as = ImmutableCreateTenantConfig.class)
  interface CreateTenantConfig extends TenantConfigCommand {
    String getRepoId();
    String getName();
    
    @Value.Default
    @Override default TenantConfigCommandType getCommandType() { return TenantConfigCommandType.CreateTenantConfig; }
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    
    @Type(value = ImmutableArchiveTenantConfig.class, name = "ArchiveTenantConfig"),
    @Type(value = ImmutableChangeTenantConfigInfo.class, name = "ChangeTenantConfigInfo"),
    
  })
  interface TenantConfigUpdateCommand extends TenantConfigCommand {
    String getTenantConfigId();
    TenantConfigUpdateCommand withUserId(String userId);
    TenantConfigUpdateCommand withTargetDate(Instant targetDate);
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableArchiveTenantConfig.class) @JsonDeserialize(as = ImmutableArchiveTenantConfig.class)
  interface ArchiveTenantConfig extends TenantConfigUpdateCommand {
    @Value.Default
    @Override default TenantConfigCommandType getCommandType() { return TenantConfigCommandType.ArchiveTenantConfig; }
  }

  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeTenantConfigInfo.class) @JsonDeserialize(as = ImmutableChangeTenantConfigInfo.class)
  interface ChangeTenantConfigInfo extends TenantConfigUpdateCommand {
    String getName();
    @Override default TenantConfigCommandType getCommandType() { return TenantConfigCommandType.ChangeTenantConfigInfo; }
  }

}
