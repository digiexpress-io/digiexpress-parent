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
package io.dialob.session.boot;

import io.dialob.questionnaire.service.api.session.QuestionnaireSession;
import io.dialob.questionnaire.service.api.session.QuestionnaireSessionService;
import io.dialob.settings.CorsSettings;
import io.dialob.settings.SessionSettings;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.cors.CorsConfiguration;

import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantBasedCorsConfigurationSourceTest {

  @Test
  public void shouldNotGivePolicyIfNonIsConfigured() throws Exception {
    SessionSettings sessionSettings = new SessionSettings();
    QuestionnaireSessionService questionnaireSessionService = Mockito.mock(QuestionnaireSessionService.class);
    final TenantFromRequestResolver tenantFromRequestResolver = new SessionRestTenantFromRequestResolver(questionnaireSessionService);

    TenantBasedCorsConfigurationSource source = new TenantBasedCorsConfigurationSource(sessionSettings.getRest().getCors()::get, tenantFromRequestResolver);

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    assertNull(source.getCorsConfiguration(request));

    verify(request).getRequestURI();
    verifyNoMoreInteractions(request, questionnaireSessionService);
  }

  @Test
  public void shouldGiveDefaultPolicyIfRequestDoNotMatch() throws Exception {
    SessionSettings sessionSettings = new SessionSettings();
    QuestionnaireSessionService questionnaireSessionService = Mockito.mock(QuestionnaireSessionService.class);
    final TenantFromRequestResolver tenantFromRequestResolver = new SessionRestTenantFromRequestResolver(questionnaireSessionService);


    CorsSettings corsSettings = new CorsSettings();
    corsSettings.getAllowedOrigins().add("*");
    sessionSettings.getRest().getCors().put("default", corsSettings);

    TenantBasedCorsConfigurationSource source = new TenantBasedCorsConfigurationSource(sessionSettings.getRest().getCors()::get, tenantFromRequestResolver);

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    CorsConfiguration corsConfiguration = source.getCorsConfiguration(request);
    assertNotNull(corsConfiguration);
    assertIterableEquals(asList("*"), corsConfiguration.getAllowedOrigins());

    verify(request).getRequestURI();
    verifyNoMoreInteractions(request, questionnaireSessionService);
  }

  @Test
  public void shouldResolveTenantFromQuestionnaireAndGivePolicyConfiguredToThatTenant() throws Exception {
    SessionSettings sessionSettings = new SessionSettings();
    QuestionnaireSessionService questionnaireSessionService = Mockito.mock(QuestionnaireSessionService.class);
    final TenantFromRequestResolver tenantFromRequestResolver = new SessionRestTenantFromRequestResolver(questionnaireSessionService);
    QuestionnaireSession questionnaireSession = Mockito.mock(QuestionnaireSession.class);


    CorsSettings corsSettings = new CorsSettings();
    corsSettings.getAllowedOrigins().add("*");
//    sessionSettings.getRest().getCors().put("default", corsSettings);
    sessionSettings.getRest().getCors().put("tenant-id", corsSettings);

    TenantBasedCorsConfigurationSource source = new TenantBasedCorsConfigurationSource(sessionSettings.getRest().getCors()::get, tenantFromRequestResolver);

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/session-id");
    when(questionnaireSessionService.findOne("session-id")).thenReturn(questionnaireSession);
    when(questionnaireSession.getTenantId()).thenReturn("tenant-id");

    CorsConfiguration corsConfiguration = source.getCorsConfiguration(request);
    assertNotNull(corsConfiguration);
    assertIterableEquals(asList("*"), corsConfiguration.getAllowedOrigins());

    verify(request).getRequestURI();
    verify(questionnaireSessionService).findOne("session-id");
    verify(questionnaireSession).getTenantId();
    verifyNoMoreInteractions(request, questionnaireSessionService,questionnaireSession);
  }

  @Test
  public void shouldResolveTenantFromQuestionnaireAndReturnNullWhenDefaultIsNotConfigured() throws Exception {
    SessionSettings sessionSettings = new SessionSettings();
    QuestionnaireSessionService questionnaireSessionService = Mockito.mock(QuestionnaireSessionService.class);
    final TenantFromRequestResolver tenantFromRequestResolver = new SessionRestTenantFromRequestResolver(questionnaireSessionService);
    QuestionnaireSession questionnaireSession = Mockito.mock(QuestionnaireSession.class);


    CorsSettings corsSettings = new CorsSettings();
    corsSettings.getAllowedOrigins().add("*");
    sessionSettings.getRest().getCors().put("tenant-id", corsSettings);

    TenantBasedCorsConfigurationSource source = new TenantBasedCorsConfigurationSource(sessionSettings.getRest().getCors()::get, tenantFromRequestResolver);

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/session-id");
    when(questionnaireSessionService.findOne("session-id")).thenReturn(questionnaireSession);
    when(questionnaireSession.getTenantId()).thenReturn("tenant-id-unk");

    CorsConfiguration corsConfiguration = source.getCorsConfiguration(request);
    assertNull(corsConfiguration);

    verify(request).getRequestURI();
    verify(questionnaireSessionService).findOne("session-id");
    verify(questionnaireSession).getTenantId();
    verifyNoMoreInteractions(request, questionnaireSessionService,questionnaireSession);
  }

  @Test
  public void shouldResolveTenantFromQuestionnaireAndGiveDefaultPolicyWhenThereIsNonForTenant() throws Exception {
    SessionSettings sessionSettings = new SessionSettings();
    QuestionnaireSessionService questionnaireSessionService = Mockito.mock(QuestionnaireSessionService.class);
    final TenantFromRequestResolver tenantFromRequestResolver = new SessionRestTenantFromRequestResolver(questionnaireSessionService);
    QuestionnaireSession questionnaireSession = Mockito.mock(QuestionnaireSession.class);


    CorsSettings corsSettings = new CorsSettings();
    corsSettings.getAllowedOrigins().add("*");
    sessionSettings.getRest().getCors().put("default", corsSettings);
    corsSettings = new CorsSettings();
    corsSettings.getAllowedOrigins().add("tenant");
    sessionSettings.getRest().getCors().put("tenant-id", corsSettings);

    TenantBasedCorsConfigurationSource source = new TenantBasedCorsConfigurationSource(sessionSettings.getRest().getCors()::get, tenantFromRequestResolver);

    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/session-id");
    when(questionnaireSessionService.findOne("session-id")).thenReturn(questionnaireSession);
    when(questionnaireSession.getTenantId()).thenReturn("tenant-id-none");

    CorsConfiguration corsConfiguration = source.getCorsConfiguration(request);
    assertNotNull(corsConfiguration);
    assertIterableEquals(asList("*"), corsConfiguration.getAllowedOrigins());

    verify(request).getRequestURI();
    verify(questionnaireSessionService).findOne("session-id");
    verify(questionnaireSession).getTenantId();
    verifyNoMoreInteractions(request, questionnaireSessionService,questionnaireSession);
  }

}
