package io.dialob.spring.composer.config;

import org.springframework.beans.factory.annotation.Value;

/*-
 * #%L
 * wrench-component-assets-persistence
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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;



@Component
@ConfigurationProperties(prefix="wrench.assets.pg")
public class PgConfigBean {

  private String repositoryName;
  private String branchSpecifier;
  
  private String pgHost;
  @Value("${pgPort:5432}")
  private Integer pgPort;
  @Value("${pgPoolSize:3}")
  private Integer pgPoolSize;
  
  private String pgDb;
  private String pgUser;
  private String pgPass;

  
  @Value("${enabled:false}")
  private boolean enabled;

  @Value("${email:asset.manager@resys.io}")
  private String email;

  @Value("${message:application commit}")
  private String message;

  public String getRepositoryName() {
    return repositoryName;
  }

  public void setRepositoryName(String repositoryName) {
    this.repositoryName = repositoryName;
  }

  public String getBranchSpecifier() {
    return branchSpecifier;
  }

  public void setBranchSpecifier(String branchSpecifier) {
    this.branchSpecifier = branchSpecifier;
  }

  public String getPgHost() {
    return pgHost;
  }

  public void setPgHost(String pgHost) {
    this.pgHost = pgHost;
  }

  public Integer getPgPort() {
    return pgPort;
  }

  public void setPgPort(Integer pgPort) {
    this.pgPort = pgPort;
  }

  public Integer getPgPoolSize() {
    return pgPoolSize;
  }

  public void setPgPoolSize(Integer pgPoolSize) {
    this.pgPoolSize = pgPoolSize;
  }

  public String getPgDb() {
    return pgDb;
  }

  public void setPgDb(String pgDb) {
    this.pgDb = pgDb;
  }

  public String getPgUser() {
    return pgUser;
  }

  public void setPgUser(String pgUser) {
    this.pgUser = pgUser;
  }

  public String getPgPass() {
    return pgPass;
  }

  public void setPgPass(String pgPass) {
    this.pgPass = pgPass;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
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

}
