package io.resys.limaone.spi.dialob;

import java.util.Objects;

import io.dialob.api.form.FormTag;
import io.dialob.api.form.ImmutableFormTag;
import io.resys.limaone.spi.dialob.FormDb.CreateFormTag;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateFormTagImpl implements CreateFormTag {
  private final FormDbProps db;
  
  private String formId;
  private String tagName;
  
  @Override
  public CreateFormTag formId(String formId) {
    this.formId = Objects.requireNonNull(formId, () -> "formId must be defined");
    return this;
  }
  
  @Override
  public CreateFormTag formVersion(String tagName) {
    this.tagName = Objects.requireNonNull(tagName, () -> "tagName must be defined");
    return this;
  }

  @Override
  public Uni<FormTag> build() {
    Objects.requireNonNull(formId, () -> "formId must be defined");
    Objects.requireNonNull(tagName, () -> "tagName must be defined");
    
    final var body = ImmutableFormTag.builder()
      .name(tagName)
      .formName(formId)
      .type(FormTag.Type.NORMAL)
      .build();
    
    return db.getClient()
        .httpQuery()
        .uri(uri -> uri.append("forms").append(formId).append("tags").append(tagName).build())
        .method(FormTag.class)
        .postOneObject(body)
        .onItem().transformToUni(ignore -> new FormTagQueryImpl(db).getOneTag(formId, tagName));
  }
}