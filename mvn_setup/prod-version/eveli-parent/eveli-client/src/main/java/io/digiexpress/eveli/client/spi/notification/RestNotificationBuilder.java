package io.digiexpress.eveli.client.spi.notification;

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

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.digiexpress.eveli.client.api.NotificationCommands.Client;
import io.digiexpress.eveli.client.api.NotificationCommands.ClientType;
import io.digiexpress.eveli.client.api.NotificationCommands.NotificationBuilder;
import io.digiexpress.eveli.client.api.NotificationCommands.NotificationRequest;
import io.digiexpress.eveli.client.api.NotificationCommands.NotificationResponse;
import io.digiexpress.eveli.client.config.EveliPropsNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class RestNotificationBuilder implements NotificationBuilder {

  private final EveliPropsNotification props;
  private final RestTemplate client;
  private NotificationRequest request = new NotificationRequest();
  
  @Override
  public NotificationBuilder title(String title) {
    this.request.setNotificationTitle(title);
    return this;
  }

  @Override
  public NotificationBuilder content(String content) {
    this.request.setNotificationMessage(content);
    return this;
  }

  @Override
  public NotificationBuilder userId(String userId) {
    if (this.request.getClient() == null) {
      this.request.setClient(new Client());
    }
    this.request.getClient().setClientId(userId);
    return this;
  }

  @Override
  public NotificationBuilder userIdType(String userId, ClientType userType) {
    if (this.request.getClient() == null) {
      this.request.setClient(new Client());
    }
    this.request.getClient().setClientId(userId);
    this.request.getClient().setClientType(userType);
    return this;
  }

  @Override
  public NotificationBuilder notificationId(String notificationId) {
    this.request.setNotificationId(notificationId);
    return this;
  }

  @Override
  public NotificationResponse build() {
    NotificationResponse result = null;
    final String logPrefix = "Notification sending request, refId: " + request.getNotificationId();
    log.info("{}, starting request processing", logPrefix);
    log.debug("{}, log request: {}", logPrefix, request);
    if (!props.getEnabled()) {
      log.info("{}, result: cancelled, reason: notification sending disabled in configuration.", logPrefix);
    }
    else {
      try {
        result = sendNotification(logPrefix);
        log.info("{}, result: {}", logPrefix, result != null ? result.getResponseCode() : "null");
      }
      catch (Exception e) {
        log.error("{}, result: error", logPrefix, e);
      }
    }
    return result;
  }

  private NotificationResponse sendNotification(String logPrefix) {
    if (StringUtils.isEmpty(props.getServiceUrl())) {
      log.warn("{}, notification url is not configured, skipping sending message", logPrefix);
      return null;
    }
    
    final HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    headers.setContentType(MediaType.APPLICATION_JSON);

    final HttpEntity<NotificationRequest> requestEntity = new HttpEntity<>(request, headers);
    String serviceUrl = UriComponentsBuilder.fromHttpUrl(props.getServiceUrl()).toUriString();
    log.debug("{}, sending notification message to url {}", logPrefix, props.getServiceUrl());
    ResponseEntity<NotificationResponse> response = client.exchange(serviceUrl, 
        HttpMethod.POST, requestEntity, NotificationResponse.class);
    if (response.getStatusCode().is2xxSuccessful()) {
      log.debug("{}, notification sending completed, result: {}", logPrefix, response.getBody());
    }
    else {
      log.warn("{}, notification sending response code {}, sending failed", logPrefix, response.getStatusCode());
    }
    return response.getBody();
  }
}
