package io.digiexpress.eveli.client.web.resources.gamut;

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

import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.cache.DuplicateDetectionCache;
import io.digiexpress.eveli.client.event.runnables.QuestionnaireCompletionRunnable;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("dialobSubmitCallback")
@Slf4j
public class DialobCallbackController {
  
  private final PortalClient client;
  private final ThreadPoolTaskScheduler submitTaskScheduler;
  private final DuplicateDetectionCache cache;
  private final Long submitMessageDelay;
  
  public DialobCallbackController(
      PortalClient client, 
      ThreadPoolTaskScheduler submitTaskScheduler,
      DuplicateDetectionCache cache,
      Long submitMessageDelay) {
    this.client = client;
    this.submitTaskScheduler = submitTaskScheduler;
    this.cache = cache;
    this.submitMessageDelay = submitMessageDelay;
  }

  @PostMapping
  public ResponseEntity<String> handleCallback(@RequestBody Questionnaire questionnaire) {
    String questionnaireId = questionnaire.getId();
    log.info("Dialob callback handler: questionnaire id: {}, start processing", questionnaireId);
    
    if (isMessageUnique(questionnaireId)) {
      handleDialobCompletion(questionnaireId);
    } else {
      log.info("Message: Dialob, questionnaire ID: {}, result: handling skipped, duplicate message", questionnaireId);
    }

    return ResponseEntity.ok("OK");
  }

  private void handleDialobCompletion(String questionnaireId) {
    if (submitMessageDelay == 0) {
      log.debug("Dialob callback handler: questionnaire id: {}, synchronous processing", questionnaireId);
      client.dialob().complete(questionnaireId);
      log.info("Message: Dialob, questionnaire ID: {}, result: handled", questionnaireId);
    }
    else {
      log.debug("Dialob callback handler: questionnaire id: {}, asynchronous processing with delay {} ms.", questionnaireId, submitMessageDelay);
      // create runnable and schedule its execution by some amount to overcome issue #2536
      // this should allow enough time to get all changes stored even if completion message arrives before last answer
      Runnable runnable = new QuestionnaireCompletionRunnable(client, questionnaireId);
      submitTaskScheduler.schedule(runnable, new Date(System.currentTimeMillis() + submitMessageDelay));
      log.info("Dialob callback handler: questionnaire id: {}, result: scheduled with delay {} seconds", questionnaireId, submitMessageDelay/1000);
    }
  }
  
  private boolean isMessageUnique(String questionnaireId) {
    String messageId = generateId();
    String cachedMessageId = cache.findQuestionnaireMessage(questionnaireId, messageId);
    log.info("Message: Dialob, questionnaire ID: {}, cached message id: {}", questionnaireId, cachedMessageId);
    return StringUtils.equals(messageId, cachedMessageId);
      
  }
    
  private String generateId() {
    return UUID.randomUUID().toString().toUpperCase().replace("-", "");
  }
}