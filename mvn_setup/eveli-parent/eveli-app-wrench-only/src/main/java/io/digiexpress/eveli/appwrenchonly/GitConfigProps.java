package io.digiexpress.eveli.appwrenchonly;

/*-
 * #%L
 * eveli-app-wrench-only
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


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;



@ConfigurationProperties(prefix = "wrench.assets.git")
public class GitConfigProps {
  private String repositoryUrl;
  private String branchSpecifier;
  private String repositoryPath;

  @Value("${enabled:false}")
  private boolean enabled;

  @Value("${privateKey:classpath:ssh/id_rsa}")
  private String privateKey;

  @Value("${email:asset.manager@resys.io}")
  private String email;

  @Value("${message:application commit}")
  private String message;

  @Value("${path:src/main/resources}")
  private String path;

  public String getRepositoryUrl() {
    return repositoryUrl;
  }
  public GitConfigProps setRepositoryUrl(String repositoryUrl) {
    this.repositoryUrl = repositoryUrl;
    return this;
  }
  public String getBranchSpecifier() {
    return branchSpecifier;
  }
  public GitConfigProps setBranchSpecifier(String branchSpecifier) {
    this.branchSpecifier = branchSpecifier;
    return this;
  }
  public String getPrivateKey() {
    return privateKey;
  }
  public GitConfigProps setPrivateKey(String privateKey) {
    this.privateKey = privateKey;
    return this;
  }
  public String getPath() {
    return path;
  }
  public GitConfigProps setPath(String path) {
    this.path = path;
    return this;
  }
  public String getEmail() {
    return email;
  }
  public void setEmail(String email) {
    this.email = email;
  }
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }
  public String getRepositoryPath() {
    return repositoryPath;
  }
  public void setRepositoryPath(String repositoryPath) {
    this.repositoryPath = repositoryPath;
  }
  public boolean isEnabled() {
    return enabled;
  }
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
