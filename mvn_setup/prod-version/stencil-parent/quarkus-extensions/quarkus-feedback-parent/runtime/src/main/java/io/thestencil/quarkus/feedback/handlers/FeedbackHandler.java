package io.thestencil.quarkus.feedback.handlers;

/*-
 * #%L
 * quarkus-stencil-feedback
 * %%
 * Copyright (C) 2021 - 2024 Copyright 2021 ReSys OÜ
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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import io.thestencil.quarkus.feedback.FeedbackContext;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.EncodeException;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.buffer.Buffer;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FeedbackHandler extends FeedbackTemplate {

  private final ObjectMapper mapper;
  
  public FeedbackHandler(
      CurrentVertxRequest currentVertxRequest, 
      ObjectMapper mapper) {
  
    super(currentVertxRequest);
    this.mapper = mapper;
  }
  
  @Override
  protected void handleResource(RoutingContext event, HttpServerResponse response, FeedbackContext ctx) {
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

    final var path = event.normalizedPath();
    
     
    if(path.startsWith(ctx.getAllowedPath())) {
      response.end(toBuffer(ctx.getAllowed()));
      return;
    }
    
 // fill
    if(path.startsWith(ctx.getFillPath())) {
    	ctx.getClient().fill()
        .path(path)
        .method(event.request().method())
        .body(body(event))
        .build()          
        .onFailure().invoke(e -> catch422(e, ctx, response))
        .subscribe().with(data -> response.end(data.toString()));
      return;
     }
    
    final var actionId = event.request().getParam("id");
    final var actionLocale = event.request().getParam("locale");
    

    // cancel fill
    if(event.request().method() == HttpMethod.DELETE) {
      
      ctx.getClient().cancelUserAction()
      .processId(actionId)
      .userId(ctx.getUserId())
      .userName(ctx.getUserName())
      .build()
      
      .onItem().transform(data -> toBuffer(data))
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));

    // start new fill
    } else if(event.request().method() == HttpMethod.GET) {
      

      if(!ctx.getAllowed().contains(actionId)) {
        catch403("actionId: " + actionId + ", not allowed!, Allowed: " + ctx.getAllowed() + "!", ctx, response);
        return; 
      }
      
      final var inputContextId = event.request().getParam("inputContextId");
      final var inputParentContextId = event.request().getParam("inputParentContextId");
      
      createUserAction(
          ctx, actionId, actionLocale, inputContextId, inputParentContextId
       )
      .onItem().transform((UserAction data) -> toBuffer(data))
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));
    } else {
      catch404("unknown user action", ctx, response);
    }  
  }
  
  
  private Uni<UserAction> createUserAction(
      FeedbackContext ctx, 
      String actionId, 
      String clientLocale, 
      String inputContextId,
      String inputParentContextId
      ) {
    

    final var create = ctx.getClient().createUserAction()
      .actionName(actionId)
      .protectionOrder(false)
      .language(clientLocale);
    	
    return create
      .userName(ctx.getFirstName(), ctx.getLastName())
      .email(ctx.getEmail())
      .address(ctx.getAddress())
      .userId(ctx.getUserId())
      .inputParentContextId(inputParentContextId)
      .inputContextId(inputContextId)
      .build();
  }
  
  
 
  
  private Buffer body(RoutingContext rc) {
    if(rc.body().buffer() == null) {
      return null;
    } else {
      return Buffer.newInstance(rc.body().buffer());
    }
  }


  public io.vertx.core.buffer.Buffer toBuffer(Object object) {
    try {
      return io.vertx.core.buffer.Buffer.buffer(mapper.writeValueAsBytes(object));
    } catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
    }
  }
}
