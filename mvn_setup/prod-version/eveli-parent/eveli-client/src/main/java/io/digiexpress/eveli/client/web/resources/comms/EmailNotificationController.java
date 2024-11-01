package io.digiexpress.eveli.client.web.resources.comms;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.NotificationCommands.EmailRequest;
import io.digiexpress.eveli.client.api.NotificationCommands.EmailResponse;
import io.digiexpress.eveli.client.config.EveliPropsEmail;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping(path = "/api/email")
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationController {
  
  
  private final EveliPropsEmail emailProps;
  private final EmailFilter filter;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<EmailResponse> sendEmailNotification(@RequestBody EmailRequest request)
      throws AddressException, MessagingException {
    final String logPrefix = "Email sending request, refId: " + request.getRefId();
    
    List<String> emailAddressList = request.getRecipientAddresses();
    
    log.info("{}, title {}, number of recipients: {}", logPrefix, request.getNotificationTitle(), 
        emailAddressList != null ? emailAddressList.size() : 0);
    log.debug("{}, recipients: {}", logPrefix, emailAddressList);
    
    if (emailAddressList == null || emailAddressList.isEmpty()) {
      log.warn("{}, result: cancelled, reason: no email addresses.", logPrefix);
      return ResponseEntity.ok().body(EmailResponse.builder().responseCode(404).message("No email addresses").build());
    }
    
    InternetAddress[] internetAddresses = parseEmailAddressesToInternetAddresses(emailAddressList, logPrefix);

    if (internetAddresses.length == 0) {
      log.warn("{}, result: cancelled, reason: no valid or allowed email addresses.", logPrefix);
      return ResponseEntity.ok().body(EmailResponse.builder().responseCode(403).message("No valid or allowed email addresses").build());
    }
    if (!emailProps.getEnabled()) {
      log.info("{}, result: cancelled, reason: email sending disabled in configuration.", logPrefix);
      return ResponseEntity.ok().body(EmailResponse.builder().responseCode(405).message("Email sending disabled").build());
    }
    
    sendEmail(request, internetAddresses);
    
    log.info("{}, result: sent email to {} recipient(s).", logPrefix, internetAddresses.length);
    return ResponseEntity.ok().body(EmailResponse.builder().emailCount(internetAddresses.length).responseCode(0).message("OK").build());
  }

  private void sendEmail(EmailRequest request, InternetAddress[] internetAddresses)
      throws MessagingException, AddressException {
    Properties props = new Properties();
    props.put("mail.smtp.host", emailProps.getHostName());
    props.put("mail.smtp.port", emailProps.getHostPort());
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.ssl.protocols", "TLSv1.2");
    
    
    Session session = Session.getInstance(props);
    final var msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(emailProps.getSenderEmail(), false));

    msg.setRecipients(MimeMessage.RecipientType.TO, internetAddresses);
    msg.setSubject(request.getNotificationTitle());
    msg.setText(request.getNotificationMessage());
    msg.setSentDate(new Date());
    msg.setHeader("Auto-Submitted", "auto-generated");
    msg.setHeader("X-Auto-Response-Suppress", "DR, RN, NRN, OOF, AutoReply");

    Transport.send(msg);
  }

  private InternetAddress[] parseEmailAddressesToInternetAddresses(List<String> emailAddressList, String logPrefix) {
    List<InternetAddress> emailAddresses = new ArrayList<>();
    for (String emailAddress : emailAddressList) {
      if (!filter.isValidEmail(emailAddress)) {
        log.warn("{}, email {} has no valid address, skipping recipient", logPrefix, emailAddress);
      }
      else {
        try {
          InternetAddress[] emailInternetAddresses = InternetAddress.parse(emailAddress);
          for (InternetAddress address: emailInternetAddresses) {
            if (filter.isEnabledEmail(address)) {
              emailAddresses.add(address);
            }
            else {
              log.info("{}, email {} is not in allowlist, skipping recipient", logPrefix, address);
            }
          }
        } 
        catch (AddressException e) {
          log.warn("{}, email {} address parse error {}, skipping recipient", logPrefix, emailAddress, e);
        }
      }
    }
    return emailAddresses.toArray(new InternetAddress[emailAddresses.size()]);
  }
  
  
  @Slf4j
  @RequiredArgsConstructor
  public static class EmailFilter {
    private final EveliPropsEmail properties;
    
    public boolean isValidEmail(String email) {
      if (StringUtils.isBlank(email)) {
        log.info("Email filter: empty email");
        return false;
      }
      if (!EmailValidator.getInstance().isValid(email)) {
        log.warn("Incorrect email {}", email);
        return false;
      }
      if (!emailHasValidDomain(email)) {
        log.warn("Email {} not for correct domain", email);
        return false;
      }
      if (properties.getAllowedRecipients() != null && properties.getAllowedRecipients().size() >0 
          && !properties.getAllowedRecipients().contains(email)) {
        log.warn("Email {} not in allowlist", email);
        return false;
      }
      return true;
    }

    public boolean isEnabledEmail(InternetAddress email) {
      String emailAddress = email.getAddress();
      if (!emailHasValidDomain(emailAddress)) {
        log.warn("Email {} domain not enabled", email);
        return false;
      }
      if (properties.getAllowedRecipients() != null && properties.getAllowedRecipients().size() >0 
          && !properties.getAllowedRecipients().contains(emailAddress)) {
        log.warn("Email {} not in allowlist", email);
      }
      return true;
    }

    private boolean emailHasValidDomain(String emailAddress) {
      for (String domain : properties.getEnabledDomains()) {
        if (emailAddress.endsWith(domain)) {
          return true;
        }
      }
      return false;
    }
  }
}
