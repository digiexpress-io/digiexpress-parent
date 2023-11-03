package io.dialob.api.form;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Optional;

@Value.Immutable
@JsonSerialize(as = ImmutableFormValidationError.class)
@JsonDeserialize(as = ImmutableFormValidationError.class)
@Gson.TypeAdapters
@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
@Value.Style(validationMethod = Value.Style.ValidationMethod.NONE, jdkOnly = true)
public interface FormValidationError extends Serializable {

  enum Level {
    INFO,
    WARNING,
    ERROR,
    FATAL
  }

  enum Type {
    VISIBILITY,
    VALIDATION,
    REQUIREMENT,
    VARIABLE,
    GENERAL,
    CLASSNAME,
    @Deprecated // Unused
    VALUE_ENTRY,
    VALUESET,
    VALUESET_ENTRY,
    CANADDROW,
    CANREMOVEROW
  }

  String getItemId();

  String getMessage();

  @Value.Default
  default Level getLevel() {
    return Level.ERROR;
  }

  Type getType();

  Optional<String> getExpression();

  Optional<Integer> getStartIndex();

  Optional<Integer> getEndIndex();

  Optional<Integer> getIndex();

}
