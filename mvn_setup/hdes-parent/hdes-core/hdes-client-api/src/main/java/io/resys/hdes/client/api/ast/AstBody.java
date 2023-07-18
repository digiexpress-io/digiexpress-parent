package io.resys.hdes.client.api.ast;

import java.io.Serializable;
import java.util.List;

/*-
 * #%L
 * wrench-assets-datatype
 * %%
 * Copyright (C) 2016 - 2021 Copyright 2020 ReSys OÜ
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

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public interface AstBody extends Serializable {
  String getName();
  @Nullable
  String getDescription();
  Headers getHeaders();
  AstBodyType getBodyType();
  List<AstCommandMessage> getMessages();
  
  @JsonSerialize(as = ImmutableHeaders.class)
  @JsonDeserialize(as = ImmutableHeaders.class)
  @Value.Immutable
  interface Headers extends Serializable {
    List<TypeDef> getAcceptDefs();
    List<TypeDef> getReturnDefs();
  }

  enum AstBodyType { 
    FLOW, FLOW_TASK, DT, TAG, TYPE_DEF
  }
  
  
  @JsonSerialize(as = ImmutableAstSource.class)
  @JsonDeserialize(as = ImmutableAstSource.class)
  @Value.Immutable
  interface AstSource extends Serializable {
    String getId();
    String getHash();
    AstBodyType getBodyType();
    List<AstCommand> getCommands();
  }
  
  @JsonSerialize(as = ImmutableAstCommandMessage.class)
  @JsonDeserialize(as = ImmutableAstCommandMessage.class)
  @Value.Immutable
  interface AstCommandMessage extends Serializable {
    int getLine();
    String getValue();
    CommandMessageType getType();
    @Nullable
    AstCommandRange getRange();
  }
  
  @JsonSerialize(as = ImmutableAstCommandRange.class)
  @JsonDeserialize(as = ImmutableAstCommandRange.class)
  @Value.Immutable
  interface AstCommandRange extends Serializable {
    int getStart();
    int getEnd();
    @Nullable
    Integer getColumn();
    @Nullable
    Boolean getInsert();
  }

  enum CommandMessageType { ERROR, WARNING }
  
}