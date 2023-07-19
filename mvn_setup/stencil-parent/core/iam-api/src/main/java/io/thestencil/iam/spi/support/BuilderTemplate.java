package io.thestencil.iam.spi.support;

/*-
 * #%L
 * iam-api
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

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.mutiny.core.MultiMap;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.WebClient;


public class BuilderTemplate {
  private final WebClient client;
  private final RequestOptions init;
  
  protected BuilderTemplate(WebClient client, RequestOptions init) {
    super();
    this.client = client;
    this.init = init;
  }
  
  protected RequestOptions options(String path) {
    final String uri = this.init.getURI() + path;
    return new RequestOptions(init)
        .setURI(uri)
        .setHeaders(this.init.getHeaders());
  }
  
  public <T> HttpRequest<Buffer> request(HttpMethod method, String uri) {
    return this.getClient()
        .requestAbs(method, uri)
        .putHeaders(getHeaders());
  }
  
  public <T> HttpRequest<Buffer> get(String uri) {
    return this.getClient()
        .requestAbs(HttpMethod.GET, uri)
        .putHeaders(getHeaders());
  }

  public <T> HttpRequest<Buffer> delete(String uri) {
    return this.getClient()
        .requestAbs(HttpMethod.DELETE, uri)
        .putHeaders(getHeaders());
  }
  public <T> HttpRequest<Buffer> post(String uri) {
    return this.getClient()
        .requestAbs(HttpMethod.POST, uri)
        .putHeaders(getHeaders());
  }
  
  public MultiMap getHeaders() {
    final io.vertx.core.MultiMap headers = init.getHeaders() == null ? 
        io.vertx.core.MultiMap.caseInsensitiveMultiMap() : 
        init.getHeaders();
    return new MultiMap(headers);
  }
  
  public String getUri(String path) {
    final var options = options(path);
    return "http://" + options.getHost() + "/" + options.getURI();
  }

  public WebClient getClient() {
    return client;
  }

}
