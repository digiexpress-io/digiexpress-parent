package io.dialob.api.validation;

import org.immutables.value.Value;

@Value.Style(validationMethod = Value.Style.ValidationMethod.NONE)
public interface WithValidation<T extends WithValidation<T>> {

  static <T extends WithValidation> T validate(T instance) {
    return (T) DialobApiValidatorLocator.getValidator().validate(instance);
  }

  @Value.Check
  default T validate() {
    return (T) validate(this);
  }
}
