package io.dialob.api.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableResponse.class)
@JsonDeserialize(as = ImmutableResponse.class)
@Gson.TypeAdapters
@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
@Value.Style(validationMethod = Value.Style.ValidationMethod.NONE, jdkOnly = true)
public interface Response extends ResponseStatus {

  String getError();

  String getReason();

}
