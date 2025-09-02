package io.digiexpress.eveli.client.spi.taskaudit;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.digiexpress.eveli.client.api.TaskAuditClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.thena.mq.client.api.ThenaMqAppConfig;
import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskAuditClientImpl implements TaskAuditClient {

  private final TaskClient taskClient;
  private final Optional<ThenaMqClient> mqClient;
  private final Optional<ThenaMqAppConfig> mqConfig;
  
  @Override
  public TaskAuditQuery createTaskAuditQuery() {
    // TODO Auto-generated method stub
    return null;
  }

}
