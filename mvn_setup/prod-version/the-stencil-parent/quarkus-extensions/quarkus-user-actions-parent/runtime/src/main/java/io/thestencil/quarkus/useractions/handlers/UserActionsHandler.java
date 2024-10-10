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
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.IAMClient;
import io.thestencil.iam.api.IAMClient.RepresentedCompany;
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
        final RepresentedPerson person = client.getUser().getRepresentedPerson();
        final RepresentedCompany company = client.getUser().getRepresentedCompany();
        if(person == null && company == null) {
          return null; // Nobody is represented
        }
        
        final var id = event.request().getHeader("cookie");
        final var query = person != null ? 
            iam.personRolesQuery().id(id).get() : 
            iam.companyRolesQuery().id(id).get();
        return query
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
      iam.userQuery().get().onItem().transformToUni(client -> {
        

        final var user = client.getUser();
        
        if(user.getRepresentedCompany() != null || user.getRepresentedPerson() != null) {
          final var id = event.request().getHeader("cookie");
          final var query = user.getRepresentedPerson() != null ? 
              iam.personRolesQuery().id(id).get() : 
              iam.companyRolesQuery().id(id).get();
          
          final Uni<AuthorizationAction> authorizations = query
              .onItem().transformToUni(roleData -> ctx.getClient()
                  .authorizationActionQuery()
                  .userRoles(roleData.getUserRoles().getRoles())
                  .get());
          
          final var personNames = user.getRepresentedPerson() == null ? null : getRepresentativeName(user.getRepresentedPerson().getName());
          
          final var workflows = ctx.getClient().queryUserAction()
            .userId(user.getRepresentedPerson() != null ? user.getRepresentedPerson().getPersonId() : user.getRepresentedCompany().getCompanyId())
            .userName(personNames != null ? personNames[1] + " " + personNames[0]: user.getRepresentedCompany().getName())
            .representativeUserName(getUsername(client.getUser()))
            .list().collect().asList();
          
          return Uni.combine().all().unis(workflows, authorizations)
          .asTuple().onItem().transform(tuple -> {
            final var validNames = tuple.getItem2().getAllowedProcessNames();
            log.debug("Allowed process names: {}" , validNames, tuple.getItem1().stream().map(wk -> wk.getId() + "/" + wk.getName() + "/").collect(Collectors.toList()));
            return tuple.getItem1().stream().filter(wk -> validNames.contains(wk.getName())).collect(Collectors.toList());
          });
        }
        return ctx.getClient().queryUserAction()
            .userId(user.getSsn())
            .userName(getUsername(user))
            .list().collect().asList();
      })
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
        final var inputContextId = event.request().getParam("inputContextId");
        final var inputParentContextId = event.request().getParam("inputParentContextId");
        
        final RepresentedPerson person = client.getUser().getRepresentedPerson();
        final RepresentedCompany company = client.getUser().getRepresentedCompany();
        if(person == null && company == null) {
          return createUserAction(
              ctx, actionId, client, actionLocale, inputContextId, inputParentContextId
           ); // Nobody is represented
        }
                
        final var id = event.request().getHeader("cookie");
        final var query = person != null ? 
            iam.personRolesQuery().id(id).get() : 
            iam.companyRolesQuery().id(id).get();
        
        final Uni<AuthorizationAction> authorizations = query
            .onItem().transformToUni(roleData -> ctx.getClient()
                .authorizationActionQuery()
                .userRoles(roleData.getUserRoles().getRoles())
                .get());
        return authorizations.onItem().transformToUni(auth -> {
          if(auth.getAllowedProcessNames().contains(actionId)) {
            return createUserAction(ctx, actionId, client, actionLocale, inputContextId, inputParentContextId); // User allowed
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
  
  private String[] getRepresentativeName(String name) {
    final var splitAt = name.indexOf(" ");
    if(splitAt <= 0) {
      return new String[] {" ", name.trim()};
    }
    return new String[] {name.substring(0, splitAt).trim(), name.substring(splitAt).trim()};
  }
  
  private Uni<UserAction> createUserAction(
      UserActionsContext ctx, String actionId, 
      UserQueryResult client, String clientLocale, 
      String inputContextId,
      String inputParentContextId
      ) {
	  final var user = client.getUser();
    final var person = user.getRepresentedPerson();
    final var company = user.getRepresentedCompany();
    final var create = ctx.getClient().createUserAction()
      .actionName(actionId)
      .protectionOrder(user.getProtectionOrder())
      .language(clientLocale);
    
    if(person != null) {
      final var representativeName = getRepresentativeName(person.getName());
      final var representativeFirstName = representativeName[1];  
      final var representativeLastName = representativeName[0];
      
      return create
        .inputParentContextId(inputParentContextId)
        .inputContextId(inputContextId)
        .userName(representativeFirstName, representativeLastName)
        .userId(person.getPersonId())
        .representative(user.getFirstName(), user.getLastName(), user.getSsn())
        .build();
    } else if(company != null) {
      return create
        .inputParentContextId(inputParentContextId)
        .inputContextId(inputContextId)
        .companyName(company.getName())
        .userId(company.getCompanyId())
        .representative(user.getFirstName(), user.getLastName(), user.getSsn())
        .build();
    }
	
    return create
      .userName(user.getFirstName(), user.getLastName())
      .email(user.getContact().getEmail())
      .address(user.getContact().getAddressValue())
      .userId(user.getSsn())
      .inputParentContextId(inputParentContextId)
      .inputContextId(inputContextId)
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
