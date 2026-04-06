package io.resys.limaone.spi.dialob.builders;

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

import java.util.Objects;
import java.util.Optional;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormTag;
import io.resys.limaone.spi.dialob.FormDb.FormQuery;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.resys.limaone.spi.http.HttpClient.AnyProxy;
import io.smallrye.mutiny.Multi;
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

  @Override
  public Multi<Form> findAll() {
    return db.getFormHttp()
        .httpQuery()
        .uri(uri -> uri.append("forms").build())
        .method(Form.class)
        .findAllObjects()
        .onItem().invoke(formOpt -> {
          db.getCache().putForm(formOpt.getId(), formOpt);
        });
  }

  @Override
  public AnyProxy proxyAnything() {
    return db.getFormHttp().anyProxy();
  }
}
