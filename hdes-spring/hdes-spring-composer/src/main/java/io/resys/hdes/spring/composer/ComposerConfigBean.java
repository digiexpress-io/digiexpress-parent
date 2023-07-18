package io.resys.hdes.spring.composer;

import javax.annotation.Nullable;

/*-
 * #%L
 * wrench-component-context
 * %%
 * Copyright (C) 2016 - 2017 Copyright 2016 ReSys OÜ
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix="wrench.assets")
@Component
public class ComposerConfigBean {
  
  public static final String REST_CTX_PATH = "${wrench.assets.rest-context-path:/assets}";
  public static final String IDE_CTX_PATH = "${wrench.assets.ide-context-path:/ide}";
  
  // is the whole component enabled
  @Value("${enabled:true}")
  private boolean enabled;

  // composer rest api enabled
  @Value("${rest:true}")
  private boolean rest;
  @Value("${rest-context-path:/assets}")
  private String restContextPath;
  
  // composer rest ui enabled
  @Value("${ide:true}")
  private boolean ide;
  @Value("${ide-https:false}")
  private boolean ideHttps;
  @Value("${ide-redirect:true}")
  private String ideRedirect;
  @Value("${ide-context-path:/ide}")
  private String ideContextPath;

  @Nullable
  @Value("${ide-oidc-path:}")
  private String oidc;
  @Nullable
  @Value("${ide-status-path:}")
  private String status;
  

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isRest() {
    return rest;
  }

  public void setRest(boolean rest) {
    this.rest = rest;
  }

  public boolean isIde() {
    return ide;
  }

  public void setIde(boolean ide) {
    this.ide = ide;
  }

  public String getIdeRedirect() {
    return ideRedirect;
  }

  public void setIdeRedirect(String ideRedirect) {
    this.ideRedirect = ideRedirect;
  }

  public String getIdeContextPath() {
    return ideContextPath;
  }

  public void setIdeContextPath(String ideContextPath) {
    this.ideContextPath = ideContextPath;
  }

  public String getRestContextPath() {
    return restContextPath;
  }

  public void setRestContextPath(String restContextPath) {
    this.restContextPath = restContextPath;
  }

  public boolean isIdeHttps() {
    return ideHttps;
  }

  public void setIdeHttps(boolean ideHttps) {
    this.ideHttps = ideHttps;
  }

  public String getOidc() {
    return oidc;
  }

  public void setOidc(String oidc) {
    this.oidc = oidc;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

}
