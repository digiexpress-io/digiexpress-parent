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

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.thestencil.quarkus.ide.FrontendRecorder;

import java.util.Optional;

@ConfigRoot(name = FrontendRecorder.FEATURE_BUILD_ITEM)
public class FrontendConfig {

  /**
   * Stencil IDE ui group id. Needed to locate resources from webjar
   */
  @ConfigItem(defaultValue = "io.digiexpress")
  String groupId;

  /**
   * Stencil IDE ui artifact id. Needed to locate resources from webjar
   */
  @ConfigItem(defaultValue = "stencil-composer-integration")
  String artifactId;

  /**
   * Stencil IDE ui resource path inside jar
   */
  @ConfigItem
  Optional<String> webjarRoot;

  /**
   * IDE routing path
   */
  @ConfigItem(defaultValue = "portal-app")
  String servicePath;

  /**
   * Stencil IDE ui version. Needed to locate resources from webjar
   */
  @ConfigItem(defaultValue = "${quarkus.application.version}")
  String stencilComposerVersion;

  /**
   * IDE backend server path
   */
  @ConfigItem
  String serverPath;

  /**
   * Locks the IDE, edit/view disabled
   */
  @ConfigItem(defaultValue = "false")
  Boolean locked;
  
  /**
   * OIDC login path
   * "/oauth2/authorization/oidcprovider"
   */
  @ConfigItem
  Optional<String> oidcPath;
  
  /**
   * OIDC status ping path 
   */
  @ConfigItem
  Optional<String> statusPath; 
  
}
