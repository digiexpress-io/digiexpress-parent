package io.digiexpress.notification.client;

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
  public CustomerMessageBuilder ssn(String ssn) {
    request.client(Client.builder().clientId(ssn).clientType(ClientType.SSN).build());
    return this;
  }
  @Override
  public CustomerMessageBuilder crn(String crn) {
    request.client(Client.builder().clientId(crn).clientType(ClientType.CRN).build());
    return this;
  }
  @Override
  public void build() {
    final var request = this.request
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
