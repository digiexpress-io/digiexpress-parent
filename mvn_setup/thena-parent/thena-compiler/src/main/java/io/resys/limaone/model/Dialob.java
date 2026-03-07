package io.resys.limaone.model;

import io.dialob.api.form.Form;
import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;

public interface Dialob extends Body {
  Form getForm();
  
  
  default BodyType getBodyType() {
    return BodyType.DIALOB;
  }
}
