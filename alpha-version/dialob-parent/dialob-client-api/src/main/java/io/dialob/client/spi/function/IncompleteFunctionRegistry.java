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
package io.dialob.client.spi.function;

import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.api.VariableNotDefinedException;
import io.dialob.rule.parser.function.FunctionRegistry;

public class IncompleteFunctionRegistry implements FunctionRegistry {

  @Override
  public ValueType returnTypeOf(String functionName, ValueType... argTypes) throws VariableNotDefinedException {
    throw new UnsupportedOperationException("returnTypeOf()");
  }

  @Override
  public boolean isAsyncFunction(String functionName) {
    return false;
  }

  @Override
  public void invokeFunction(FunctionCallback callback, String functionName, Object... args) {
    throw new UnsupportedOperationException("invokeFunction()");
  }

  @Override
  public void invokeFunctionAsync(FunctionCallback callback, String functionName, Object... args) {
    throw new UnsupportedOperationException("invokeFunctionAsync()");
  }

  @Override
  public void configureFunction(String functionName, String implementationName, Class<?> implementationClass, boolean async) {

  }
}
