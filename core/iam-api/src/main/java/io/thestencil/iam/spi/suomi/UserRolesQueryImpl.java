package io.thestencil.iam.spi.suomi;

/*-
 * #%L
 * stencil-iam-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.stream.Collectors;

import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.IAMClient;
import io.thestencil.iam.api.IAMClient.IAMClientConfig;
import io.thestencil.iam.api.IAMClient.UserRolesQuery;
import io.thestencil.iam.api.IAMClient.UserRolesResult;
import io.thestencil.iam.api.ImmutableUserRoles;
import io.thestencil.iam.api.ImmutableUserRolesPrincipal;
import io.thestencil.iam.api.ImmutableUserRolesResult;
import io.thestencil.iam.spi.support.BuilderTemplate;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserRolesQueryImpl extends BuilderTemplate implements UserRolesQuery {
  private final IAMClientConfig config;
  private String id;
  private boolean isPersonRoles;
  public UserRolesQueryImpl(IAMClientConfig config, RequestOptions init, boolean isPersonRoles) {
    super(config.getWebClient(), init);
    this.config = config;
    this.isPersonRoles = isPersonRoles;
  }
  @Override
  public UserRolesQuery id(String id) {
    this.id = id;
    return this;
  }
  @Override
  public Uni<UserRolesResult> get() {
    return new UserQuerySuomi(config.getToken()).get()
    .onItem().transformToUni(user -> {
      if(user.getType() == IAMClient.ResultType.ANONYMOUS) {
        return Uni.createFrom().item(ImmutableUserRolesResult.builder().type(user.getType()).build());
      }
      final var uri = getUri("");
      if(log.isDebugEnabled()) {
        log.debug("USER ROLES: Uri = {}, Cookie = {}", uri, id);
      }
      return super.get(uri)
        .putHeader("cookie", id)
        .send().onItem()
        .transform(this::map);
    });
  }
  
  private UserRolesResult map(HttpResponse<?> resp) {
    if (resp.statusCode() != 200) {
      String error = "Can't create response, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
      log.error("USER ROLES: Error: {} body: {}", error, resp.bodyAsString());
      return ImmutableUserRolesResult.builder().type(IAMClient.ResultType.ERROR).build();
    }

    final JsonObject body = resp.bodyAsJsonObject();
    if(body.isEmpty()) {
      return ImmutableUserRolesResult.builder().type(IAMClient.ResultType.EMPTY).build();
    }

    final var jsonRoles = body.getJsonArray("roles");
    final var roles = jsonRoles.stream().map(data -> (String) data).collect(Collectors.toList());
    
    
    final ImmutableUserRolesPrincipal principal;
    if(isPersonRoles) {
      final var jsonPrincipal = body.getJsonObject("principal");
      principal = jsonPrincipal == null ? null : ImmutableUserRolesPrincipal.builder()
          .name(jsonPrincipal.getString("name"))
          .identifier(jsonPrincipal.getString("personId"))
          .build();
    } else {
      final var jsonName = body.getString("name");
      final var jsonIdentifier = body.getString("identifier");
      
      principal = jsonIdentifier == null ? null : ImmutableUserRolesPrincipal.builder()
          .name(jsonName)
          .identifier(jsonIdentifier)
          .build(); 
    }
    
    return ImmutableUserRolesResult.builder().type(IAMClient.ResultType.OK)
        .userRoles(ImmutableUserRoles.builder()
            .roles(roles)
            .principal(principal)
            .build())
        .build();
  }
}
