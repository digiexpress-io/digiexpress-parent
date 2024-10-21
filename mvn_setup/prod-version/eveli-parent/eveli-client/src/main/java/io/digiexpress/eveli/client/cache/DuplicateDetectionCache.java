package io.digiexpress.eveli.client.cache;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DuplicateDetectionCache {
  @Cacheable(value="completedQuestionnaire", key="#p0", sync = true)
  public String findQuestionnaireMessage(String questionnaire, String messageKey) {
    log.info("get into caching method with questionnaire {} and message key {}", questionnaire, messageKey);
    return messageKey;
  }
  
}
