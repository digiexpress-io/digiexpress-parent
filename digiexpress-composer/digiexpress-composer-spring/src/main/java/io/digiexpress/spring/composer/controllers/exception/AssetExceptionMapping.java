package io.digiexpress.spring.composer.controllers.exception;

/*-
 * #%L
 * wrench-assets-bundle
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
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
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.WebUtils;


@ControllerAdvice
public class AssetExceptionMapping {
  private static final Logger LOGGER = LoggerFactory.getLogger(AssetExceptionMapping.class);


  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> handleConflict(Exception t, WebRequest webRequest) {
    HttpHeaders headers = new HttpHeaders();
    if(t instanceof DataRedirectException) {
      DataRedirectException redirect = (DataRedirectException) t;
      headers.set("Location", redirect.getUrl());
      return new ResponseEntity<>(null, headers, HttpStatus.FOUND);
    }
    
    long timestamp = System.currentTimeMillis();
    String stacktrace = new StringBuilder().
        append(String.valueOf(timestamp)).append(" ").
        append(t.getMessage()).
        append(ExceptionUtils.getStackTrace(t)).toString();
    String hash = exceptionHash(stacktrace, timestamp);
    LOGGER.error("Internal error ID " + hash, t);
    
    Map.Entry<Integer, List<Message>> body = createBody(t, hash);
    List<Message> messages = body.getValue();
    messages.add(new Message("stacktrace", stacktrace));
    return handleExceptionInternal(t, body.getValue(), headers, HttpStatus.resolve(body.getKey()), webRequest);
  }

  protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body,
      HttpHeaders headers, HttpStatus status, WebRequest request) {

    if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
      request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
    }
    return new ResponseEntity<>(body, headers, status);
  }
  
  /**
   * Calculate unique hash for exception case
   * @return Unique hash for exception
   */
  protected String exceptionHash(String msg, long timestamp) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.reset();
      md5.update(msg.getBytes(Charset.forName("UTF-8")));
      byte[] digest = md5.digest();
      return Hex.encodeHexString(digest);
    } catch (NoSuchAlgorithmException ex) {
      // Fall back to just hex timestamp in this improbable situation
      LOGGER.warn("MD5 Digester not found, falling back to timestamp hash", ex);
      return Long.toHexString(timestamp);
    }
  }

  private Map.Entry<Integer, List<Message>> createBody(Exception e, String hash) {
    List<Message> messages = new ArrayList<>();
    int status = 500;
      messages.add(new Message("internalError", "internal error, see log code"));
    

    List<Message> body = messages.stream()
        .map(m -> translate(m.setLogCode(hash)))
        .collect(Collectors.toList());
    return new AbstractMap.SimpleEntry<>(status, body);
  }
  

  protected Message translate(Message message) {
    try {
      String translation =  MessageFormat.format(message.getValue(), message.getArgs());
      return new Message(message.getCode(), translation, message.getContext()).setArgs(message.getArgs());
    } catch(Exception e) {
      LOGGER.debug(e.getMessage(), e);
      return message;
    }
  }
}
