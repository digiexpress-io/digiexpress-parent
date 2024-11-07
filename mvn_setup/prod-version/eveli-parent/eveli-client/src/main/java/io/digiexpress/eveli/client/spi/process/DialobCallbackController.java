package io.digiexpress.eveli.client.spi.process;

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

import java.util.concurrent.TimeUnit;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import io.digiexpress.eveli.client.api.GamutClient.UserActionFillEvent;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.dialob.api.DialobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class DialobCallbackController {
  
  private final ThreadPoolTaskScheduler submitTaskScheduler;
  private final ProcessClient processClient;
  private final DialobClient dialobClient;
  
  @Scheduled(fixedRate = 3, timeUnit = TimeUnit.MINUTES)
  public void reportCurrentTime() {
    
  }
  
  @EventListener
  public void handleFillCompleted(UserActionFillEvent event) {
  
    log.info("handle event, {}", event);
  }
  
  
}
