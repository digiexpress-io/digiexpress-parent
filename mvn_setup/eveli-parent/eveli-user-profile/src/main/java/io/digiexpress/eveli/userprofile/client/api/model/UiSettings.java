package io.digiexpress.eveli.userprofile.client.api.model;

/*-
 * #%L
 * eveli-user-profile
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable @JsonSerialize(as = ImmutableUiSettings.class) @JsonDeserialize(as = ImmutableUiSettings.class)
public interface UiSettings extends Serializable {
  String getId();
  String getUserId();
  String getSettingsId();
  
  List<UiSettings.UiSettingForConfig> getConfig();
  List<UiSettings.UiSettingForVisibility> getVisibility();

  
  
  @Value.Immutable @JsonSerialize(as = ImmutableUiSettingForConfig.class) @JsonDeserialize(as = ImmutableUiSettingForConfig.class)
  interface UiSettingForConfig extends Serializable {
    String getDataId();
    String getValue();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUiSettingForVisibility.class) @JsonDeserialize(as = ImmutableUiSettingForVisibility.class)
  interface UiSettingForVisibility extends Serializable {
    String getDataId();
    Boolean getEnabled();
  }

}
