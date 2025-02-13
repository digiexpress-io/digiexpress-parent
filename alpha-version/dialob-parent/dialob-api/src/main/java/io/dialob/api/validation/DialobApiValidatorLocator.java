package io.dialob.api.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ServiceLoader;
import java.util.Set;

class DialobApiValidatorLocator {

  private static DialobApiValidator validator;

  private static DialobApiValidatorFactory validationApiDialobApiValidatorFactory = () -> instance -> {
    Validator v = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<Object>> constraintViolations = v.validate(instance);
    if (!constraintViolations.isEmpty()) {
      throw new ConstraintViolationException(constraintViolations);
    }
    return instance;
  };

  public synchronized static DialobApiValidator locate() {
    if (validator != null) {
      return validator;
    }
    ServiceLoader<DialobApiValidatorFactory> loader = ServiceLoader.load(DialobApiValidatorFactory.class);
    DialobApiValidatorFactory factory = validationApiDialobApiValidatorFactory;
    for (DialobApiValidatorFactory dialobApiValidatorFactory : loader) {
      factory = dialobApiValidatorFactory;
    }
    return factory.create();
  };

  public static DialobApiValidator getValidator() {
    if (validator == null) {
      validator = locate();
    }
    return validator;
  }
}
