package io.resys.metis.spi.ai;

/*-
 * #%L
 * metis-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StructuredChatService {

  private static final int DEFAULT_ATTEMPTS = 2;

  private final ChatClient chatClient;
  private final int maxAttempts;

  public StructuredChatService(ChatClient chatClient) {
    this(chatClient, DEFAULT_ATTEMPTS);
  }

  public StructuredChatService(ChatClient chatClient, int maxAttempts) {
    this.chatClient = chatClient;
    this.maxAttempts = Math.max(1, maxAttempts);
  }

  public <T> Optional<T> generate(String prompt, Class<T> type, String subject) {
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        final var answer = chatClient.prompt().user(prompt).call().entity(type);
        if (answer != null) {
          return Optional.of(answer);
        }
      } catch (Exception e) {
        log.warn("Metis chat attempt {}/{} failed for {}, because of: {}",
            attempt, maxAttempts, subject, e.getMessage());
      }
    }
    return Optional.empty();
  }
}
