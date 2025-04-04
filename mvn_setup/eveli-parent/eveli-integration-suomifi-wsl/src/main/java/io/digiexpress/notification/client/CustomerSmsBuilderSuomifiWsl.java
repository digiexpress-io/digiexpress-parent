package io.digiexpress.notification.client;

/*-
 * #%L
 * eveli-integration-suomifi-wsl
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
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;

import io.digiexpress.eveli.client.api.CommsClient.CustomerMessageBuilder;
import io.digiexpress.notification.client.NotificationServiceClientAPI.Client;
import io.digiexpress.notification.client.NotificationServiceClientAPI.ClientType;
import io.digiexpress.notification.client.NotificationServiceClientAPI.NotificationRequest;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class CustomerSmsBuilderSuomifiWsl implements CustomerMessageBuilder {
  private final NotificationServiceClientAPI client;
  private final NotificationRequest.NotificationRequestBuilder request = NotificationRequest.builder();
  private final List<TempEmail> emails = new ArrayList<>();
  private String messageId;
  private String senderId;

  
  @Builder @Data
  private static class TempEmail {
    private String locale; 
    private String title; 
    private String content;
  }
  @Override
  public CustomerMessageBuilder sms(String title, String content) {
    request
      .notificationTitle(title)
      .notificationMessage(content);
    return this;
  }
  @Override
  public CustomerMessageBuilder email(String locale, String title, String content) {
    emails.add(TempEmail.builder()
        .title(title)
        .locale(locale)
        .content(content)
        .build());
    return this;
  }

  @Override
  public CustomerMessageBuilder senderId(String senderId) {
    this.senderId = senderId;
    return null;
  }
  @Override
  public CustomerMessageBuilder messageId(String messageId) {
    this.messageId = messageId;
    return this;
  }

  @Override
  public void build() {
    final var request = this.request
      .client(Client.builder().clientId(senderId).clientType(senderId.length() == 9 ? ClientType.CRN : ClientType.SSN).build())
      .emailTitle(emails.stream().collect(Collectors.toMap(e -> e.locale(), e -> e.title())))
      .emailMessage(emails.stream().collect(Collectors.toMap(e -> e.locale(), e -> e.content())))
      .build();
    
    try {
      client.sendClientNotification(request, messageId);
    } catch (DatatypeConfigurationException e1) {
      throw new RuntimeException(e1.getMessage(), e1);
    }
  }

}
