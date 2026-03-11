package io.resys.limaone.spi.dialob;

import java.util.Objects;

import io.dialob.api.form.Form;
import io.resys.limaone.spi.dialob.FormDb.MergeForm;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MergeFormImpl implements MergeForm {
  private final FormDbProps db;
  
  private Form form;
  
  @Override
  public MergeForm props(Form form) {
    this.form = Objects.requireNonNull(form, () -> "form must be defined");
    return this;
  }

  @Override
  public Uni<Form> build() {
    Objects.requireNonNull(form, () -> "form must be defined");
    return db.getClient()
        .httpQuery()
        .uri(uri -> uri.append("forms").append(form.getId()).build())
        .method(Form.class)
        .putOneObject(form)
        .onItem().invoke(updatedForm -> db.getCache().putForm(form.getId(), updatedForm));
  }
}