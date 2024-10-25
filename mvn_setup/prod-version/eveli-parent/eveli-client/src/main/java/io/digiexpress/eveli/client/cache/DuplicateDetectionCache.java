package io.digiexpress.eveli.client.cache;

/*-
 * #%L
 * eveli-client
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

import org.springframework.cache.annotation.Cacheable;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DuplicateDetectionCache {
  @Cacheable(value="completedQuestionnaire", key="#p0", sync = true)
  public String findQuestionnaireMessage(String questionnaire, String messageKey) {
    log.info("get into caching method with questionnaire {} and message key {}", questionnaire, messageKey);
    return messageKey;
  } 
}
