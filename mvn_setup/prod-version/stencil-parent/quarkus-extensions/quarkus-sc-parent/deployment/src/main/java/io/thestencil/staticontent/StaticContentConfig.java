package io.thestencil.staticontent;

/*-
 * #%L
 * quarkus-stencil-sc-deployment
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

import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.nio.file.Path;
import java.time.ZoneOffset;
import java.util.Optional;

@ConfigMapping
@ConfigRoot(name = StaticContentProcessor.FEATURE_BUILD_ITEM)
public interface StaticContentConfig {
  
  /**
   * Static content routing path
   */
  @WithDefault("portal-app/site")
  String servicePath();
  
  /**
   * Static content for accessing images
   */
  @WithDefault("portal-app/site/images")
  String imagePath();
  
  /**
   * Default locale and not found locale for site contents
   */
  @WithDefault("en")
  String defaultLocale();
  
  /**
   * Server offset
   */
  @WithDefault("+2")
  ZoneOffset offset();
  
  /**
   * Artifact from where to search
   */
  Optional<Path> siteJson();

  /**
   * Artifact from where to search
   * groupId:artifactId
   * io.resys.client.portal:static-content
   */
  Optional<String> webjar();
}
