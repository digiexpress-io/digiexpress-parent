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
package io.dialob.questionnaire.service;

import io.dialob.api.proto.ImmutableActions;
import io.dialob.questionnaire.service.api.event.QuestionnaireEventPublisher;
import io.dialob.questionnaire.service.api.session.ImmutableQuestionnaireSession;
import io.dialob.questionnaire.service.api.session.QuestionnaireSession;
import io.dialob.questionnaire.service.api.session.QuestionnaireSessionSaveService;
import io.dialob.questionnaire.service.api.session.QuestionnaireSessionService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class QuestionnaireSessionProcessingServiceTest {

  @Test
  public void shouldNotPublicCompleteEventIfNotCompleted() throws Exception {
    QuestionnaireSessionService questionnaireSessionService = mock(QuestionnaireSessionService.class);
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    CacheManager sessionCacheManager = mock(CacheManager.class);
    QuestionnaireSessionSaveService questionnaireSessionSaveService = mock(QuestionnaireSessionSaveService.class);
    QuestionnaireSession session = mock(QuestionnaireSession.class);
    QuestionnaireEventPublisher eventPublisher = mock(QuestionnaireEventPublisher.class);

    QuestionnaireSessionProcessingService questionnaireSessionProcessingService = new QuestionnaireSessionProcessingService(
      questionnaireSessionService,
      meterRegistry,
      Optional.of(sessionCacheManager),
      questionnaireSessionSaveService,
      eventPublisher);

    when(questionnaireSessionService.findOne("q1")).thenReturn(session);
    when(session.isCompleted()).thenReturn(false);
    when(session.dispatchActions(eq("123"),any(Collection.class))).thenReturn(
      ImmutableQuestionnaireSession.DispatchActionsResult.builder()
        .isDidComplete(false)
        .actions(ImmutableActions.builder().rev("124").build()).build());

    questionnaireSessionProcessingService.answerQuestion("q1", "123", Collections.emptyList());

    verify(questionnaireSessionService).findOne("q1");
    verify(sessionCacheManager).getCache("sessionCache");
    verify(session).dispatchActions(eq("123"),any(Collection.class));
    verify(session).isCompleted();
    verifyNoMoreInteractions(questionnaireSessionService, sessionCacheManager, questionnaireSessionSaveService, session, eventPublisher);

  }


  @Test
  public void shouldPublishCompleteEventIfCompleted() throws Exception {
    QuestionnaireSessionService questionnaireSessionService = mock(QuestionnaireSessionService.class);
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    CacheManager sessionCacheManager = mock(CacheManager.class);
    QuestionnaireSessionSaveService questionnaireSessionSaveService = mock(QuestionnaireSessionSaveService.class);
    QuestionnaireSession session = mock(QuestionnaireSession.class);
    QuestionnaireEventPublisher eventPublisher = mock(QuestionnaireEventPublisher.class);

    QuestionnaireSessionProcessingService questionnaireSessionProcessingService = new QuestionnaireSessionProcessingService(
      questionnaireSessionService,
      meterRegistry,
      Optional.of(sessionCacheManager),
      questionnaireSessionSaveService,
      eventPublisher);

    when(questionnaireSessionService.findOne("q1")).thenReturn(session);
    when(session.getSessionId()).thenReturn(Optional.of("q1"));
    when(session.getTenantId()).thenReturn("t1");
    when(session.isCompleted()).thenReturn(false);
    when(session.dispatchActions(eq("123"),any(Collection.class))).thenReturn(
      ImmutableQuestionnaireSession.DispatchActionsResult.builder()
        .isDidComplete(true)
        .actions(ImmutableActions.builder().rev("124").build()).build());

    questionnaireSessionProcessingService.answerQuestion("q1", "123", Collections.emptyList());

    verify(questionnaireSessionService).findOne("q1");
    verify(session).dispatchActions(eq("123"),any(Collection.class));
    verify(questionnaireSessionSaveService).save(session);
    verify(eventPublisher).completed("t1","q1");
    verify(session).getSessionId();
    verify(session).getTenantId();
    verify(session).isCompleted();

    verifyNoMoreInteractions(questionnaireSessionService, sessionCacheManager, questionnaireSessionSaveService, session, eventPublisher);

  }

  @Test
  public void shouldRejectUpdatesToCompletedQuestionnaires() throws Exception {
    QuestionnaireSessionService questionnaireSessionService = mock(QuestionnaireSessionService.class);
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    CacheManager sessionCacheManager = mock(CacheManager.class);
    QuestionnaireSessionSaveService questionnaireSessionSaveService = mock(QuestionnaireSessionSaveService.class);
    QuestionnaireSession session = mock(QuestionnaireSession.class);
    QuestionnaireEventPublisher eventPublisher = mock(QuestionnaireEventPublisher.class);

    QuestionnaireSessionProcessingService questionnaireSessionProcessingService = new QuestionnaireSessionProcessingService(
      questionnaireSessionService,
      meterRegistry,
      Optional.of(sessionCacheManager),
      questionnaireSessionSaveService,
      eventPublisher);

    when(questionnaireSessionService.findOne("q1")).thenReturn(session);
    when(session.getSessionId()).thenReturn(Optional.of("q1"));
    when(session.getTenantId()).thenReturn("t1");
    when(session.isCompleted()).thenReturn(true);

    questionnaireSessionProcessingService.answerQuestion("q1", "123", Collections.emptyList());

    verify(questionnaireSessionService).findOne("q1");
    verify(session).isCompleted();
    verify(session).getRev();

    verifyNoMoreInteractions(questionnaireSessionService, sessionCacheManager, questionnaireSessionSaveService, session, eventPublisher);

  }


}
