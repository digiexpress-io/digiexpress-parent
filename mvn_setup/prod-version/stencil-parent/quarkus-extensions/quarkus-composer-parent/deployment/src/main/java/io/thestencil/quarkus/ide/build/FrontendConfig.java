package io.thestencil.quarkus.ide.build;

/*-
 * #%L
 * quarkus-stencil-composer-deployment
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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
import io.thestencil.quarkus.ide.FrontendRecorder;

import java.util.Optional;

@ConfigMapping(prefix = "quarkus." + FrontendRecorder.FEATURE_BUILD_ITEM)
@ConfigRoot
public interface FrontendConfig {

  /**
   * Stencil IDE ui group id. Needed to locate resources from webjar
   */
  @WithDefault("io.digiexpress")
  String groupId();

  /**
   * Stencil IDE ui artifact id. Needed to locate resources from webjar
   */
  @WithDefault("stencil-composer-integration")
  String artifactId();

  /**
   * Stencil IDE ui resource path inside jar
   */
  Optional<String> webjarRoot();

  /**
   * IDE routing path
   */
  @WithDefault("portal-app")
  String servicePath();

  /**
   * Stencil IDE ui version. Needed to locate resources from webjar
   */
  @WithDefault("${quarkus.application.version}")
  String stencilComposerVersion();

  /**
   * IDE backend server path
   */
  String serverPath();

  /**
   * Locks the IDE, edit/view disabled
   */
  @WithDefault("false")
  Boolean locked();
  
  /**
   * OIDC login path
   * "/oauth2/authorization/oidcprovider"
   */
  Optional<String> oidcPath();
  
  /**
   * OIDC status ping path 
   */
  Optional<String> statusPath();
  
}
