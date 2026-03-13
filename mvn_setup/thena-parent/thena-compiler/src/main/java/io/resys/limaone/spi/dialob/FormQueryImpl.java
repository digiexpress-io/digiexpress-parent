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
      final var cached = db.getCache().getForm(formId);
      if (cached.isPresent()) {
        return Uni.createFrom().item(cached);
      }
      return db.getFormHttp()
        .httpQuery()
        .uri(uri -> uri.append("forms").append(formId).build())
        .method(Form.class)
        .findOneObject()
        .onItem().invoke(formOpt -> {
          if (formOpt.isPresent()) {
            db.getCache().putForm(formId, formOpt.get());
          }
        });
    }
    
    Objects.requireNonNull(formName, () -> "formName must be defined");
    Objects.requireNonNull(formVersion, () -> "formVersion must be defined");
    
    final var cachedTag = db.getCache().getFormTag(formName, formVersion);
    if (cachedTag.isPresent()) {
      final var resolvedFormId = cachedTag.get().getFormId();
      final var cachedForm = db.getCache().getForm(resolvedFormId);
      if (cachedForm.isPresent()) {
        return Uni.createFrom().item(cachedForm);
      }
    }
    
    return db.getFormHttp()
      .httpQuery()
      .uri(uri -> uri.append("forms").append(formName).append("tags").append(formVersion).build())
      .method(FormTag.class)
      .findOneObject()
      .onItem().transformToUni(tagOpt -> {
        if (tagOpt.isEmpty()) {
          return Uni.createFrom().item(Optional.<Form>empty());
        }
        final var tag = tagOpt.get();
        db.getCache().putFormTag(formName, formVersion, tag);
        
        final var resolvedFormId = tag.getFormId();
        final var cachedForm = db.getCache().getForm(resolvedFormId);
        if (cachedForm.isPresent()) {
          return Uni.createFrom().item(cachedForm);
        }
        
        return db.getFormHttp()
          .httpQuery()
          .uri(uri -> uri.append("forms").append(resolvedFormId).build())
          .method(Form.class)
          .findOneObject()
          .onItem().invoke(formOpt -> {
            if (formOpt.isPresent()) {
              db.getCache().putForm(resolvedFormId, formOpt.get());
            }
          });
      });
  }
}