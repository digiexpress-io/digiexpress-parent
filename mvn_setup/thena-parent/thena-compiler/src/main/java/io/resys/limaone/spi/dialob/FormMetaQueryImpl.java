package io.resys.limaone.spi.dialob;

import io.resys.limaone.spi.dialob.FormDb.FormMetaQuery;
import io.resys.limaone.spi.dialob.FormDb.FormMetadata;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FormMetaQueryImpl implements FormMetaQuery {
  private final FormDbProps db;
  
  @Override
  public Multi<FormMetadata> findAll() {
    return db.getClient()
      .httpQuery()
      .uri(uri -> uri.append("forms").build())
      .method(FormMetadata.class)
      .findAllObjects();
  }
}