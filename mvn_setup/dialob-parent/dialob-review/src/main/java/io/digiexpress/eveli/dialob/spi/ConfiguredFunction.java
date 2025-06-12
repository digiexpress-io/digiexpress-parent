package io.digiexpress.eveli.dialob.spi;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;

import io.dialob.rule.parser.api.ValueType;

@Value.Immutable
public interface ConfiguredFunction {

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