package io.dialob.spring.composer.controllers.util;

/*-
 * #%L
 * hdes-spring-composer
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import java.util.List;

import javax.annotation.Nullable;

import io.dialob.spring.composer.ComposerAutoConfiguration.IdeToken;


public class ThymeleafConfig {
  
  private String contextPath;
  private String hash;
  private String mainJs;
  private List<String> css;
  private String manifest;
  @Nullable
  private IdeToken csrf;
  private String url;
  @Nullable
  private String oidc;
  @Nullable
  private String status;
  
  public String getContextPath() {
    return contextPath;
  }
  public ThymeleafConfig setContextPath(String contextPath) {
    this.contextPath = contextPath;
    return this;
  }
  public IdeToken getCsrf() {
    return csrf;
  }
  public ThymeleafConfig setCsrf(IdeToken csrf) {
    this.csrf = csrf;
    return this;
  }
  public String getHash() {
    return hash;
  }
  public ThymeleafConfig setHash(String hash) {
    this.hash = hash;
    return this;
  }
  public String getUrl() {
    return url;
  }
  public ThymeleafConfig setUrl(String url) {
    this.url = url;
    return this;
  }
  public String getMainJs() {
    return mainJs;
  }
  public ThymeleafConfig setMainJs(String mainJs) {
    this.mainJs = mainJs;
    return this;
  }
  public List<String> getCss() {
    return css;
  }
  public ThymeleafConfig setCss(List<String> css) {
    this.css = css;
    return this;
  }
  public String getManifest() {
    return manifest;
  }
  public ThymeleafConfig setManifest(String manifest) {
    this.manifest = manifest;
    return this;
  }
  public String getOidc() {
    return oidc;
  }
  public ThymeleafConfig setOidc(String oidc) {
    this.oidc = oidc;
    return this;
  }
  public String getStatus() {
    return status;
  }
  public ThymeleafConfig setStatus(String status) {
    this.status = status;
    return this;
  }
}
