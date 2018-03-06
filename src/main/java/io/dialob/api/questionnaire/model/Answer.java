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
package io.dialob.api.questionnaire.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Date;

@Value.Immutable
@Value.Modifiable
@JsonSerialize(as = ImmutableAnswer.class)
@JsonDeserialize(as = ImmutableAnswer.class)
@Gson.TypeAdapters
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface Answer extends Serializable {

  @Value.Parameter
  String getId();

  @Value.Parameter
  @JsonInclude(JsonInclude.Include.ALWAYS)
  @Nullable Object getValue();

  /**
   * Text and number field answers stored as strings
   *
   * @return user's answer in original format
   */
  @Nullable String getAnswer();

  @Nullable Date getUpdated();

  @Nullable String getUserId();

}
