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

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import io.dialob.rule.parser.api.ValueType;
import io.dialob.rule.parser.api.VariableNotDefinedException;
import io.dialob.rule.parser.function.FunctionRegistry;
import io.dialob.rule.parser.function.FunctionRegistryException;
import lombok.extern.slf4j.Slf4j;
import org.immutables.value.Value;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

@Slf4j
@Value.Enclosing
public class FunctionRegistryImpl implements FunctionRegistry {

  private final ListMultimap<String, ConfiguredFunction> configuredFunctions = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

  private static final Predicate<ValueType[]> MATCH_OBJECT_ARRAY = args -> {
    return args.length == 1 && args[0].isArray();
  };

  @Override
  public ValueType returnTypeOf(@Nonnull String functionName, ValueType... argTypes) throws VariableNotDefinedException {
    for (ConfiguredFunction configuredFunction : configuredFunctions.get(functionName)) {
      if (configuredFunction != null && configuredFunction.getArgumentMatcher().test(argTypes)) {
        return configuredFunction.getReturnType();
      }
    }
    throw new VariableNotDefinedException(functionName);
  }

  @Override
  public boolean isAsyncFunction(String functionName) {
    for (ConfiguredFunction configuredFunction : configuredFunctions.get(functionName)) {
      if (configuredFunction.isAsync()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void configureFunction(@Nonnull String functionName, @Nonnull Class<?> implementationClass, boolean async) {
    configureFunction(functionName, functionName, implementationClass, async);
  }

  public void configureFunction(@Nonnull String functionName, @Nonnull String implementationName, @Nonnull Class<?> implementationClass, boolean async) {
    try {
      for (final Method method : implementationClass.getMethods()) {
        if (method.getName().equals(implementationName) && isPublicAndStatic(method)) {
          final ValueType returnType = ValueType.valueTypeOf(method.getReturnType());
          final List<ValueType> argumentTypes = new ArrayList<>();
          log.debug("Try register method {} as {}[]", method, functionName);
          Predicate<ValueType[]> argumentMatcher = null;
          for (final Class<?> argType : method.getParameterTypes()) {
            final ValueType valueType;
            if (argType == Object[].class) {
              argumentMatcher = MATCH_OBJECT_ARRAY;
            } else {
              valueType = ValueType.valueTypeOf(argType);
              if (valueType == null) {
                log.warn("Failed to map {}", argType);
              }
              argumentTypes.add(valueType);
            }
          }
          if (!argumentTypes.contains(null)) {
            ImmutableConfiguredFunction.Builder builder = ImmutableConfiguredFunction.builder();
            if (argumentMatcher != null) {
              builder.argumentMatcher(argumentMatcher);
            }
            configuredFunctions.put(functionName,
              builder
                .functionName(functionName)
                .staticMethodName(implementationName)
                .returnType(returnType)
                .addAllArgumentValueTypes(argumentTypes)
                .argumentTypes(method.getParameterTypes())
                .functionImplementationClass(implementationClass)
                .isAsync(async)
              .build());
            return;
          } else {
            log.warn("Could not map function '{}' argument types to fact types. Registration skipped.", functionName);
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    throw new FunctionRegistryException("Could not find function public static "+ implementationClass.getCanonicalName() + "." + implementationName);
  }

  private boolean isPublicAndStatic(Method method) {
    return Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers());
  }


  /**
   * Invoke function dynamically by name
   *
   * @param callback      After function this result is delegated to this
   * @param functionName  Name of function to call
   * @param args          List of call arguments
   */
  @Override
  public void invokeFunction(final FunctionRegistry.FunctionCallback callback, @Nonnull String functionName, Object... args) {
    String failure;
    try {
      ConfiguredFunction configuredFunction = findConfiguredFunction(functionName, args);
      Method method = findMethod(configuredFunction);
      if (method != null) {
        final Object out = method.invoke(null, args);
        callback.succeeded(configuredFunction.getReturnType().getTypeClass().cast(out));
        return;
      }
      failure = "Can't find function " + functionName;
    } catch (InvocationTargetException e) {
      // Exception thrown by function
      failure = e.getTargetException().getMessage();
    } catch (Exception e) {
      log.warn("Couldn't invoke function " + functionName, e);
      failure = e.getMessage();
    }
    callback.failed(failure);
  }

  protected ConfiguredFunction findConfiguredFunction(final String canonicalFunctionName, final Object... args) {
    final String functionName = canonicalFunctionName.substring(canonicalFunctionName.lastIndexOf('.') + 1);
    for (final ConfiguredFunction configuredFunction : configuredFunctions.get(functionName)) {
      if (configuredFunction.doesMatch(canonicalFunctionName, args)) {
        return configuredFunction;
      }
    }
    return null;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private Method findMethod(ConfiguredFunction configuredFunction) throws NoSuchMethodException {
    if (configuredFunction == null) {
      return null;
    }
    final Class implClass = configuredFunction.getFunctionImplementationClass();
    // TODO: Caching by function name + arg values as key
    return implClass.getMethod(configuredFunction.getStaticMethodName(), configuredFunction.getArgumentTypes());
  }

  /**
   * Execute dynamically invoked function asynchronously by name
   *
   * @param callback              After function this result is delegated to this
   * @param functionName Canonical function name to call
   * @param args                  List of arguments
   */
  @Override
  public void invokeFunctionAsync(final FunctionCallback callback, @Nonnull String functionName, Object... args) {
    try {
      invokeFunction(callback, functionName, args);
    } catch (Exception taskRejectedException) {
      callback.failed(taskRejectedException.getMessage());
    }
  }

}
