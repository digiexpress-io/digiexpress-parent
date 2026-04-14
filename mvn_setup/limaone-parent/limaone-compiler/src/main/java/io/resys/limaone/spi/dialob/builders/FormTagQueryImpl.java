package io.resys.limaone.spi.dialob.builders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.dialob.api.form.FormTag;
import io.resys.limaone.spi.dialob.FormDb.FormAndTag;
import io.resys.limaone.spi.dialob.FormDb.FormTagQuery;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class FormTagQueryImpl implements FormTagQuery {
  private final FormDbProps db;
  
  @Override
  public Multi<FormTag> findAll() {
    return db.getFormHttp()
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
    return db.getFormHttp()
        .httpQuery()
        .uri(uri -> uri.append("forms").append(formName).append("tags").append(tagName).build())
        .method(FormTag.class)
        .getOneObject()
        .onItem().invoke(tag -> db.getCache().putFormTag(formName, tagName, tag));
  }

  @Override
  public Multi<FormTag> findAll(String formName) {
    return db.getFormHttp()
        .httpQuery()
        .uri(uri -> uri.append("forms").append(formName).append("tags").build())
        .method(FormTag.class)
        .findAllObjects();
  }

  @Override
  public Multi<FormAndTag> flatAll() {
    
    return Uni.combine().all().unis(
        new FormMetaQueryImpl(db).findAll().collect().asList(),
        findAll().collect().asList()
    ).asTuple().onItem().transformToMulti(tuple -> {

      final Map<String, @NotNull String> formLabels = new HashMap<>(); 
      
      for(final var item : tuple.getItem1()) {
        if(item.getId() == null) {
          continue;
        }
        formLabels.put(item.getId(), item.getMetadata().getLabel());
      }

      
      
      final var tags = new ArrayList<FormAndTag>();
      for (var formTag : tuple.getItem2()) {
        tags.add(ImmutableFormAndTag.builder()
            .formName(formTag.getFormName())
            .formLabel(formLabels.get(formTag.getFormName()))
            .tagFormId(formTag.getFormId())
            .tagName(formTag.getName())
            .build());
      }
      
      return Multi.createFrom().items(tags.stream());
    });
  }
  
  @Value @Builder
  private static class ImmutableFormAndTag implements FormAndTag {
    String formLabel;
    String formName;
    String tagFormId;
    String tagName;
  }
  
}
