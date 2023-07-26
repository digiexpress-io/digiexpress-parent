/*
 * Copyright 2018 ReSys OÜ
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
package io.dialob.api.form;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dialob.api.annotation.AllowNulls;
import io.dialob.api.annotation.Nullable;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Value.Immutable
@Value.Modifiable
@JsonSerialize(as = ImmutableFormItem.class)
@JsonDeserialize(as = ImmutableFormItem.class)
@Gson.TypeAdapters
@JsonIgnoreProperties({"style", "options"})
@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
@Value.Style(validationMethod = Value.Style.ValidationMethod.NONE, jdkOnly = true)
public interface FormItem extends Serializable {

  @NotNull
  String getId();

  @NotNull
  String getType();

  @Nullable
  String getView();

  @NotNull
  Map<String, String> getLabel();

  @NotNull
  Map<String, String> getDescription();

  @Nullable
  String getRequired();

  @NotNull
  Map<String, String> getRequiredErrorText();

  @Nullable
  Boolean getReadOnly();

  @NotNull
  List<String> getItems();

  @NotNull
  List<String> getClassName();

  @Nullable
  String getActiveWhen();

  @Nullable
  String getCanAddRowWhen();

  @Nullable
  String getCanRemoveRowWhen();

  @NotNull
  List<Validation> getValidations();

  @Nullable
  String getValueSetId();

  @Nullable
  Object getDefaultValue();

  @Nullable @AllowNulls
  Map<String, Object> getProps();

  @JsonInclude
  @JsonAnyGetter
  @AllowNulls
  @Gson.Ignore
  Map<String, Object> getAdditionalProperties();

}
