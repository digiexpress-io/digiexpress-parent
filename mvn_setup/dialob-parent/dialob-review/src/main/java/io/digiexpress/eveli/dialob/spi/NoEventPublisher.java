package io.digiexpress.eveli.dialob.spi;

/*-
 * #%L
 * dialob-review
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

import java.net.InetAddress;

import io.dialob.api.proto.Actions;
import io.dialob.questionnaire.service.api.event.QuestionnaireEventPublisher;

public class NoEventPublisher extends QuestionnaireEventPublisher {

  public NoEventPublisher() {
    super(null, null);

  }

  @Override
  public void opened(String questionnaireId) {
  }

  @Override
  public void created(String questionnaireId) {

  }

  @Override
  public void completed(String tenantId, String questionnaireId) {
  }

  @Override
  public void actions(String questionnaireId, Actions actions) {
  }

  @Override
  public void clientConnected(String questionnaireId, InetAddress client) {
  }

  @Override
  public void clientDisconnected(String questionnaireId, InetAddress client, int closeStatus) {

  }

}
