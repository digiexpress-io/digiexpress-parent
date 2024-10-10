package io.resys.hdes.client.spi.web;

/*-
 * #%L
 * hdes-client
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.resys.hdes.client.api.HdesComposer.CopyAs;
import io.resys.hdes.client.api.HdesComposer.CreateEntity;
import io.resys.hdes.client.api.HdesComposer.DebugRequest;
import io.resys.hdes.client.api.HdesComposer.UpdateEntity;
import io.resys.hdes.client.api.ast.AstTag;
import io.smallrye.mutiny.Uni;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class HdesComposerRouter extends HdesRouterTemplate {

  public HdesComposerRouter(CurrentIdentityAssociation currentIdentityAssociation, CurrentVertxRequest currentVertxRequest) {
    super(currentIdentityAssociation, currentVertxRequest);
  }

  @Override
  protected void doRoute(RoutingContext event, HttpServerResponse response, HdesWebContext ctx, ObjectMapper objectMapper) {
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
    final var path = getPath(event);
    
    if(path.endsWith(ctx.getConfig().getServicePath())) {
    	doService(event, response, ctx, objectMapper);
    } else if (path.startsWith(ctx.getConfig().getCommandsPath())) {
      doCommands(event, response, ctx, objectMapper);
    } else if (path.startsWith(ctx.getConfig().getCopyasPath())) {
      doCopyas(event, response, ctx, objectMapper);
    } else if(path.startsWith(ctx.getConfig().getDebugsPath())){
      doDebugs(event, response, ctx, objectMapper);
    } else if(path.startsWith(ctx.getConfig().getExportsPath())) {
      doExports(event, response, ctx, objectMapper);
    } else if(path.startsWith(ctx.getConfig().getHistoryPath())) {
      doHistory(event, response, ctx, objectMapper);
    } else if(path.startsWith(ctx.getConfig().getImportsPath())) {
      doImports(event, response, ctx, objectMapper);
    } else if(path.startsWith(ctx.getConfig().getModelsPath())) {
      doModels(event, response, ctx, objectMapper);
    } else if(path.startsWith(ctx.getConfig().getResourcesPath())) {
      doResources(event, response, ctx, objectMapper);
    } else {
      HdesStatusCodes.catch404("unsupported hdes composer action", response);
    }
  }
  
  public String getPath(RoutingContext event) {
    final var path = event.normalizedPath();
    
    return path.endsWith("/") ? path.substring(0, path.length() -1) : path;
  }
  
  public void doService(RoutingContext event, HttpServerResponse response, HdesWebContext ctx, ObjectMapper objectMapper) {
	  final var client = ctx.getClient();
	  if(event.request().method() == HttpMethod.GET) { 
	    final var index = Map.of(
          "headName", client.store().getHeadName(),
          "repoName", client.store().getRepoName());
  		subscribe(Uni.createFrom().item(index), response, ctx, objectMapper);
    } else {
      	HdesStatusCodes.catch404("unsupported hdes composer/service action", response);
  	}
  }
  
  public void doCopyas(RoutingContext event, HttpServerResponse response, HdesWebContext ctx, ObjectMapper objectMapper) {
    final var client = ctx.getComposer();    
    if (event.request().method() == HttpMethod.POST) {
      subscribe(
          client.copyAs(read(event, objectMapper, CopyAs.class)), 
          response, ctx, objectMapper);
      
    } else {
      HdesStatusCodes.catch404("unsupported hdes composer/copyas action", response);
    }
  }
  
  public void doDebugs(RoutingContext event, HttpServerResponse response, HdesWebContext ctx, ObjectMapper objectMapper) {
    final var client = ctx.getComposer();
    if (event.request().method() == HttpMethod.POST) {
      subscribe(
          client.debug(read(event, objectMapper, DebugRequest.class)), 
          response, ctx, objectMapper);
    } else {
      HdesStatusCodes.catch404("unsupported hdes composer/debug action", response);
    }
  }
  
  public void doImports(RoutingContext event, HttpServerResponse response, HdesWebContext ctx, ObjectMapper objectMapper) {
    final var client = ctx.getComposer();    
    if (event.request().method() == HttpMethod.POST) {
      subscribe(
          client.importTag(read(event, objectMapper, AstTag.class)), 
          response, ctx, objectMapper);
    } else {
      HdesStatusCodes.catch404("unsupported hdes composer/imports action", response);
    }
  }
  
  public void doHistory(RoutingContext event, HttpServerResponse response, HdesWebContext ctx, ObjectMapper objectMapper) {
    final var client = ctx.getComposer();
    if(event.request().method() == HttpMethod.GET) {
      subscribe(
          client.getHistory(event.pathParam("id")), 
          response, ctx, objectMapper);        
    } else {
      HdesStatusCodes.catch404("unsupported hdes composer/history action", response);
    }
  }
  
  public void doExports(RoutingContext event, HttpServerResponse response, HdesWebContext ctx, ObjectMapper objectMapper) {
    final var client = ctx.getComposer();
    if(event.request().method() == HttpMethod.GET) {
      subscribe(
          client.getStoreDump(), 
          response, ctx, objectMapper);
    } else {
      HdesStatusCodes.catch404("unsupported hdes composer/exports action", response);
    }
  }
  
  public void doModels(RoutingContext event, HttpServerResponse response, HdesWebContext ctx, ObjectMapper objectMapper) {
    final var client = ctx.getComposer();
    if(event.request().method() == HttpMethod.GET) {
      subscribe(
          client.get(), 
          response, ctx, objectMapper);
    } else {
      HdesStatusCodes.catch404("unsupported hdes composer/models action", response);
    }
  }
  
  public void doResources(RoutingContext event, HttpServerResponse response, HdesWebContext ctx, ObjectMapper objectMapper) {
    final var client = ctx.getComposer();
    
    if(event.request().method() == HttpMethod.GET) {
      subscribe(
          client.get(event.pathParam("id")), 
          response, ctx, objectMapper);
    } else if (event.request().method() == HttpMethod.POST) {
      subscribe(
          client.create(read(event, objectMapper, CreateEntity.class)),
          response, ctx, objectMapper);
    } else if(event.request().method() == HttpMethod.PUT) {
      subscribe(
          client.update(read(event, objectMapper, UpdateEntity.class)),
          response, ctx, objectMapper);
    } else if(event.request().method() == HttpMethod.DELETE) {
      subscribe(
          client.delete(event.pathParam("id")),
          response, ctx, objectMapper);

    } else {
      HdesStatusCodes.catch404("unsupported hdes composer/resources action", response);
    }
  }

  public void doCommands(RoutingContext event, HttpServerResponse response, HdesWebContext ctx, ObjectMapper objectMapper) {
    final var client = ctx.getComposer();
    if (event.request().method() == HttpMethod.POST) {
      subscribe(
          client.dryRun(read(event, objectMapper, UpdateEntity.class)), 
          response, ctx, objectMapper);
    } else {
      HdesStatusCodes.catch404("unsupported hdes composer/commands action", response);
    }
  }
  
  public <T> T read(RoutingContext event, ObjectMapper objectMapper, Class<T> type) {
    
   // return new JsonObject(event.getBody()).mapTo(type);
    try {
      return objectMapper.readValue(event.getBody().getBytes(), type);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public <T> List<T> readList(RoutingContext event, ObjectMapper objectMapper, Class<T> type) {
    
   // return new JsonObject(event.getBody()).mapTo(type);
    try {
      return objectMapper.readValue(event.getBody().getBytes(), new TypeReference<List<T>>(){});
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public <T> void subscribe(Uni<T> uni, HttpServerResponse response, HdesWebContext ctx, ObjectMapper objectMapper) {
    uni.onItem().transform(data -> {
      try {
        return Buffer.buffer(objectMapper.writeValueAsBytes(data));
      } catch(IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    })
    .onFailure().invoke(e -> HdesStatusCodes.catch422(e, response))
    .subscribe().with(data -> response.end(data)); 
  }
}
