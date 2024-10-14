package io.resys.hdes.spring.composer.controllers.util;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import io.resys.hdes.client.spi.util.FileUtils;
import io.resys.hdes.spring.composer.ComposerIdeConfig;

public class ControllerUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(ComposerIdeConfig.class);
  private static final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

  public static IdeOnClasspath ideOnClasspath(String configContextPath) {
    final String contextPath;
    try {
      contextPath = FileUtils.cleanPath(configContextPath).length() <= 1 ? "/"
          : "/" + FileUtils.cleanPath(configContextPath) + "/";
    } catch (Exception e) {
      return new IdeOnClasspath();
    }

    try {
      final String path = "webjars/hdes-composer-ui/" + getVersion() + "/";

      final String js = chunkJs();
      final String hash = js.substring(0, js.length() - 3);
      final String manifest = resolveRuntimeScript("classpath*:**/hdes-composer-ui/**/manifest.json");

      final List<String> css = Arrays.asList(contextPath + path + "static/"
          + resolveRuntimeScript("classpath*:**/hdes-composer-ui/**/index*.css"));

      final IdeOnClasspath config = new IdeOnClasspath(
          hash, css, 
          contextPath + path + manifest,
          contextPath + path + "static/" + js);

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Hdes IDE is enabled." + System.lineSeparator() + config);
      }

      return config;
    } catch (Exception e) {
      LOGGER.debug("Hdes IDE is disabled.");
      return new IdeOnClasspath();
    }
  }

  private static String getVersion() throws IOException {
    Resource[] resources = resolver.getResources("classpath*:**/hdes-composer-ui/**/index.html");
    if (resources.length > 0) {
      String uri = resources[0].getURL().toString();
      String[] paths = uri.split("\\/");
      return paths[paths.length - 2];
    }
    return null;
  }

  private static String resolveRuntimeScript(String ideManifestJsPattern) throws IOException {
    Resource[] resources = resolver.getResources(ideManifestJsPattern);
    if (resources.length > 0) {
      return resources[0].getFilename();
    }
    return null;
  }

  private static String chunkJs() throws IOException {
    Resource[] resources = resolver.getResources("classpath*:**/hdes-composer-ui/**/index*.js");
    for (Resource resource : resources) {
      return resource.getFilename();
    }
    return null;
  }

  private static String getContextPath(String serverContextPath) {
    String cp = "";
    if (StringUtils.isNotBlank(serverContextPath)) {
      if (!serverContextPath.startsWith("/")) {
        cp = "/";
      }
      cp = cp + serverContextPath;
      if (cp.endsWith("/")) {
        cp = cp.substring(0, cp.length() - 1);
      }
    }
    return cp;
  }

  private static String getUrl(String proto, String host, String serverContextPath) {
    final String contextPath = getContextPath(serverContextPath);
    if (StringUtils.isBlank(proto)) {
      proto = "http";
    }
    if (!proto.endsWith(":")) {
      proto = proto + ":";
    }
    String baseUrl = proto + "//" + host + contextPath;
    return baseUrl;
  }

  public static String getRestUrl(String proto, String host, String apiContextPath, String serverContextPath) {
    return FileUtils.cleanPath(getUrl(proto, host, serverContextPath)) + "/" + FileUtils.cleanPath(apiContextPath) + "/";
  }
}
