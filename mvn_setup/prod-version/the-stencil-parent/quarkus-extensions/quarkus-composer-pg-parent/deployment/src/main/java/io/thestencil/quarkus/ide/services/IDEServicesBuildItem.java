package io.thestencil.quarkus.ide.services;

/*-
 * #%L
 * quarkus-stencil-ide-services-deployment
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.quarkus.builder.item.SimpleBuildItem;

public final class IDEServicesBuildItem extends SimpleBuildItem {
  
  private final String servicePath;
  private final String articlesPath;
  private final String pagesPath;
  private final String workflowsPath;
  private final String linksPath;
  private final String releasesPath;
  private final String localePath;
  private final String migrationPath;
  private final String templatesPath;
  private final String versionPath;
  
  public IDEServicesBuildItem(
      String servicePath, 
      String migrationPath, 
      String articlesPath, 
      String pagesPath, 
      String workflowsPath,
      String linksPath, 
      String releasesPath,
      String localePath,
      String templatesPath,
      String versionPath) {
    super();
    this.servicePath = servicePath;
    this.articlesPath = articlesPath;
    this.pagesPath = pagesPath;
    this.workflowsPath = workflowsPath;
    this.linksPath = linksPath;
    this.releasesPath = releasesPath;
    this.localePath = localePath;
    this.migrationPath = migrationPath;
    this.templatesPath = templatesPath;
    this.versionPath = versionPath;
  }

  public String getMigrationPath() {
    return migrationPath;
  }
  public String getServicePath() {
    return servicePath;
  }
  public String getArticlesPath() {
    return articlesPath;
  }
  public String getPagesPath() {
    return pagesPath;
  }
  public String getWorkflowsPath() {
    return workflowsPath;
  }
  public String getLinksPath() {
    return linksPath;
  }
  public String getReleasesPath() {
    return releasesPath;
  }
  public String getLocalePath() {
    return localePath;
  }
  public String getTemplatesPath() {
    return templatesPath;
  }
  public String getVersionPath() {
    return versionPath;
  }
  
  
  public static Builder builder(String servicePath) {
    return new Builder(servicePath);
  }
  
  public static class Builder {
    private final String servicePath;
    private String articlesPath; 
    private String pagesPath;
    private String workflowsPath;
    private String linksPath;
    private String releasesPath;
    private String localePath;
    private String migrationPath;
    private String templatesPath;
    private String versionPath;

    public Builder(String servicePath) {
      super();
      this.servicePath = "/" + servicePath;
    }
    public Builder articlesPath(String articlesPath) {
      this.articlesPath = servicePath + "/" + articlesPath;
      return this;
    }
    public Builder pagesPath(String pagesPath) {
      this.pagesPath = servicePath + "/" + pagesPath;
      return this;
    }
    public Builder workflowsPath(String workflowsPath) {
      this.workflowsPath = servicePath + "/" + workflowsPath;
      return this;
    }
    public Builder linksPath(String linksPath) {
      this.linksPath = servicePath + "/" + linksPath;
      return this;
    }
    public Builder releasesPath(String releasesPath) {
      this.releasesPath = servicePath + "/" + releasesPath;
      return this;
    }
    public Builder localePath(String localePath) {
      this.localePath = servicePath + "/" + localePath;
      return this;
    }
    public Builder migrationPath(String migrationPath) {
      this.migrationPath = servicePath + "/" + migrationPath;
      return this;
    }    
    public Builder templatesPath(String templatesPath) {
      this.templatesPath = servicePath + "/" + templatesPath;
      return this;
    }
    public Builder versionPath(String versionPath) {
      this.versionPath = servicePath + "/" + versionPath;
      return this;
    }
    public IDEServicesBuildItem build() {
      return new IDEServicesBuildItem(servicePath, migrationPath, articlesPath, pagesPath, workflowsPath, linksPath, releasesPath, localePath, templatesPath, versionPath);
    }
  }
}