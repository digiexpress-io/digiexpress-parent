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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.resys.limaone.spi.dialob.FormDb.FormFillQuery;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.resys.limaone.spi.http.HttpClient.RawResponse;
import io.resys.limaone.spi.http.HttpClientImpl.RawResponseImpl;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FormFillQueryImpl implements FormFillQuery {
  private final FormDbProps db;
  
  @Override
  public Uni<RawResponse> getOne(String formInstanceId) {
    Objects.requireNonNull(formInstanceId, () -> "formInstanceId can't be null");;
    if(FormFillBuilderImpl.isInvalidId(formInstanceId)) {
      return Uni.createFrom().item(new RawResponseImpl(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
    }
    return db.getQuestionnaireHttp().httpQuery()
        .uri(uri -> uri.append(formInstanceId).build())
        .method(String.class)
        .getOneAsRaw();
  }

}
