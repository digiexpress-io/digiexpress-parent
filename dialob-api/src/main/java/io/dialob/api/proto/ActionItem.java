/*
 * Copyright 2017 ReSys OÜ
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.api.proto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dialob.api.annotation.AllowNulls;
import io.dialob.api.annotation.Nullable;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Value.Immutable
@JsonSerialize(as = ImmutableActionItem.class)
@JsonDeserialize(as = ImmutableActionItem.class)
@Gson.TypeAdapters(emptyAsNulls = true)
@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
@Value.Style(jdkOnly = true)
public interface ActionItem extends Serializable {

  String getId();

  String getType();

  @Nullable String getView();

  @Nullable String getLabel();

  @Nullable String getDescription();

  @Nullable Boolean getInactive();

  @Nullable Boolean getDisabled();

  @Nullable Boolean getRequired();

  @Nullable List<String> getClassName();

  @Nullable Object getValue();

  @Nullable List<String> getItems();

  @Nullable String getActiveItem();

  @Nullable List<String> getAvailableItems();

  @Nullable Set<Action.Type> getAllowedActions();

  @Nullable Boolean getAnswered();

  @Nullable String getValueSetId();

  @Nullable @AllowNulls
  Map<String, Object> getProps();

}
