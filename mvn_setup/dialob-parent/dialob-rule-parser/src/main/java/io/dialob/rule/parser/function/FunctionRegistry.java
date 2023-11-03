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
package io.dialob.rule.parser.function;

import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.api.VariableNotDefinedException;
import org.jetbrains.annotations.NotNull;

public interface FunctionRegistry {

  @NotNull
  ValueType returnTypeOf(@NotNull String functionName, ValueType... argTypes) throws VariableNotDefinedException;

  boolean isAsyncFunction(String functionName);

  void invokeFunction(FunctionRegistry.FunctionCallback callback, @NotNull String functionName, Object... args);

  void invokeFunctionAsync(FunctionRegistry.FunctionCallback callback, @NotNull String functionName, Object... args);

  void configureFunction(@NotNull String functionName, @NotNull String implementationName, @NotNull Class<?> implementationClass, boolean async);

  default void configureFunction(@NotNull String functionName, @NotNull Class<?> implementationClass, boolean async) {
    configureFunction(functionName, functionName, implementationClass, async);
  }

  interface FunctionCallback {
    /**
     * Called when function execution completes normally.
     * @param result return value from function
     */
    void succeeded(Object result);

      /**
       * Called if there was error in function execution
       * @param error error message from execution
       */
    void failed(@NotNull String error);
  }
}
