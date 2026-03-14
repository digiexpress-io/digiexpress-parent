package io.resys.limaone.spi.dialob.builders;

import java.util.Objects;
import java.util.Optional;

import io.dialob.api.form.FormPutResponse;
import io.dialob.api.form.FormTag;
import io.resys.limaone.spi.dialob.FormDb.CreateFormTag;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateFormTagImpl implements CreateFormTag {
  private final FormDbProps db;
  
  private String formName;
  private String tagName;
  
  @Override
  public CreateFormTag formName(String formName) {
    this.formName = Objects.requireNonNull(formName, () -> "formId must be defined");
    return this;
  }
  
  @Override
  public CreateFormTag formVersion(String tagName) {
    this.tagName = Objects.requireNonNull(tagName, () -> "tagName must be defined");
    return this;
  }

  @Override
  public Uni<FormTag> build() {
    Objects.requireNonNull(formName, () -> "formName must be defined");
    Objects.requireNonNull(tagName, () -> "tagName must be defined");
    
    final var body = new FormTag.Builder()
      .name(tagName)
      .formName(formName)
      .type(FormTag.Type.NORMAL)
      .build();
    
    return db.getFormHttp()
        .httpQuery()
        .uri(uri -> uri.append("forms").append(formName).append("tags").build())
        .method(FormTag.class)
        .postOneObject(body, this::mapResult)
        .onItem().transformToUni(ignore -> new FormTagQueryImpl(db).getOneTag(formName, tagName));
  }
  
  private String mapResult(JsonObject result) {
    
    // does'nt serialize directly... map it manually
    final var resp = new FormPutResponse.Builder()
      .id("")
      .rev("")
      .ok(result.getBoolean("ok"))
      .error(result.getString("error"))
      .build();
    if(Boolean.TRUE.equals(resp.ok())) {
      return "";
    }
    final var errors = Optional.ofNullable(result.getJsonArray("errors"))
        .map(e -> ", errors:\n" + e.encodePrettily())
        .orElse("");
    
    throw new DialobException("Failed to create form tag, error msg: " + resp.getError() + errors);
  }
}