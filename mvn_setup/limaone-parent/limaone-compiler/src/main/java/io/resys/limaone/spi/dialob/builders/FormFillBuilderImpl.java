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
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.resys.limaone.spi.dialob.FormDb.FormFillBuilder;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.resys.limaone.spi.http.HttpClient.RawResponse;
import io.resys.limaone.spi.http.HttpClientImpl.RawResponseImpl;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FormFillBuilderImpl implements FormFillBuilder {
  private final FormDbProps db;
  
  private Function<String, Uni<?>> callback;
  private String formInstanceId;
  private String body;

  @Override
  public FormFillBuilder formInstanceId(String formInstanceId) {
    this.formInstanceId = Objects.requireNonNull(formInstanceId, () -> "formInstanceId can't be null");;
    return this;
  }
  @Override
  public FormFillBuilder actions(String body) {
    this.body = body;
    return this;
  }
  @Override
  public FormFillBuilder onCompletion(Function<String, Uni<?>> callback) {
    this.callback = Objects.requireNonNull(callback, () -> "callback can't be null");
    return this;
  }

  @Override
  public Uni<RawResponse> build() {
    Objects.requireNonNull(formInstanceId, () -> "formInstanceId can't be null");;
    if(isInvalidId(formInstanceId)) {
      return Uni.createFrom().item(new RawResponseImpl(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
    }
    if(body == null) {
      return db.getQuestionnaireHttp().httpQuery()
          .uri(uri -> uri.append(formInstanceId).build())
          .method(String.class)
          .getOneAsRaw()
          .call(this::determineCallback);
    }
    
    return db.getQuestionnaireHttp().httpQuery()
      .uri(uri -> uri.append(formInstanceId).build())
      .method(String.class)
      .postOneAsRaw(body)
      .call(this::determineCallback);
  }
  
  private Uni<Void> determineCallback(RawResponse resp) {    
    if (callback != null) {
      return callback.apply(resp.getBody())
          .onItem().transformToUni(ignore -> Uni.createFrom().voidItem()); // or whatever Uni you want to pass
    }
    return Uni.createFrom().voidItem();
  }
  
  
  public static boolean isInvalidId(String sessionId) {
    return sessionId == null || !sessionId.matches("[a-fA-F0-9-_]+");
  }
}
