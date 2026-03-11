package io.resys.limaone.spi.dialob;

import java.util.Objects;
import java.util.Optional;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;
import io.resys.limaone.spi.dialob.FormDb.FormQuery;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FormQueryImpl implements FormQuery {
  private final FormDbProps db;
  
  private String formId;
  private String formName;
  private String formVersion;
  
  @Override
  public FormQuery formId(String formId) {
    this.formId = Objects.requireNonNull(formId, () -> "formId must be defined");
    return this;
  }
  
  @Override
  public FormQuery formTag(String formName, String formVersion) {
    this.formName = Objects.requireNonNull(formName, () -> "formName must be defined");
    this.formVersion = Objects.requireNonNull(formVersion, () -> "formVersion must be defined");
    return this;
  }
  
  @Override
  public Uni<Optional<Form>> findOne() {
    if (formId != null) {
      return db.getClient()
        .httpQuery()
        .uri(uri -> uri.append("forms").append(formId).build())
        .method(Form.class)
        .findOneObject();
    }
    
    Objects.requireNonNull(formName, () -> "formName must be defined");
    Objects.requireNonNull(formVersion, () -> "formVersion must be defined");
    return db.getClient()
      .httpQuery()
      .uri(uri -> uri.append("forms").append(formName).append("tags").append(formVersion).build())
      .method(FormTag.class)
      .findOneObject()
      .onItem().transformToUni(tagOpt -> {
        if (tagOpt.isEmpty()) {
          return Uni.createFrom().item(Optional.<Form>empty());
        }
        return db.getClient()
          .httpQuery()
          .uri(uri -> uri.append("forms").append(tagOpt.get().getFormId()).build())
          .method(Form.class)
          .findOneObject();
      });
    
  }
}