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


import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = ConfigPhase.RUN_TIME, name = UserActionsRecorder.FEATURE_BUILD_ITEM)
public class RuntimeConfig {
  /**
   * Default locale for creating forms
   */
  @ConfigItem(defaultValue = "fi")
  String defaultLocale;
  
  /**
   * Configuration for process management backend
   */
  @ConfigItem
  ProcessesConfig processes;

  /**
   * Configuration for files management backend
   */
  @ConfigItem
  AttachmentsConfig attachments;
  
  /**
   * Configuration for tasks management backend
   */
  @ConfigItem
  TasksConfig tasks;
  
  /**
   * Configuration for form filling backend
   */
  @ConfigItem
  FillConfig fill;

  /**
   * Configuration for form api backend
   */  
  @ConfigItem
  ReviewConfig review;
}
