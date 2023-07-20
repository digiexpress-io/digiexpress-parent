package io.dialob.api.form;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dialob.api.rest.Response;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
@JsonSerialize(as = ImmutableFormPutResponse.class)
@JsonDeserialize(as = ImmutableFormPutResponse.class)
@Gson.TypeAdapters
@JsonInclude(content = JsonInclude.Include.NON_NULL, value = JsonInclude.Include.NON_EMPTY)
@Value.Style(validationMethod = Value.Style.ValidationMethod.NONE, jdkOnly = true)
public interface FormPutResponse extends Response {

  String getId();

  String getRev();

  List<FormValidationError> getErrors();

  Form getForm();
}
