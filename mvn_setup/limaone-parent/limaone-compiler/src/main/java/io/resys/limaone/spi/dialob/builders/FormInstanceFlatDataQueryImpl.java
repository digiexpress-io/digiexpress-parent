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

import java.time.Duration;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import io.resys.limaone.spi.dialob.FormDb.FormInstanceFlatData;
import io.resys.limaone.spi.dialob.FormDb.FormInstanceFlatData.Status;
import io.resys.limaone.spi.dialob.FormDb.FormInstanceFlatDataQuery;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.resys.limaone.spi.dialob.ImmutableFormInstanceFlatData;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FormInstanceFlatDataQueryImpl implements FormInstanceFlatDataQuery {
  private final FormDbProps db;
  private String formInstanceId;
  private String locale;
  private String timeZone;

  @Override
  public FormInstanceFlatDataQuery instanceId(String formInstanceId) {
    this.formInstanceId = formInstanceId;
    return this;
  }

  @Override
  public FormInstanceFlatDataQuery locale(String locale) {
    this.locale = locale;
    return this;
  }

  @Override
  public FormInstanceFlatDataQuery timeZone(String timeZone) {
    this.timeZone = timeZone;
    return this;
  }

  @Override
  public Uni<FormInstanceFlatData> getOne() {
    final var formInstanceId = this.formInstanceId;
    Objects.requireNonNull(formInstanceId, () -> "formInstanceId can't be null");
    if(FormFillBuilderImpl.isInvalidId(formInstanceId)) {
      return Uni.createFrom().item(ImmutableFormInstanceFlatData.builder()
          .id(formInstanceId)
          .status(Status.NOT_FOUND)
          .message("invalid form instance id: " + formInstanceId)
          .build());
    }
    return db.getFormHttp().httpQuery()
        .uri(uri -> uri
            .append("questionnaires").append(formInstanceId).append("session-state" + queryParams())
            .build())
        .method(String.class)
        .getOneAsRaw()
        .onItem().transform(raw -> (FormInstanceFlatData) ImmutableFormInstanceFlatData.builder()
            .id(formInstanceId)
            .status(Status.COMPLETED)
            .body(new JsonObject(raw.getBody()))
            .build())
        .onFailure().recoverWithItem(e -> fromFailure(formInstanceId, e));
  }

  @Override
  public FormInstanceFlatData getOneSync() {
    final var workerTimeout = Duration.ofMinutes(1);
    final var workerPool = Infrastructure.getDefaultWorkerPool();
    return this.getOne()
      .runSubscriptionOn(workerPool)
      .await().atMost(workerTimeout);
  }

  private String queryParams() {
    final var params = new StringBuilder();
    if(timeZone != null) {
      params.append("tz=").append(timeZone);
    }
    if(locale != null) {
      if(!params.isEmpty()) {
        params.append("&");
      }
      params.append("lang=").append(locale);
    }
    return params.isEmpty() ? "" : "?" + params;
  }

  private FormInstanceFlatData fromFailure(String formInstanceId, Throwable e) {
    if(e.getCause() instanceof HttpClientErrorException http) {
      if(http.getStatusCode().value() == HttpStatus.CONFLICT.value()) {
        return ImmutableFormInstanceFlatData.builder()
            .id(formInstanceId)
            .status(Status.NOT_COMPLETED)
            .message("form instance is not completed: " + formInstanceId)
            .build();
      }
      if(http.getStatusCode().value() == HttpStatus.NOT_FOUND.value() ||
         http.getStatusCode().value() == HttpStatus.METHOD_NOT_ALLOWED.value()) {
        return ImmutableFormInstanceFlatData.builder()
            .id(formInstanceId)
            .status(Status.NOT_FOUND)
            .message("form instance not found: " + formInstanceId)
            .build();
      }
    }
    return ImmutableFormInstanceFlatData.builder()
        .id(formInstanceId)
        .status(Status.ERROR)
        .message(e.getMessage())
        .build();
  }

}
