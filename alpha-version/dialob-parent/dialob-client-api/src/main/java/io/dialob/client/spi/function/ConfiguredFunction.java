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

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;

import io.dialob.rule.parser.api.ValueType;

@Value.Immutable
interface ConfiguredFunction {

  String getFunctionName();

  String getStaticMethodName();

  ValueType getReturnType();

  List<ValueType> getArgumentValueTypes();

  @Value.Default
  default Predicate<ValueType[]> getArgumentMatcher() {
    return argTypes -> getArgumentValueTypes().equals(Arrays.asList(argTypes));
  }

  Class<?>[] getArgumentTypes();

  Class getFunctionImplementationClass();

  boolean isAsync();

  default boolean doesMatch(String canonicalFunctionName, final Object... args) {
    if (StringUtils.equalsAny(canonicalFunctionName, getFunctionName(), getCanonicalName())) {
      final Class<?>[] argumentTypes = getArgumentTypes();
      for (int i = 0; i < args.length; i++) {
        if (argumentTypes.length < i
          || (args[i] != null && !argumentTypes[i].isAssignableFrom(args[i].getClass())))
        {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  default String getCanonicalName() {
    return getFunctionImplementationClass().getCanonicalName() + "." + getStaticMethodName();
  }
}

