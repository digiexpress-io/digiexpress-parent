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

import io.digiexpress.eveli.dialob.api.DialobProxy;
import io.digiexpress.eveli.dialob.spi.DialobAssert.DialobException;
import io.netty.handler.codec.http.HttpHeaderNames;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;

import java.util.Collections;
import java.util.Map;

@RequiredArgsConstructor
public class DialobProxyImpl implements DialobProxy {

  private final DialobService dialobService;

  @Override
  public ResponseEntity<String> sessionPost(String sessionId, String body) {
    if (invalidSessionId(sessionId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    final var headers = new HttpHeaders();
    final var requestEntity = new HttpEntity<>(body, headers);
    try {
      return dialobService.getSessions().exchange("/"+ sessionId, HttpMethod.POST, requestEntity, String.class);
    } catch (Exception e) {
      throw new DialobException(e.getMessage(), e);
    }
  }

  @Override
  public ResponseEntity<String> sessionGet(String sessionId) {
    if (invalidSessionId(sessionId)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    try {
      return dialobService.getSessions().getForEntity("/" + sessionId, String.class);
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
      .forEach((entry) -> reqHeaders.put(entry.getKey(), Collections.singletonList(entry.getValue())));
    return dialobService.getForms().exchange(path, method, new HttpEntity<>(body, reqHeaders), String.class);
  }

  static boolean invalidSessionId(String sessionId) {
    return sessionId == null || !sessionId.matches("[a-fA-F0-9-_]+");
  }

}
