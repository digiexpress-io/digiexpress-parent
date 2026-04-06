package io.digiexpress.eveli.client.web.resources.assets;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.resys.limaone.spi.dialob.FormDb;
import io.resys.limaone.spi.dialob.FormDb.FormAndTag;
import io.resys.limaone.spi.dialob.FormDb.FormMetadata;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/worker/rest/api/assets/dialob")
@RequiredArgsConstructor
public class AssetsDialobController {
  
  private final FormDb dialobCommands;

  
  @GetMapping(value="/fill/{sessionId}")
  public Uni<?> fillProxyGet(@PathVariable("sessionId") String sessionId) {
    return dialobCommands.withTenant()
        .formFillQuery().getOne(sessionId)
        .onItem().transform(e -> e.unwrap());
  }
  @PostMapping(value="/fill/{sessionId}")
  public Uni<?> fillProxyPost(@PathVariable("sessionId") String sessionId, @RequestBody String body) {
    return dialobCommands.withTenant().createFormFill()
        .actions(body)
        .formInstanceId(sessionId)
        .build().onItem().transform(e -> e.unwrap());
  }

  @RequestMapping(path="/proxy/api/forms/**", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<?> proxy(
      HttpServletRequest request, 
      @RequestBody(required = false) String body,
      @RequestHeader Map<String, String> headers) {
    
    final var query = request.getQueryString();
    final var path = "/forms" + request.getServletPath().substring(46);
    final var method = request.getMethod();
    
    return dialobCommands.withTenant()
        .formQuery().proxyAnything()
        .path(path).httpMethod(method).body(body).headers(headers).query(query).build()
        .onItem().transform(e -> e.unwrap());
  }
 
  @GetMapping(path="/tags", produces = MediaType.APPLICATION_JSON_VALUE)
  public Multi<FormAndTag> allTags() {
    return dialobCommands.withTenant().formTagQuery().flatAll();
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Multi<FormMetadata> allForms() {
    return dialobCommands.withTenant().formMetaQuery().findAll();
  }
}
