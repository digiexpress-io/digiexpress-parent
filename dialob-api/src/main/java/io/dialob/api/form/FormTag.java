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

import java.util.Date;

import jakarta.validation.constraints.NotNull;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableFormTag.class)
@JsonDeserialize(as = ImmutableFormTag.class)
@Gson.TypeAdapters
@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
@Value.Style(validationMethod = Value.Style.ValidationMethod.NONE, jdkOnly = true)
public interface FormTag extends FormEntity {

  enum Type {
    NORMAL,
    MUTABLE
  }

  @NotNull
  String getFormName();

  @NotNull
  String getName();

  @Nullable
  String getRefName();

  @Nullable
  Date getCreated();

  @Nullable
  String getFormId();

  @Nullable
  String getDescription();

  @NotNull
  @Value.Default
  default Type getType() {
    return Type.NORMAL;
  }

}
