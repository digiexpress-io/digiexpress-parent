package io.resys.limaone.spi.dialob.builders;

import java.util.Objects;

import io.dialob.api.form.Form;
import io.resys.limaone.spi.dialob.FormDb.CreateForm;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateFormImpl implements CreateForm {
  private final FormDbProps db;
  
  private Form form;
  
  @Override
  public CreateForm props(Form form) {
    this.form = Objects.requireNonNull(form, () -> "form must be defined");
    return this;
  }

  @Override
  public Uni<Form> build() {
    Objects.requireNonNull(form, () -> "form must be defined");
    return db.getFormHttp()
        .httpQuery()
        .uri(uri -> uri.append("forms").build()).method(Form.class)
        .postOneObject(form);
  }

}
