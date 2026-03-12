package io.resys.limaone.spi.dialob;

import java.util.Objects;
import java.util.Optional;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormPutResponse;
import io.resys.limaone.spi.dialob.FormDb.MergeForm;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
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
        .putOneObject(form, this::mapResult)
        .onItem()
        .invoke(updatedForm -> db.getCache().evictForm(form.getId()));
  }
  
  
  private Form mapResult(JsonObject result) {
    // does'nt serialize directly... map it manually
    final var resp = new FormPutResponse.Builder()
      .id(result.getString("id"))
      .rev(result.getString("rev"))
      .ok(result.getBoolean("ok"))
      .error(result.getString("error"))
      .build();
    if(Boolean.TRUE.equals(resp.ok())) {
      return form;
    }
    final var errors = Optional.ofNullable(result.getJsonArray("errors"))
        .map(e -> ", errors:\n" + e.encodePrettily())
        .orElse("");
    
    throw new DialobException("Failed to merge form, error msg: " + resp.getError() + errors);
  }
}