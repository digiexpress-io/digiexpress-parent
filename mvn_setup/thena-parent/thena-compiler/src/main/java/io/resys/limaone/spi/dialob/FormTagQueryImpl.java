package io.resys.limaone.spi.dialob;

import io.dialob.api.form.FormTag;
import io.resys.limaone.spi.dialob.FormDb.FormTagQuery;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FormTagQueryImpl implements FormTagQuery {
  private final FormDbProps db;
  
  @Override
  public Multi<FormTag> findAll() {
    return db.getClient()
      .httpQuery()
      .uri(uri -> uri.append("tags").build())
      .method(FormTag.class)
      .findAllObjects();
  }

  @Override
  public Uni<FormTag> getOneTag(String formName, String tagName) {
    final var cached = db.getCache().getFormTag(formName, tagName);
    if (cached.isPresent()) {
      return Uni.createFrom().item(cached.get());
    }
    return db.getClient()
        .httpQuery()
        .uri(uri -> uri.append("forms").append(formName).append("tags").append(tagName).build())
        .method(FormTag.class)
        .getOneObject()
        .onItem().invoke(tag -> db.getCache().putFormTag(formName, tagName, tag));
  }

  @Override
  public Multi<FormTag> findAll(String formName) {
    return db.getClient()
        .httpQuery()
        .uri(uri -> uri.append("forms").append(formName).append("tags").build())
        .method(FormTag.class)
        .findAllObjects();
  }
}