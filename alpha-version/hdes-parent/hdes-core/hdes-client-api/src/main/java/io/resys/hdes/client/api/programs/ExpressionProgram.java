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

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.hdes.client.api.ast.TypeDef.ValueType;
import io.resys.hdes.client.api.programs.Program.ProgramResult;

public interface ExpressionProgram {
  String getSrc();
  ValueType getType();
  List<String> getConstants();
  ExpressionResult run(Object context);

  @Value.Immutable
  interface ExpressionResult extends ProgramResult {
    ValueType getType();
    List<String> getConstants();
    @Nullable
    Object getValue();
  }

}
