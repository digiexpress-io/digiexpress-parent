package io.digiexpress.eveli.client.spi.notification;

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

import io.digiexpress.eveli.client.api.NotificationCommands;
import io.digiexpress.eveli.client.api.NotificationCommands.EmailNotificationBuilder;
import io.digiexpress.eveli.client.api.NotificationCommands.EmailRequest;
import io.digiexpress.eveli.client.api.NotificationCommands.EmailResponse;
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
public class RestEmailNotificationBuilder implements NotificationCommands.EmailNotificationBuilder {

    private final EveliPropsEmail emailProps;
    private final RestTemplate client;
    
    private EmailRequest props = new EmailRequest();

    @Override
    public EmailNotificationBuilder title(String notificationTitle) {
      props.setNotificationTitle(notificationTitle);
      return this;
    }

    @Override
    public EmailNotificationBuilder message(String notificationMessage) {
      props.setNotificationMessage(notificationMessage);
      return this;
    }

    @Override
    public EmailNotificationBuilder address(String recipientAddress) {
      if (props.getRecipientAddresses() == null) {
        props.setRecipientAddresses(new ArrayList<>());
      }
      props.getRecipientAddresses().add(recipientAddress);
      return this;
    }

    @Override
    public EmailNotificationBuilder refId(String refId) {
      props.setRefId(refId);
      return this;
    }

    @Override
    public EmailNotificationBuilder addresses(List<String> recipientAddress) {
      if (props.getRecipientAddresses() == null) {
        props.setRecipientAddresses(new ArrayList<>());
      }
      props.getRecipientAddresses().addAll(recipientAddress);
      return this;
    }

    @Override
    public void build() {
      final String logPrefix = "Email sending request, refId: " + props.getRefId();
      List<String> emailAddressList = props.getRecipientAddresses();
      
      log.info("{}, title {}, number of recipients: {}", logPrefix, props.getNotificationTitle(), 
          emailAddressList != null ? emailAddressList.size() : 0);
      log.debug("{}, recipients: {}", logPrefix, emailAddressList);
      
      if (!emailProps.getEnabled()) {
        log.info("{}, result: cancelled, reason: email sending disabled in configuration.", logPrefix);
      }
      else {
        try {
          sendEmailNotification(this.props, logPrefix);
        }
        catch (Exception e) {
          log.error("{}, result: error", logPrefix, e);
        }
      }
    }

    private void sendEmailNotification(EmailRequest request, String logPrefix) {
      if (StringUtils.isEmpty(emailProps.getServiceUrl())) {
        log.info("Notification url is not configured, skipping sending for message: {}", props);
        return;
      }
      
      final HttpHeaders headers = new HttpHeaders();
      headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      headers.setContentType(MediaType.APPLICATION_JSON);

      final HttpEntity<EmailRequest> requestEntity = new HttpEntity<>(request, headers);
      String serviceUrl = UriComponentsBuilder.fromHttpUrl(emailProps.getServiceUrl()).toUriString();
      log.info("Sending notification message {} to url {}", request, emailProps.getServiceUrl());
      ResponseEntity<EmailResponse> response = client.exchange(serviceUrl, 
          HttpMethod.POST, requestEntity, EmailResponse.class);
      if (response.getStatusCode().is2xxSuccessful()) {
        EmailResponse result = response.getBody();
        log.info("Email sending response OK, result: {}", result);
      }
      else {
        log.warn("Email sending response code {}, email sending failed", response.getStatusCode());
      }
      
    }
}
