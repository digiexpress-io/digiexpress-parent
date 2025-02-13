/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.rule.parser.api;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Value.Enclosing
public interface VariableFinder {

  interface Var {

    String getName();

    ValueType getValueType();

  }

  @Value.Immutable
  interface Variable extends Var {

    Optional<String> getScope();

    Optional<Object> getPlaceHolderValue();

    Optional<String> getValueSetId();

  }

  @Value.Immutable
  interface Function extends Var {

    boolean isAsync();

  }

  @Nullable
  String getScope();

  @Nullable
  ValueType typeOf(String variableName) throws VariableNotDefinedException;

  @Nullable
  ValueType returnTypeOf(String functionName, ValueType... argTypes) throws VariableNotDefinedException;

  boolean isAsync(String functionName);

  /**
   * Maps context specific alias id to real id
   *
   * @param aliasName
   * @return id of actual variable
   */
  @NotNull
  default String mapAlias(String aliasName) {
    return aliasName;
  }

  /**
   * @param variableName
   * @return contextId of variable, and null if global
   */
  default Optional<String> findVariableScope(String variableName) {
    return Optional.empty();
  }

  default Optional<String> findValueSetIdFor(String variableId) {
    return Optional.empty();
  }

  default Optional<Object> getVariableDefaultValue(String variableId) {
    return Optional.empty();
  }
}
