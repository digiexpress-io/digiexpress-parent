package io.thestencil.quarkus.iam.deployment;

/*-
 * #%L
 * quarkus-stencil-iam-deployment
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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
import io.thestencil.quarkus.iam.IAMRecorder;

@ConfigMapping
@ConfigRoot(name = IAMRecorder.FEATURE_BUILD_ITEM)
public interface IAMConfig {
  
  /**
   * Static content routing path
   */
  @WithDefault("portal-app/iam")
  String servicePath();
}
