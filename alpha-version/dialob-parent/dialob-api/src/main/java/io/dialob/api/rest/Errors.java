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
package io.dialob.api.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dialob.api.annotation.Nullable;
import io.swagger.v3.oas.annotations.media.Schema;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Value.Immutable
@Value.Enclosing
@JsonSerialize(as = ImmutableErrors.class)
@JsonDeserialize(as = ImmutableErrors.class)
@Gson.TypeAdapters
@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
@Value.Style(jdkOnly = true)
public abstract class Errors implements Serializable {

  @Schema(description = "error timestamp")
  @Nullable
  @Value.Default
  public Date getTimestamp() {
    return new Date();
  }

  @Schema(description = "HTTP status code", example = "403")
  @Nullable
  public abstract Integer getStatus();

  @Nullable
  public abstract String getError();

  @Nullable
  public abstract String getMessage();

  @Schema(description = "Java stacktrace, if server is configured to send one.")
  @Nullable
  public abstract String getTrace();

  @Nullable
  public abstract String getPath();

  @Schema(description = "List of identified errors in entity")
  @Nullable
  public abstract List<Error> getErrors();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Value.Immutable
  @JsonSerialize(as = ImmutableErrors.Error.class)
  @JsonDeserialize(as = ImmutableErrors.Error.class)
  @Gson.TypeAdapters
  @Value.Style(jdkOnly = true)
  public interface Error extends Serializable {

    @Schema(description = "Error classifying code")
    @Nullable
    String getCode();

    @Schema(description = "Javascript path notation to entity attribute")
    @Nullable
    String getContext();

    @Schema(description = "Invalid value on entity attribute")
    @Nullable
    Object getRejectedValue();

    @Schema(description = "Error description")
    @Nullable
    String getError();
  }
}
