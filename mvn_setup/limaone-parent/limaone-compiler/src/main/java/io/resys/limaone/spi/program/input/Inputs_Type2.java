package io.resys.limaone.spi.program.input;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import io.resys.limaone.model.FlowTask.ServiceData;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ImmutableResolvedParameter;
import io.resys.limaone.program.ProgramInput.ResolvedParameter;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Inputs_Type2 implements ParameterResolver {
  //data object that should be directly transformed to target
  private final Object serviceData;
  private boolean isBuilt;
  private boolean isSuitable;
  
  public ResolvedParameter getValue(Parameter typeDef) {
    if(serviceData == null) {
      return ImmutableResolvedParameter.builder().found(false).build();
    }
    if(!isBuilt) {
      isBuilt = true;
      isSuitable = serviceData.getClass().isAnnotationPresent(ServiceData.class);
    }
    
    if(!isSuitable) {
      return ImmutableResolvedParameter.builder().found(false).build();
    }
    
    if(Boolean.TRUE.equals(typeDef.getData())) {
      final var value = (Serializable) JsonObject.mapFrom(serviceData).mapTo(typeDef.getBeanType());
      return ImmutableResolvedParameter.builder().found(true).value(value).build();
    }
    return ImmutableResolvedParameter.builder().found(false).build();
  }
}
