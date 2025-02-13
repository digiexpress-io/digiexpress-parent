package io.digiexpress.eveli.client.spi.comms;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.digiexpress.eveli.client.api.CommsClient;
import io.digiexpress.eveli.client.api.CommsClient.EmailBuilder;
import io.digiexpress.eveli.client.config.EveliPropsEmail;
import io.resys.thena.support.RepoAssert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

/**
 * Email notification builder implementation based on REST API to service.
 * This implementation assumes that REST API accepts EmailBuilderDelegate.EmailRequest 
 * as request body and returns @EmailResponse.
 */
@RequiredArgsConstructor
public class EmailBuilderDelegate implements CommsClient.EmailBuilder {
  
  private final EveliPropsEmail config;
  private final RestTemplate client;
  private final EmailRequest request = new EmailRequest();
  private final EmailBuilderLogger logger = new EmailBuilderLogger();
  
  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class EmailRequest {
    private String notificationTitle; 
    private String notificationMessage;
    @Singular
    private List<String> recipientAddresses;
    private String refId;
  }

  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class EmailResponse {
    /**
     * Possible codes:
     * <ul>
     * <li> 0 - message sent
     * <li> 403 - No valid email addresses
     * <li> 404 - No email addresses
     * <li> 405 - Email sending disabled
     * <li> 500 - technical error
     * </ul>
     */
    private int responseCode;
    private String message;
    @Builder.Default
    private int emailCount = 0;
  }

  @Override
  public EmailBuilder refId(String refId) {
    request.setRefId(refId);
    return this;
  }
  @Override
  public EmailBuilder title(String notificationTitle) {
    request.setNotificationTitle(notificationTitle);
    return this;
  }
  @Override
  public EmailBuilder message(String notificationMessage) {
    request.setNotificationMessage(notificationMessage);
    return this;
  }
  @Override
  public EmailBuilder recipientAddress(String recipientAddress) {
    if (request.getRecipientAddresses() == null) {
      request.setRecipientAddresses(new ArrayList<>());
    }
    request.getRecipientAddresses().add(recipientAddress);
    return this;
  }
  @Override
  public EmailBuilder recipientAddress(List<String> recipientAddress) {
    if (request.getRecipientAddresses() == null) {
      request.setRecipientAddresses(new ArrayList<>());
    }
    request.getRecipientAddresses().addAll(recipientAddress);
    return this;
  }

  @Override
  public void build() {
    if(!Boolean.TRUE.equals(config.getEnabled())) {
      logger.emailDisabled();
      return;
    }
    
    RepoAssert.notEmpty(request.notificationTitle, () -> "title must be defined!");
    RepoAssert.notEmpty(request.refId, () -> "refId must be defined!");
    RepoAssert.notEmpty(request.notificationMessage, () -> "message must be defined!");
    
    final var receivers = Optional.ofNullable(request.getRecipientAddresses()).orElse(Collections.emptyList());
    
    logger.emailCreated(receivers, request.notificationTitle, request.refId);
    if (receivers.isEmpty()) {
      logger.noRecipients();
      return;
    } 

    try {
      sendEmailNotification();
    } catch (Exception e) {
      logger.emailFailed(receivers, e);
    } finally {
      logger.close();
    }

  }

  private void sendEmailNotification() {
    if (StringUtils.isEmpty(config.getServiceUrl())) {
      logger.emailDisabledServiceUrlMissing();
      return;
    }

    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);

    final var requestEntity = new HttpEntity<EmailRequest>(request, headers);
    final var serviceUrl = UriComponentsBuilder.fromHttpUrl(config.getServiceUrl()).toUriString();
    
    logger.emailSentWithDelegate(serviceUrl);
    final var response = client.exchange(serviceUrl, HttpMethod.POST, requestEntity, EmailResponse.class);
    
    if (response.getStatusCode().is2xxSuccessful()) {
      final var result = response.getBody();
      logger.emailSent(result.getEmailCount());
    } else {
      logger.emailFailed(response);
    }
  }
}
