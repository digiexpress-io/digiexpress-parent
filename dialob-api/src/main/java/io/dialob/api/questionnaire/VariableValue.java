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
package io.dialob.api.questionnaire;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dialob.api.annotation.Nullable;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Value.Immutable
@Value.Modifiable
@JsonSerialize(as = ImmutableVariableValue.class)
@JsonDeserialize(as = ImmutableVariableValue.class)
@Gson.TypeAdapters
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Style(allParameters = true, jdkOnly = true)
public interface VariableValue extends Serializable {

  @NotNull
  String getId();

  @JsonInclude(JsonInclude.Include.ALWAYS)
  @Nullable Object getValue();


}
