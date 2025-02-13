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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dialob.api.annotation.Nullable;
import io.dialob.api.questionnaire.Error;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableAction.class)
@JsonDeserialize(as = ImmutableAction.class)
@Gson.TypeAdapters(emptyAsNulls = true)
@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
@Value.Style(jdkOnly = true)
public interface Action extends Serializable {

  public enum Type {
    ANSWER(true),
    NEXT(true),
    PREVIOUS(true),
    GOTO(true),
    COMPLETE(true),
    ADD_ROW(true),
    DELETE_ROW(true),
    REMOVE_ANSWERS(true),
    RESET(false),
    ITEM(false),
    REMOVE_ITEMS(false),
    ERROR(false),
    REMOVE_ERROR(false),
    VALUE_SET(false),
    REMOVE_VALUE_SETS(false),
    NOT_FOUND(false),
    SERVER_ERROR(false),
    ROWS(false),
    SET_VALUE(false),
    SET_FAILED(false),
    LOCALE(false),
    SET_LOCALE(true);

    final boolean clientAction;

    Type(boolean clientAction) {
      this.clientAction = clientAction;
    }

    public boolean isClientAction() {
      return clientAction;
    }
  }

  Type getType();

  @Nullable
  String getId();

  @Nullable
  String getMessage();

  @Nullable
  String getTrace();

  @Nullable
  ActionItem getItem();

  @Nullable
  Error getError();

  @Nullable
  Object getAnswer();

  @Nullable
  List<String> getIds();

  @Nullable
  Object getValue();

  @Nullable
  ValueSet getValueSet();

  @JsonIgnore
  @Nullable
  Boolean getServerEvent();

  @JsonIgnore
  @Nullable
  String getResourceId();

}
