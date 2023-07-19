package io.thestencil.quarkus.ide;

/*-
 * #%L
 * quarkus-stencil-ide-deployment
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;

import io.quarkus.deployment.configuration.ConfigurationError;
import io.quarkus.deployment.util.FileUtil;

public class IndexFactory {

  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private String frontendPath;
    private String server;
    private String indexFileContent;
    private Boolean locked;
    private String oidc;
    private String status;
    
    public Builder frontend(String frontendPath) {
      this.frontendPath = frontendPath;
      return this;
    }
    public Builder oidc(String oidc) {
      this.oidc = oidc;
      return this;
    }
    public Builder status(String status) {
      this.status = status;
      return this;
    }
    public Builder locked(Boolean locked) {
      this.locked = locked;
      return this;
    }
    public Builder server(String backendPath) {
      this.server = backendPath;
      return this;
    }
    public Builder index(Path path) {
      File file = path.toFile();
      try(InputStream stream = new FileInputStream(file)) {
        byte[] bytes = FileUtil.readFileContents(stream);
        this.indexFileContent = new String(bytes, StandardCharsets.UTF_8);
      } catch (Exception e) {
        throw new ConfigurationError(new StringBuilder("Failed to create frontend index.html, ")
            .append("msg = ").append(e.getMessage()).append(System.lineSeparator()).append(",")
            .append("path = ").append(path).append("!")
            .toString());
      }
      return this;
    }
    public Builder index(byte[] indexFileContent) {
      this.indexFileContent = new String(indexFileContent, StandardCharsets.UTF_8);
      return this;
    }
    private String formatConfig(String value) {
      if(value == null) {
        return "undefined";
      }
      return "'" + value + "'";
    }
    public byte[] build() {
      PortalAssert.notEmpty(frontendPath, () -> "define frontendPath!");
      PortalAssert.notEmpty(server, () -> "define server!");
       
      String newPath = frontendPath.startsWith("/") ? frontendPath : "/" + frontendPath;
      newPath = newPath.endsWith("/") ? newPath : newPath + "/";
      StringBuilder newHref = new StringBuilder().append(newPath);
      StringBuilder newConfig = new StringBuilder()
          .append("const portalconfig={")
          .append("server: {")
          .append("    url: " + formatConfig(server) + ",")
          .append("    buildTime: " + formatConfig(LocalDateTime.now().toString()) + ",")
          .append("    locked: " + (Boolean.TRUE.equals(locked) ? true : "undefined") + ",")
          .append("    status: " + formatConfig(status) + ",")
          .append("    oidc: " + formatConfig(oidc) + ",")
          .append("  }, ")
          .append("}");  
      
      return (indexFileContent
          .replaceAll("/portal/", newPath)
          .replaceAll("https://portalconfig/", newHref.toString())
          .replaceFirst("const portalconfig=\\{\\}", newConfig.toString())
          + "<!-- NEW - PATH: " + newPath + "-->")
          .getBytes(StandardCharsets.UTF_8);
    }
  }
}
