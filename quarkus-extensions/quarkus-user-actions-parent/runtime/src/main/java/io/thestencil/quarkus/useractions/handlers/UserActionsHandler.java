package io.thestencil.quarkus.useractions.handlers;

/*-
 * #%L
 * quarkus-stencil-user-actions
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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.IAMClient;
import io.thestencil.iam.api.IAMClient.RepresentedPerson;
import io.thestencil.iam.api.IAMClient.UserQueryResult;
import io.thestencil.iam.api.UserActionsClient.AuthorizationAction;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import io.thestencil.quarkus.useractions.UserActionsContext;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class UserActionsHandler extends UserActionsTemplate {

  private final ObjectMapper mapper;
  
  public UserActionsHandler(
      CurrentIdentityAssociation currentIdentityAssociation,
      CurrentVertxRequest currentVertxRequest, 
      ObjectMapper mapper) {
  
    super(currentIdentityAssociation, currentVertxRequest);
    this.mapper = mapper;
  }
  
  @Override
  protected void handleResource(RoutingContext event, HttpServerResponse response, UserActionsContext ctx, IAMClient iam) {
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

    final var path = event.normalizedPath();
    
    if(path.startsWith(ctx.getMessagesPath())) {
      String actionId = event.request().getParam("id");

      
      iam.userQuery().get().onItem().transformToUni(client -> {

        if(event.request().method() == HttpMethod.POST) {
          final var body = new JsonObject(event.body().buffer());
          
          return ctx.getClient().replyTo()
            .processId(actionId)
            .userId(client.getUser().getSsn())
            .userName(getUsername(client.getUser()))
            .replyToId(body.getString("replyToId"))
            .text(body.getString("text"))
            .build().onItem().transform(data -> toBuffer(data));
        }
        
        return ctx.getClient().markUser()
          .processId(actionId)
          .userId(client.getUser().getSsn())
          .userName(getUsername(client.getUser()))
          .build().onItem().transform(data -> toBuffer(data));
      })
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));
      
      
    } else if(path.startsWith(ctx.getFillPath())) {
      ctx.getClient().fill()
        .path(path)
        .method(event.request().method())
        .body(body(event))
        .build()          
        .onFailure().invoke(e -> catch422(e, ctx, response))
        .subscribe().with(data -> response.end(data.toString()));
      
    } else if(path.startsWith(ctx.getReviewPath())) {
      ctx.getClient().review().path(path).build()
        .onFailure().invoke(e -> catch422(e, ctx, response))
        .subscribe().with(data -> response.end(data.toString()));

    } else if(path.startsWith(ctx.getAttachmentsPath())) {
      handleUserActionAttachments(event, response, ctx, iam);
    } else if(path.startsWith(ctx.getAuthorizationsPath())) {
      handleAuthorizations(event, response, ctx, iam);
    } else {
      handleUserAction(event, response, ctx, iam);
    }
  }
  
  private Buffer body(RoutingContext rc) {
    if(rc.body().buffer() == null) {
      return null;
    } else {
      return Buffer.newInstance(rc.body().buffer());
    }
  }

  @SuppressWarnings("unchecked")
  private void handleUserActionAttachments(RoutingContext event, HttpServerResponse response, UserActionsContext ctx, IAMClient iam) {
    
    
    String actionId = event.request().getParam("actionId");
    String attachmentId = event.request().getParam("attachmentId");

    if(actionId != null && event.request().method() == HttpMethod.POST) {
      final List<Map<String, String>> files = event.body().asJsonArray().getList();      
      iam.userQuery().get().onItem().transformToUni(client ->
        ctx.getClient().attachment()
        .userId(client.getUser().getSsn())
        .userName(getUsername(client.getUser()))
        .processId(actionId)
        .call(b -> files.stream()
            .forEach(item -> b.data(
                item.get("fileName"), 
                item.get("fileType")
                )))
        .build().collect().asList()
      )
      .onItem().transform(data -> toBuffer(data))
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));    
    } else if(actionId != null && attachmentId != null && event.request().method() == HttpMethod.GET) {
        
      iam.userQuery().get().onItem().transformToUni(client ->
        
      ctx.getClient().attachmentDownload()
        .userId(client.getUser().getSsn())
        .userName(getUsername(client.getUser()))
        .processId(actionId)
        .attachmentId(attachmentId)
        .build()
      )
      .onItem().transform(data -> toBuffer(data))
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));    
      
    } else {
      catch404("unknown user action attachment", ctx, response);      
    }
  }
  
  private void handleAuthorizations(RoutingContext event, HttpServerResponse response, UserActionsContext ctx, IAMClient iam) {
    

   if(event.request().method() == HttpMethod.GET) {
        
      iam.userQuery().get().onItem().transformToUni(client -> {
        final RepresentedPerson rep = client.getUser().getRepresentedPerson();
        if(rep == null) {
          return null; // Nobody is represented
        }
        return iam
            .userRolesQuery().id(event.request().getHeader("cookie")).get()
            .onItem().transformToUni(roleData -> ctx.getClient()
                .authorizationActionQuery()
                .userRoles(roleData.getUserRoles().getRoles())
                .get());
      })
      .onItem().transform(data -> toBuffer(data))
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));    
      
    } else {
      catch404("unknown user action attachment", ctx, response);      
    }
  }
  
  
  private void handleUserAction(RoutingContext event, HttpServerResponse response, UserActionsContext ctx, IAMClient iam) {
    String actionId = event.request().getParam("id");
    String actionLocale = event.request().getParam("locale");

    if(actionId == null) {
      iam.userQuery().get().onItem().transformToUni(client -> 
        ctx.getClient().queryUserAction()
        .userId(client.getUser().getSsn())
        .userName(getUsername(client.getUser()))
        .list().collect().asList()
      )
      .onItem().transform(data -> toBuffer(data))
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));    
    } else if(event.request().method() == HttpMethod.DELETE) {
      iam.userQuery().get().onItem().transformToUni(client ->
      
        ctx.getClient().cancelUserAction()
        .processId(actionId)
        .userId(client.getUser().getSsn())
        .userName(getUsername(client.getUser()))
        .build()
      )
      .onItem().transform(data -> toBuffer(data))
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));

    } else if(event.request().method() == HttpMethod.GET) {
      iam.userQuery().get().onItem().transformToUni(client -> {
        
        final RepresentedPerson rep = client.getUser().getRepresentedPerson();
        if(rep == null) {
          return createUserAction(ctx, actionId, client, actionLocale); // Nobody is represented
        }
        final Uni<AuthorizationAction> authorizations = iam
            .userRolesQuery().id(event.request().getHeader("cookie")).get()
            .onItem().transformToUni(roleData -> ctx.getClient()
                .authorizationActionQuery()
                .userRoles(roleData.getUserRoles().getRoles())
                .get());
        return authorizations.onItem().transformToUni(auth -> {
          if(auth.getAllowedProcessNames().contains(actionId)) {
            return createUserAction(ctx, actionId, client, actionLocale); // User allowed
          }
          
          log.error("User blocked from accessing process: {} because they are not authorized!", actionId);
          return Uni.createFrom().nullItem(); 
        });
      })
      .onItem().transform((UserAction data) -> toBuffer(data))
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));
    } else {
      catch404("unknown user action", ctx, response);
    }  
  }
  
  
  private Uni<UserAction> createUserAction(UserActionsContext ctx, String actionId, UserQueryResult client, String clientLocale) {
    
    return ctx.getClient().createUserAction()
      .actionName(actionId)
      .protectionOrder(client.getUser().getProtectionOrder())
      .userName(client.getUser().getFirstName(), client.getUser().getLastName())
      .language(clientLocale)
      .email(client.getUser().getContact().getEmail())
      .address(client.getUser().getContact().getAddressValue())
      .userId(client.getUser().getSsn())
      .build();
  }
  
  public io.vertx.core.buffer.Buffer toBuffer(Object object) {
    try {
      return io.vertx.core.buffer.Buffer.buffer(mapper.writeValueAsBytes(object));
    } catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
    }
  }
}
