package io.thestencil.quarkus.iam;

/*-
 * #%L
 * quarkus-stencil-iam
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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.arc.DefaultBean;
import io.thestencil.iam.api.IAMClient;
import io.thestencil.iam.api.ImmutableRemoteIntegration;
import io.thestencil.iam.spi.suomi.IAMClientSuomi;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

@ApplicationScoped
public class IAMBeanFactory {
  
  /**
   * Injection point for the ID Token issued by the OpenID Connect Provider
   */
  @Inject JsonWebToken idToken;
  
  private String servicePath;
  private RuntimeConfig runtimeConfig;

  public IAMBeanFactory setServicePath(String servicePath) {
    this.servicePath = servicePath;
    return this;
  }

  public IAMBeanFactory setRuntimeConfig(RuntimeConfig runtimeConfig) {
    this.runtimeConfig = runtimeConfig;
    return this;
  }
  
  @Produces @Singleton @DefaultBean
  public IAMClient iamClient(Vertx vertx) {
    final var webClient = WebClient.create(vertx, new WebClientOptions());
    final var personSecurityProxy = ImmutableRemoteIntegration.builder()
        .host(cleanPath(runtimeConfig.personSecurityProxy.host))
        .path(cleanPath(runtimeConfig.personSecurityProxy.path))
        .build();
    final var companySecurityProxy = ImmutableRemoteIntegration.builder()
        .host(cleanPath(runtimeConfig.companySecurityProxy.host))
        .path(cleanPath(runtimeConfig.companySecurityProxy.path))
        .build();
    
    return IAMClientSuomi.builder()
        .idToken(idToken).servicePath(servicePath)
        .personSecurityProxy(personSecurityProxy)
        .companySecurityProxy(companySecurityProxy)
        .webClient(webClient)
        .builder();
  }
  
  public static String cleanPath(String value) {
    final var start = value.startsWith("/") ? value.substring(1) : value;
    return start.endsWith("/") ? value.substring(0, start.length() -2) : start;
  }
}
