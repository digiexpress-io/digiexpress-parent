package io.thestencil.iam.spi.integrations;

import org.eclipse.microprofile.jwt.JsonWebToken;

/*-
 * #%L
 * iam-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.UserActionsClient.ReviewBuilder;
import io.thestencil.iam.api.UserActionsClient.UserActionsClientConfig;
import io.thestencil.iam.spi.support.BuilderTemplate;
import io.thestencil.iam.spi.support.PortalAssert;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;

public class DefaultReviewBuilder extends BuilderTemplate implements ReviewBuilder {

  private final UserActionsClientConfig config;
  private String path;
    
  public DefaultReviewBuilder(RequestOptions init, UserActionsClientConfig config, JsonWebToken idToken) {
    super(config.getWebClient(), init, idToken);
    this.config = config;
  }
  @Override
  public ReviewBuilder path(String path) {
    this.path = path;
    return this;
  }
  @Override
  public Uni<Buffer> build() {
    PortalAssert.notNull(path, () -> "path must be defined!");
    
    return super.get(getUri("/questionnaires" + path())).send()
        .onItem().transformToUni(resp -> formWithSession(resp.bodyAsJsonObject()));
  }

  
  public Uni<Buffer> formWithSession(JsonObject session) {
    final var formId = session.getJsonObject("metadata").getString("formId");
    final var uri = "/forms/" + formId;

    final Uni<HttpResponse<Buffer>> response = super.get(getUri(uri)).send();
    return response.onItem().transform(resp -> {
      final var form = resp.bodyAsJsonObject();
      final var formWithSession = new JsonObject();
      formWithSession.put("session", session);
      formWithSession.put("form", form);
      return Buffer.newInstance(formWithSession.toBuffer());
    });
  }

  private String path() {
    return path.substring(config.getReviewPath().length());
  }
}
