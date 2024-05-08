package io.resys.userprofile.client.api.model;

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



@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableUpsertUiSettings.class, name = "UpsertUiSettings")
})
public interface UiSettingsCommand extends Serializable {
  String getUserId();
  String getSettingsId();
  UiSettingsCommandType getCommandType();
  
  enum UiSettingsCommandType {
    UpsertUiSettings
  }

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    @Type(value = ImmutableUpsertUiSettings.class, name = "UpsertUiSettings")
  })
  interface UiSettingsUpdateCommand extends UiSettingsCommand {}
  
  @Value.Immutable @JsonSerialize(as = ImmutableUpsertUiSettings.class) @JsonDeserialize(as = ImmutableUpsertUiSettings.class)
  interface UpsertUiSettings extends UiSettingsUpdateCommand {
    List<UiSettings.UiSettingForConfig> getConfig();
    List<UiSettings.UiSettingForVisibility> getVisibility();
    List<UiSettings.UiSettingsForSorting> getSorting();
    @Override default UiSettingsCommandType getCommandType() { return UiSettingsCommandType.UpsertUiSettings; }
  }  
}
