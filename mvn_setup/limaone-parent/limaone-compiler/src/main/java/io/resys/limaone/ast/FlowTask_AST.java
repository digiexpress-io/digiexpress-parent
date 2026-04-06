package io.resys.limaone.ast;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.FlowTask.FlowTaskPropType;
import io.resys.limaone.model.FlowTask.FlowTaskExecutable;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Parameter;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableFlowTask_AST.class)
@JsonDeserialize(as = ImmutableFlowTask_AST.class)
public interface FlowTask_AST extends Simple_AST, Serializable {

  @JsonIgnore
  Class<? extends FlowTaskExecutable> getBeanType();
  
  String getValue();
  @Nullable Parameter getTypeDef0();
  @Nullable Parameter getTypeDef1();
  @Nullable Parameter getReturnDef1();
  
  List<ServiceRef> getRefs();
  

  @Value.Immutable
  @JsonSerialize(as = ImmutableServiceRef.class)
  @JsonDeserialize(as = ImmutableServiceRef.class)
  interface ServiceRef extends Serializable {
    Model.BodyType getBodyType();
    String getRefValue();
  }
  
  FlowTaskPropType getExecutorType();
  

}
