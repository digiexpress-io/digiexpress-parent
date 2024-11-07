package io.digiexpress.eveli.dialob.spi;

/*-
 * #%L
 * dialob-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.Arrays;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import io.digiexpress.eveli.dialob.api.DialobProxy;
import io.digiexpress.eveli.dialob.spi.DialobAssert.DialobException;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DialobProxyImpl implements DialobProxy {

  private final DialobService dialobService;
  
  @Override
  public ResponseEntity<String> sessionPost(String sessionId, String body) {
    final var headers = new HttpHeaders();
    
    final HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
    try {
      ResponseEntity<String> response = dialobService.getSessions().exchange("/"+ sessionId, HttpMethod.POST, requestEntity, String.class);
      return response;
    } catch (Exception e) {
      throw new DialobException(e.getMessage(), e);
    }
  }

  @Override
  public ResponseEntity<String> sessionGet(String sessionId) {            
    try {
      
      ResponseEntity<String> response = dialobService.getSessions().getForEntity("/" + sessionId, String.class);
      return response;
    } catch (Exception e) {
      throw new DialobException(e.getMessage(), e);
    }
  }
  @Override
  public ResponseEntity<String> formRequest(String path, String query, HttpMethod method, String body, Map<String, String> headers) {
    
    final var reqHeaders = new HttpHeaders();
    
    headers.entrySet().stream()
      .filter(entry ->
        entry.getKey().equals(HttpHeaderNames.CONTENT_TYPE.toString()) ||
        entry.getKey().equals(HttpHeaderNames.ACCEPT.toString())
      )
      .forEach((entry) -> reqHeaders.put(entry.getKey(), Arrays.asList(entry.getValue())));
    return dialobService.getForms().exchange(path, method, new HttpEntity<String>(body, reqHeaders), String.class);
  }
}
