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
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.digiexpress.eveli.client.api.CommsClient;
import io.digiexpress.eveli.client.api.CommsClient.EmailNotificationBuilder;
import io.digiexpress.eveli.client.api.CommsClient.EmailRequest;
import io.digiexpress.eveli.client.api.CommsClient.EmailResponse;
import io.digiexpress.eveli.client.config.EveliPropsEmail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Email notification builder implementation based on REST API to service.
 * This implementation assumes that REST API accepts io.digiexpress.eveli.client.api.NotificationCommands.EmailRequest 
 * as request body and returns @EmailResponse.
 */
@Slf4j
@RequiredArgsConstructor
public class RestEmailNotificationBuilder implements CommsClient.EmailNotificationBuilder {

    private final EveliPropsEmail emailProps;
    private final RestTemplate client;
    
    private EmailRequest request = new EmailRequest();

    @Override
    public EmailNotificationBuilder title(String notificationTitle) {
      request.setNotificationTitle(notificationTitle);
      return this;
    }

    @Override
    public EmailNotificationBuilder message(String notificationMessage) {
      request.setNotificationMessage(notificationMessage);
      return this;
    }

    @Override
    public EmailNotificationBuilder address(String recipientAddress) {
      if (request.getRecipientAddresses() == null) {
        request.setRecipientAddresses(new ArrayList<>());
      }
      request.getRecipientAddresses().add(recipientAddress);
      return this;
    }

    @Override
    public EmailNotificationBuilder refId(String refId) {
      request.setRefId(refId);
      return this;
    }

    @Override
    public EmailNotificationBuilder addresses(List<String> recipientAddress) {
      if (request.getRecipientAddresses() == null) {
        request.setRecipientAddresses(new ArrayList<>());
      }
      request.getRecipientAddresses().addAll(recipientAddress);
      return this;
    }

    @Override
    public void build() {
      final String logPrefix = "Email sending request, refId: " + request.getRefId();
      List<String> emailAddressList = request.getRecipientAddresses();
      
      log.info("{}, title {}, number of recipients: {}", logPrefix, request.getNotificationTitle(), 
          emailAddressList != null ? emailAddressList.size() : 0);
      log.debug("{}, recipients: {}", logPrefix, emailAddressList);
      
      if (!emailProps.getEnabled()) {
        log.info("{}, result: cancelled, reason: email sending disabled in configuration.", logPrefix);
      }
      else {
        try {
          sendEmailNotification(logPrefix);
          log.info("{}, result: sending completed.", logPrefix);
        }
        catch (Exception e) {
          log.error("{}, result: error", logPrefix, e);
        }
      }
    }

    private void sendEmailNotification(String logPrefix) {
      if (StringUtils.isEmpty(emailProps.getServiceUrl())) {
        log.warn("{}, email service url is not configured, sending skipped", logPrefix);
        return;
      }
      
      final HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      headers.setContentType(MediaType.APPLICATION_JSON);

      final HttpEntity<EmailRequest> requestEntity = new HttpEntity<>(request, headers);
      String serviceUrl = UriComponentsBuilder.fromHttpUrl(emailProps.getServiceUrl()).toUriString();
      log.debug("{}, sending email message to service url {}", logPrefix, emailProps.getServiceUrl());
      ResponseEntity<EmailResponse> response = client.exchange(serviceUrl, 
          HttpMethod.POST, requestEntity, EmailResponse.class);
      if (response.getStatusCode().is2xxSuccessful()) {
        EmailResponse result = response.getBody();
        log.debug("{}, email sending response OK, result: {}", logPrefix, result != null ? result.getResponseCode() : "null");
      }
      else {
        log.warn("{}, email sending response code {}, email sending failed with body: {}", logPrefix, response.getStatusCode(), response.getBody());
      }
      
    }
}
