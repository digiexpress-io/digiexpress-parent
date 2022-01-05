package io.resys.hdes.client.api.programs;

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
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.hdes.client.api.HdesClient.ExecutorBuilder;
import io.resys.hdes.client.api.ast.TypeDef;

public interface Program extends Serializable {
  
  interface ProgramResult extends Serializable {
  }  
  
  interface ProgramContext extends Serializable {
    ProgramContextNamedValue getValueWithMeta(String typeDefName);
    Serializable getValue(TypeDef typeDef);
    
    // Throws exception if not found
    Serializable getValue(String typeDefName);
    Optional<Serializable> findValue(String typeDefName);
    
    <T> T getBean(Class<T> type);
    FlowProgram getFlow(String name);
    DecisionProgram getDecision(String name);
    ServiceProgram getService(String name);
    
    ExecutorBuilder executor();
    
  }
  
  @Value.Immutable
  interface ProgramContextNamedValue {
    Boolean getFound();
    @Nullable
    Serializable getValue();
  }
}
