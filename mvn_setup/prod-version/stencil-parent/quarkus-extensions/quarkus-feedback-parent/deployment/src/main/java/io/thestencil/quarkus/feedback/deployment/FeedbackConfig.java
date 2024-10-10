package io.thestencil.quarkus.feedback.deployment;

/*-
 * #%L
 * quarkus-stencil-user-actions-deployment
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
import io.quarkus.runtime.annotations.ConfigRoot;
import io.thestencil.quarkus.feedback.FeedbackRecorder;


@ConfigRoot(name = FeedbackRecorder.FEATURE_BUILD_ITEM)
public class FeedbackConfig {
  
  /**
   * Static content routing path
   */
  @ConfigItem(defaultValue = "portal-app/feedback")
  String servicePath;
}
