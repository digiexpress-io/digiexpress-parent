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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ImmutableResolvedParameter;
import io.resys.limaone.program.ProgramInput.ResolvedParameter;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Inputs_Type3 implements ParameterResolver {

  // generic data sources that will be used for init of genericData
  private final List<Supplier<Map<String, Serializable>>> suppliers;
  
  // generic data to transform to target
  private Map<String, Serializable> genericData;
  
  public ResolvedParameter getValue(Parameter typeDef) {
    if(genericData == null) {
      genericData = new HashMap<>();
      suppliers.forEach(e -> genericData.putAll(e.get()));
    }
    if(typeDef.getData() && typeDef.getBeanType() != null) {
      final var value = (Serializable) JsonObject.mapFrom(genericData).mapTo(typeDef.getBeanType());
      return ImmutableResolvedParameter.builder().found(true).value(value).build();
    }
    
    final var value = (Serializable) genericData.get(typeDef.getName());
    return ImmutableResolvedParameter.builder().found(true).value(value).build();
  }
  
  
  public static Inputs_Type3 of(Map<String, Serializable> any) {
    return new Inputs_Type3(Arrays.asList(() -> any));
  }
}
