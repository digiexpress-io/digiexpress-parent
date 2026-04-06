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
import java.util.function.Function;

import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ImmutableResolvedParameter;
import io.resys.limaone.program.ProgramInput.ResolvedParameter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Inputs_Type1 implements ParameterResolver {
  private final Function<Parameter, Object> callbackThatWillSupplyAllData;

  @Override
  public ResolvedParameter getValue(Parameter typeDef) {
    if(callbackThatWillSupplyAllData == null) {
      return ImmutableResolvedParameter.builder().found(false).build();
    }
    final Serializable target = (Serializable) callbackThatWillSupplyAllData.apply(typeDef);
    if(target != null) {
      return ImmutableResolvedParameter.builder().found(true).value(target).build();
    }
    return ImmutableResolvedParameter.builder().found(false).build();
  }
}
