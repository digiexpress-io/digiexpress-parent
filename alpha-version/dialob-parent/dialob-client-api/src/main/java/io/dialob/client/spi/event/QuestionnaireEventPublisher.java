/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.client.spi.event;

import java.net.InetAddress;

import javax.annotation.Nonnull;

import io.dialob.api.proto.Actions;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuestionnaireEventPublisher {

  private final EventPublisher applicationEventPublisher;


  public void opened(@Nonnull String questionnaireId) {
    applicationEventPublisher.publish(ImmutableQuestionnaireOpenedEvent.builder()
      .questionnaireId(questionnaireId)
      .build());
  }

  public void created(@Nonnull String questionnaireId) {
    applicationEventPublisher.publish(ImmutableQuestionnaireCreatedEvent.builder()
      .questionnaireId(questionnaireId)
      .build());
  }

  public void completed(@Nonnull String questionnaireId) {
    applicationEventPublisher.publish(ImmutableQuestionnaireCompletedEvent.builder()
      .questionnaireId(questionnaireId)
      .build());
  }

  public void actions(@Nonnull String questionnaireId, @Nonnull Actions actions) {
    applicationEventPublisher.publish(ImmutableQuestionnaireActionsEvent.builder()
      .questionnaireId(questionnaireId)
      .actions(actions)
      .build());
  }

  public void clientConnected(@Nonnull String questionnaireId, InetAddress client) {
    applicationEventPublisher.publish(ImmutableQuestionnaireClientConnectedEvent.builder()
      .questionnaireId(questionnaireId)
      .client(client)
      .build());
  }

  public void clientDisconnected(@Nonnull String questionnaireId, InetAddress client, int closeStatus) {
    applicationEventPublisher.publish(ImmutableQuestionnaireClientDisconnectedEvent.builder()
      .questionnaireId(questionnaireId)
      .client(client)
      .closeStatus(closeStatus)
      .build());
  }
}
