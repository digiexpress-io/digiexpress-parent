package io.dialob.api.validation;

@FunctionalInterface
public interface DialobApiValidator {

  WithValidation validate(WithValidation instance);

}
