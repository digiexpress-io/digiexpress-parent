package io.thestencil.quarkus.feedback.handlers;

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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.CDI;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.exception.ExceptionUtils;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.thestencil.quarkus.feedback.FeedbackContext;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class FeedbackTemplate implements Handler<RoutingContext> {


  private final CurrentVertxRequest currentVertxRequest;
  
  public FeedbackTemplate(
      CurrentVertxRequest currentVertxRequest) {
    super();
    this.currentVertxRequest = currentVertxRequest;
  }
  
  protected abstract void handleResource(RoutingContext event, HttpServerResponse response, FeedbackContext ctx);
  
  protected void handleSecurity(RoutingContext event) {
    currentVertxRequest.setCurrent(event);
  }
  
  @Override
  public void handle(RoutingContext event) {
    ManagedContext requestContext = Arc.container().requestContext();
    if (requestContext.isActive()) {
      handleSecurity(event);      
      HttpServerResponse response = event.response();
      FeedbackContext ctx = CDI.current().select(FeedbackContext.class).get();
      try {
        handleResource(event, response, ctx);
      } catch (Exception e) {
        catch422(e, ctx, response);
      }
     return; 
    }
    
    HttpServerResponse response = event.response();
    FeedbackContext ctx = CDI.current().select(FeedbackContext.class).get();
    try {
      requestContext.activate();
      handleSecurity(event);
      handleResource(event, response, ctx);
    } finally {
      requestContext.terminate();
    }
  }
  
  public static void catch404(String id, FeedbackContext ctx, HttpServerResponse response) {
    
    // Log error
    String log = new StringBuilder().append("Token not found with id: ").append(id).toString();
    String hash = exceptionHash(log);
    FeedbackTemplate.log.error(hash + " - " + log);
    
    Map<String, String> msg = new HashMap<>();
    msg.put("appcode", hash);
    
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
    response.setStatusCode(404);
    response.end( Json.encode(msg) );
  }

  public static void catch422(String e, FeedbackContext ctx, HttpServerResponse response) {
    
    // Log error
    String log = new StringBuilder().append(e).append(System.lineSeparator()).toString();
    String hash = exceptionHash(log);
    FeedbackTemplate.log.error(hash + " - " + log);
    
    Map<String, String> msg = new HashMap<>();
    msg.put("appcode", hash);
    
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
    response.setStatusCode(422);
    response.end( Json.encode(msg) );
  }
  
  public static void catch403(String e, FeedbackContext ctx, HttpServerResponse response) {
    
    // Log error
    String log = new StringBuilder().append(e).append(System.lineSeparator()).toString();
    String hash = exceptionHash(log);
    FeedbackTemplate.log.error(hash + " - " + log);
    
    Map<String, String> msg = new HashMap<>();
    msg.put("appcode", hash);
    
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
    response.setStatusCode(403);
    response.end( Json.encode(msg) );
  }
  
  
  public static void catch422(Throwable e, FeedbackContext ctx, HttpServerResponse response) {
    String stack = ExceptionUtils.getStackTrace(e);
    
    // Log error
    String log = new StringBuilder().append(e.getMessage()).append(System.lineSeparator()).append(stack).toString();
    String hash = exceptionHash(log);
    FeedbackTemplate.log.error(hash + " - " + log);
    
    Map<String, String> msg = new HashMap<>();
    msg.put("appcode", hash);
    
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
    response.setStatusCode(422);
    response.end( Json.encode(msg) );
  }

  public static String exceptionHash(String msg) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.reset();
      md5.update(msg.getBytes(Charset.forName("UTF-8")));
      byte[] digest = md5.digest();
      return Hex.encodeHexString(digest);
    } catch (NoSuchAlgorithmException ex) {
      // Fall back to just hex timestamp in this improbable situation
      FeedbackTemplate.log.warn("MD5 Digester not found, falling back to timestamp hash", ex);
      long timestamp = System.currentTimeMillis();
      return Long.toHexString(timestamp);
    }
  }
  
}
