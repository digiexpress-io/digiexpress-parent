package io.thestencil.quarkus.useractions;

/*-
 * #%L
 * quarkus-stencil-user-actions
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


import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping
@ConfigRoot(phase = ConfigPhase.RUN_TIME, name = UserActionsRecorder.FEATURE_BUILD_ITEM)
public interface RuntimeConfig {
  /**
   * Default locale for creating forms
   */
  @WithDefault("fi")
  String defaultLocale();
  
  /**
   * Configuration for process management backend
   */
  ProcessesConfig processes();

  /**
   * Configuration for files management backend
   */
  AttachmentsConfig attachments();
  
  /**
   * Configuration for tasks management backend
   */
  TasksConfig tasks();
  
  /**
   * Configuration for form filling backend
   */
  FillConfig fill();

  /**
   * Configuration for form api backend
   */  
  ReviewConfig review();
}
