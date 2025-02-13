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

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.digiexpress.eveli.client.api.CommsClient.NotificationBuilder;
import io.digiexpress.eveli.client.config.EveliPropsNotification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class NotificationBuilderDelegate implements NotificationBuilder {

  private final EveliPropsNotification props;
  private final RestTemplate client;
  private final NotificationRequest request = new NotificationRequest();

  
  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class NotificationRequest {
    private Client client;
    private String notificationId;
    private String notificationTitle; 
    private String notificationMessage;
  }
  
  public static enum ClientType {
    SSN,
    CRN
  }
  
  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Client {
    private String clientId;
    private ClientType clientType;
  }

  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class NotificationResponse {
    /**
     * Possible codes:
     * <ul>
     * <li> 0 - message sent
     * <li> 204 - Client has not enabled message receiving 
     * <li> 400..499 - business error code, specific to service
     * <li> 500 - technical error
     * <li> 307 - message sending is disabled in system
     * </ul>
     */
    private int responseCode;
    private String message;
  }

  
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
  public NotificationBuilder ssn(String userId) {
    return this.userIdType(userId, ClientType.SSN);
  }
  @Override
  public NotificationBuilder crn(String userId) {
    return this.userIdType(userId, ClientType.CRN);
  }
  private NotificationBuilder userIdType(String userId, ClientType userType) {
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
  public void build() {
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
