package io.digiexpress.eveli.client.event.runnables;

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

import io.digiexpress.eveli.client.api.PortalClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class QuestionnaireCompletionRunnable  implements Runnable {

  private final PortalClient client;
  private final String questionnaireId;

  @Override
  public void run() {
    log.info("Runnable Task with questionnaire id {} started", questionnaireId);
    client.hdes().execute(questionnaireId);
    log.info("Runnable Task with questionnaire id {} completed", questionnaireId);
  }
}
