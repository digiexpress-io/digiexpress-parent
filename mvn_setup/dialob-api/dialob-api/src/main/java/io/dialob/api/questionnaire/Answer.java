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
package io.dialob.api.questionnaire;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dialob.api.annotation.Nullable;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Date;

@Value.Immutable
@Value.Modifiable
@JsonSerialize(as = ImmutableAnswer.class)
@JsonDeserialize(as = ImmutableAnswer.class)
@Gson.TypeAdapters
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value.Style(validationMethod = Value.Style.ValidationMethod.NONE, jdkOnly = true)
public interface Answer extends Serializable {

  @Value.Parameter
  String getId();

  /**
   * Text and number field answers stored in orignal format
   *
   * @return user's answer in original format
   */
  @Value.Parameter
  @JsonInclude(JsonInclude.Include.ALWAYS)
  @Nullable Object getValue();

  @Nullable String getType();

  @Nullable Object getAcceptedValue();

  @Nullable Date getUpdated();

  @Nullable String getUserId();

}
