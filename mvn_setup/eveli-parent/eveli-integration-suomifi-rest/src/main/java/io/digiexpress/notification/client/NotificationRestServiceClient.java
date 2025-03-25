package io.digiexpress.notification.client;

/*-
 * #%L
 * eveli-integration-suomifi-rest
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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.notification.client.AccessTokenTypes.AccessTokenRequestBody;
import io.digiexpress.notification.client.AccessTokenTypes.AccessTokenResponse;
import io.digiexpress.notification.client.MailboxesActiveTypes.EndUserId;
import io.digiexpress.notification.client.MailboxesActiveTypes.MailboxesActiveRequest;
import io.digiexpress.notification.client.MailboxesActiveTypes.MailboxesActiveResponse;
import io.digiexpress.notification.client.RestMessageTypes.BodyFormat;
import io.digiexpress.notification.client.RestMessageTypes.CustomisedMessageNotification;
import io.digiexpress.notification.client.RestMessageTypes.CustomisedMessageNotificationContent;
import io.digiexpress.notification.client.RestMessageTypes.CustomisedMessageNotificationTitle;
import io.digiexpress.notification.client.RestMessageTypes.MessageNotifications;
import io.digiexpress.notification.client.RestMessageTypes.MessageServiceType;
import io.digiexpress.notification.client.RestMessageTypes.NewElectronicMessageFromClientOrganization;
import io.digiexpress.notification.client.RestMessageTypes.NewElectronicOnly;
import io.digiexpress.notification.client.RestMessageTypes.Recipient;
import io.digiexpress.notification.client.RestMessageTypes.Reminder;
import io.digiexpress.notification.client.RestMessageTypes.ReplyAllowedBy;
import io.digiexpress.notification.client.RestMessageTypes.RestMessageResponse;
import io.digiexpress.notification.client.RestMessageTypes.Sender;
import io.digiexpress.notification.client.RestMessageTypes.SenderDetailsInNotifications;
import io.digiexpress.notification.client.RestMessageTypes.UnreadMessageNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class NotificationRestServiceClient implements NotificationRestServiceClientAPI {
  public static final int NOTIFICATION_DISABLED_CODE = 307;
  private final SuomiFiRestProperties orgProperties;
  private final ObjectMapper mapper;

  @Override
  public NotificationRestResponse sendClientNotification(NotificationRequest request, String requestId) {
    NotificationRestResponse result = new NotificationRestResponse();
    log.debug("Rest notification: request id: {}, starting process", requestId);
    try {
      AccessTokenResponse accessTokenResponse = fetchAuthenticationToken();
      String accessToken = accessTokenResponse.getAccess_token();
      boolean isClientMailboxActive = checkUserMailbox(request.getClientId(), accessToken);
      if (isClientMailboxActive) {
        log.debug("Rest notification: request id: {}, client mailbox active, start sending", requestId);
        Long notificationId = sendNotification(request, requestId, accessToken);
        log.debug("Rest notification: request id: {}, send completed, notification id: {}", requestId, notificationId);
        result.setMessageId(notificationId);
        result.setCode(200);
        result.setSuccess(true);  
      }
      else {
        log.debug("Rest notification: request id: {}, client mailbox not active", requestId);
        result.setCode(NOTIFICATION_DISABLED_CODE);
        result.setReason("Notifications disabled for user");
        result.setSuccess(false); 
      }
    }
    catch (RestApiCallException e) {
      log.info("Rest notification: request id: {}, error in call", requestId, e);
      result.setSuccess(false);
      result.setCode(e.getCode().value());
      result.setReason(e.getMessage());
      if (e.getResponse().getValidationErrors() != null) {
        log.info("Rest notification: request id: {}, validation result: {}", 
            e.getResponse().getValidationErrors().stream().map(el->el.getError()).collect(Collectors.joining(",")));
        result.setValidationErrors(e.getResponse().getValidationErrors()
            .stream()
            .map(ve -> new NotificationRestServiceClientAPI.ValidationError(ve.getError()))
            .collect(Collectors.toList()));
      }
    }
    
    return result;
  }

  private Long sendNotification(NotificationRequest request, String requestId, String token) {
    
    NewElectronicMessageFromClientOrganization body = new NewElectronicMessageFromClientOrganization();
    NewElectronicOnly electronic = new NewElectronicOnly();
    electronic.setAttachments(new Object[0]);
    electronic.setBody(request.getNotificationMessage());
    electronic.setBodyFormat(orgProperties.isMarkdownEnabled() ? BodyFormat.Markdown : BodyFormat.Text);
    electronic.setMessageServiceType(MessageServiceType.Normal);
    
    MessageNotifications notifications = new MessageNotifications();
    notifications.setSenderDetailsInNotifications(SenderDetailsInNotifications.OrganisationAndServiceName);
    UnreadMessageNotification unreadMessageNotification = new UnreadMessageNotification();
    unreadMessageNotification.setReminder(Reminder.DefaultReminder);
    notifications.setUnreadMessageNotification(unreadMessageNotification);
    if (request.getEmailMessage() != null) {
      CustomisedMessageNotification customNotification = new CustomisedMessageNotification();
      CustomisedMessageNotificationContent customContent = new CustomisedMessageNotificationContent();
      customContent.setEn(request.getEmailMessage().get("en"));
      customContent.setFi(request.getEmailMessage().get("fi"));
      customContent.setSv(request.getEmailMessage().get("sv"));
      customNotification.setContent(customContent);
      
      CustomisedMessageNotificationTitle title = new CustomisedMessageNotificationTitle();
      title.setEn(request.getEmailTitle().get("en"));
      title.setFi(request.getEmailTitle().get("fi"));
      title.setSv(request.getEmailTitle().get("sv"));
      customNotification.setTitle(title);
      notifications.setCustomisedNewMessageNotification(customNotification);
    }
    
    electronic.setNotifications(notifications);
    
    electronic.setReplyAllowedBy(ReplyAllowedBy.NoOne);
    electronic.setTitle(request.getNotificationTitle());
    electronic.setVisibility(RestMessageTypes.Visibility.Normal);
    
    body.setElectronic(electronic);
    body.setExternalId(requestId);
    Recipient recipient = new Recipient();
    recipient.setId(request.getClientId());
    body.setRecipient(recipient);
    Sender sender = new Sender();
    sender.setServiceId(orgProperties.getServiceId());
    body.setSender(sender);
    RestMessageResponse restMessageResponse = executeCall(body, "v2/messages/electronic", token, RestMessageResponse.class);
    return restMessageResponse.getMessageId();
  }

  private boolean checkUserMailbox(String clientId, String token) {
    MailboxesActiveRequest body = new MailboxesActiveRequest();
    body.setEndUsers(new ArrayList<>());
    EndUserId userId = new EndUserId();
    userId.setId(clientId);
    body.getEndUsers().add(userId);
    
    MailboxesActiveResponse mailboxResponse = executeCall(body, "v1/mailboxes/active", token, MailboxesActiveResponse.class);
    return mailboxResponse.getEndUsersWithActiveMailbox().stream().anyMatch(s -> s.getId().equals(clientId));
  }

  private AccessTokenResponse fetchAuthenticationToken() {
    AccessTokenRequestBody body = new AccessTokenRequestBody();
    body.setPassword(orgProperties.getPassword());
    body.setUsername(orgProperties.getId());
    
    AccessTokenResponse tokenResponse = executeCall(body, "v1/token", null, AccessTokenResponse.class);
    return tokenResponse;
    
  }

  private RestClient getRestClient() {
    return RestClient.create();
  }

  
  private <R, B> R executeCall(B body, String path, String token, Class<R> clazz) {
    RestClient client = getRestClient();
    R result = client.post().uri(String.format("%s/%s", orgProperties.getEndpoint(), path))
    .contentType(MediaType.APPLICATION_JSON)
    .body(body)
    .headers(h->{if (StringUtils.isNotBlank(token))h.setBearerAuth(token);})
    .retrieve()
    .onStatus(code -> code.is4xxClientError() || code.is5xxServerError(), (request, response) -> {
      RestErrorResponse restErrorResponse = null;
      try {
        restErrorResponse = mapper.readValue(response.getBody(), RestErrorResponse.class);
      }
      catch (Exception e) {
        restErrorResponse = new RestErrorResponse();
        restErrorResponse.setReason("N/A");
        restErrorResponse.setValidationErrors(new ArrayList<RestErrorResponse.ValidationErrors>());
      }
      throw new RestApiCallException(restErrorResponse, response.getStatusCode()); 
    })
    .body(clazz);
    return result;
  }
}
