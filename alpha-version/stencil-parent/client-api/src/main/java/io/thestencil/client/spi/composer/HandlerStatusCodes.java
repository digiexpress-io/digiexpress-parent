package io.thestencil.client.spi.composer;

/*-
 * #%L
 * stencil-client
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

@Slf4j
public class HandlerStatusCodes {
  public static void catch404(String id, HttpServerResponse response) {
    
    // Log error
    var message = "Token not found with id: " + id;
    String hash = exceptionHash(message);
    HandlerStatusCodes.log.error(hash + " - " + message);
    
    var msg = new HashMap<String, String>();
    msg.put("appcode", hash);
    
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json");
    response.setStatusCode(404);
    response.end( Json.encode(msg) );
  }

  public static void catch422(String desc, HttpServerResponse response) {
    // Log error
    String hash = exceptionHash(desc);
    HandlerStatusCodes.log.error(hash + " - " + desc);

    var msg = new HashMap<String, String>();
    msg.put("appcode", hash);
    
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json");
    response.setStatusCode(422);
    response.end( Json.encode(msg) );
  }

  
  public static void catch422(Throwable e, HttpServerResponse response) {
    String stack = ExceptionUtils.getStackTrace(e);
    
    // Log error
    var message = e.getMessage() + System.lineSeparator() + stack;
    String hash = exceptionHash(message);
    HandlerStatusCodes.log.error(hash + " - " + message);

    var msg = new HashMap<String, String>();
    msg.put("appcode", hash);
    
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json");
    response.setStatusCode(422);
    response.end( Json.encode(msg) );
  }

  public static String exceptionHash(String msg) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.reset();
      md5.update(msg.getBytes(StandardCharsets.UTF_8));
      byte[] digest = md5.digest();
      return Hex.encodeHexString(digest);
    } catch (NoSuchAlgorithmException ex) {
      // Fall back to just hex timestamp in this improbable situation
      log.warn("MD5 Digester not found, falling back to timestamp hash", ex);
      long timestamp = System.currentTimeMillis();
      return Long.toHexString(timestamp);
    }
  }
}
