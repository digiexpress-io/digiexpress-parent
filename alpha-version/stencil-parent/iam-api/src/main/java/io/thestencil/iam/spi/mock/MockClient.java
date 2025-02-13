package io.thestencil.iam.spi.mock;

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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableUserAction;
import io.thestencil.iam.api.UserActionsClient.UserActionsClientConfig;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import io.thestencil.iam.spi.support.BuilderTemplate;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;


public class MockClient extends BuilderTemplate {
  
  private final UserActionsClientConfig config;
  private final String formId;
  
  public MockClient(WebClient client, UserActionsClientConfig config, String formId, String apiKey) {
    super(client, new RequestOptions()
        .setURI(config.getReview().getPath())
        .setHost(config.getReview().getHost())
        .addHeader("x-api-key", apiKey)
        );
    this.config = config;
    this.formId = formId;
  }
  
  public Uni<Buffer> fill(HttpMethod method, String normalisedPath, Buffer body) {
    final var options = options("");
    final String uri = "/session/dialob" + normalisedPath.substring(this.config.getFillPath().length());
    
    final var client = getClient()
        .requestAbs(method, "https://" + options.getHost() + uri)
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json");
    
   final Uni<HttpResponse<Buffer>> response;
   if(body == null) {
     response = client.send();
   } else {
     response = client.sendBuffer(body);
   }
   return response.onItem().transform(resp -> resp.body());
  }

  public Uni<Buffer> review(String normalisedPath) {
    return this.session(normalisedPath).onItem()
      .transformToUni(session -> formWithSession(session));
  }

  public Uni<Buffer> formWithSession(JsonObject session) {
    final var options = options("");
    final var formId = session.getJsonObject("metadata").getString("formId");
    final var uri = "/forms/" + formId;
    
    final Uni<HttpResponse<Buffer>> response = getClient()
        .getAbs("https://" + options.getHost() + "/" + options.getURI() + uri)
        .putHeaders(new io.vertx.mutiny.core.MultiMap(options.getHeaders()))
        .send();
   return response.onItem().transform(resp -> {
     if(resp.statusCode() != 200) {
       throw new RuntimeException("Can't create instance, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers());
     }
     final var form = resp.bodyAsJsonObject();
     final var formWithSession = new JsonObject();
     formWithSession.put("session", session);
     formWithSession.put("form", form);
     return Buffer.newInstance(formWithSession.toBuffer());
   });
  }
  
  public Uni<JsonObject> deleteQuestionnaire(String id) {
    final var options = options("");
    final String uri = "/questionnaires/" + id;
    
    final Uni<HttpResponse<Buffer>> response = getClient()
        .deleteAbs("https://" + options.getHost() + "/" + options.getURI() + uri)
        .putHeaders(new io.vertx.mutiny.core.MultiMap(options.getHeaders()))
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
        .send();
    
   return response.onItem().transform(resp -> {
     if(resp.statusCode() != 200) {
       throw new RuntimeException("Can't create instance, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers());
     }
     return resp.bodyAsJsonObject();
   });
  }
  
  public Uni<JsonObject> session(String normalisedPath) {
    final var options = options("");
    final String uri = "/questionnaires" + normalisedPath.substring(this.config.getReviewPath().length());
    
    final Uni<HttpResponse<Buffer>> response = getClient()
        .getAbs("https://" + options.getHost() + "/" + options.getURI() + uri)
        .putHeaders(new io.vertx.mutiny.core.MultiMap(options.getHeaders()))
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
        .send();
    
   return response.onItem().transform(resp -> {
     if(resp.statusCode() != 200) {
       throw new RuntimeException("Can't create instance, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers());
     }
     return resp.bodyAsJsonObject();
   });
  }
  
  
  public Uni<List<UserAction>> completed() {
    return super.get(getUri("/questionnaires?status=COMPLETED&formId=" + formId))
      .send().onItem().transform(resp -> {
        if (resp.statusCode() == 200) {
          return resp.bodyAsJsonArray().stream()
              .map(e -> toUserForm((JsonObject) e))
              .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
      });
  }

  public Uni<List<UserAction>> open() {
    return super.get(getUri("/questionnaires?status=OPEN&formId=" + formId))
      .send().onItem().transform(resp -> {
        if (resp.statusCode() == 200) {
          return resp.bodyAsJsonArray().stream()
              .map(e -> toUserForm((JsonObject) e))
              .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
      });
  }

  public Uni<UserAction> create() {
    final var meta = new HashMap<String, Object>();
    meta.put("formId", formId);
    meta.put("language", "en");
    
    final var form = new JsonObject()
      .put("metadata", new JsonObject(meta));
    
    final var options = options("/questionnaires");
    
    return this.getClient()
      .requestAbs(HttpMethod.POST, "https://" + options.getHost() + "/" + options.getURI())
      .putHeaders(new io.vertx.mutiny.core.MultiMap(options.getHeaders()))
      .putHeader("Accept", "application/json")
      .putHeader("Content-Type", "application/json")
      .sendBuffer(Buffer.newInstance(form.toBuffer())).onItem().transform(resp -> {
        if (resp.statusCode() == 201) {
          final var entity = resp.bodyAsJsonObject();
          return ImmutableUserAction.builder()
              .id("mock-id")
              .name("mock-test")
              .viewed(true)
              .messagesUri(config.getMessagesPath())
              .reviewUri(config.getReviewPath())
              .formUri(config.getFillPath())
              .formId(entity.getString("_id"))
              .formInProgress(true)
              .status("open")
              .build();
        }
        
        throw new RuntimeException("Can't create instance, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers());
      });
  }
  
  
  private UserAction toUserForm(JsonObject entity) {
    final var open = !entity.getJsonObject("metadata").getString("status").equals("COMPLETED");
    return ImmutableUserAction.builder()
        .id("mock-id")
        .name("mock-test")
        .status(open ? "open" : "waiting")
        .viewed(!open)
        .formId(entity.getString("id"))
        .messagesUri(config.getMessagesPath())
        .reviewUri(config.getReviewPath())
        .formUri(config.getFillPath())
        .formInProgress(open)
        .build();
  }

}
