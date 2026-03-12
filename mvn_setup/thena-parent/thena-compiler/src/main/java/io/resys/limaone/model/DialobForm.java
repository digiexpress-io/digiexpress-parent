package io.resys.limaone.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.form.Form;
import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;


@JsonSerialize(as = ImmutableDialobForm.class)
@JsonDeserialize(as = ImmutableDialobForm.class)
@Value.Immutable
public interface DialobForm extends Body {
  String getFormName();
  String getFormTagName();
  Form getForm();
  
  default BodyType getBodyType() {
    return BodyType.DIALOB_FORM;
  }
}
