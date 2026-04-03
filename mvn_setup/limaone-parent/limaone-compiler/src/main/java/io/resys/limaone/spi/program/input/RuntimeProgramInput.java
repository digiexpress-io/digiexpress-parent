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
import java.util.Map;

import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ProgramInput;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RuntimeProgramInput implements ProgramInput {

  private static final long serialVersionUID = 5418242175019286658L;
  private final io.resys.limaone.program.Runtime runtime;
  private final ProgramInput delegate;

  @Override
  public Serializable getValue(Parameter typeDef) {
    if(typeDef.getBeanType() != null && typeDef.getBeanType().isAssignableFrom(runtime.getClass())) {
      return runtime;
    }
    return delegate.getValue(typeDef);
  }
  @Override
  public ResolvedParameter getValueWithMeta(String name) {
    return delegate.getValueWithMeta(name);
  }
  @Override
  public ProgramInput withInputs(Map<String, Serializable> nextInputs) {
    return new RuntimeProgramInput(runtime, delegate.withInputs(nextInputs));
  }
}
