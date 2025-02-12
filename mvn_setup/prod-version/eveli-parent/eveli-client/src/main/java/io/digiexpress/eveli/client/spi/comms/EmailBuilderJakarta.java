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
import java.util.Date;
import java.util.List;
import java.util.Properties;

import io.digiexpress.eveli.client.api.CommsClient;
import io.digiexpress.eveli.client.api.CommsClient.EmailBuilder;
import io.digiexpress.eveli.client.config.EveliPropsEmail;
import io.resys.thena.support.RepoAssert;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Email notification builder implementation based on Jakarta mail transport.
 */
@Slf4j
@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class EmailBuilderJakarta implements CommsClient.EmailBuilder {
  private final EveliPropsEmail config;
  private final List<String> recipients = new ArrayList<>();
  private String title;
  private String message;
  private String refId;

  @Override
  public EmailBuilder recipientAddress(String recipientAddress) {
    recipients.add(recipientAddress);
    return this;
  }
  @Override
  public EmailBuilder recipientAddress(List<String> recipientAddress) {
    recipients.addAll(recipientAddress);
    return this;
  }

  @Override
  public void build() {
    RepoAssert.notEmpty(title, () -> "title must be defined!");
    RepoAssert.notEmpty(refId, () -> "refId must be defined!");
    RepoAssert.notEmpty(message, () -> "message must be defined!");
    RepoAssert.notEmpty(recipients, () -> "recipientAddress must be defined!");
    
    final String logPrefix = "Email sending request, refId: " + refId;
    List<String> emailAddressList = recipients;
    
    log.info("{}, title {}, number of recipients: {}", logPrefix, title, 
        emailAddressList != null ? emailAddressList.size() : 0);
    log.debug("{}, recipients: {}", logPrefix, emailAddressList);
    
    
    if (config.getEnabled()) {
      try {
        sendEmailNotification(logPrefix);
      }
      catch (Exception e) {
        log.error("{}, result: error", logPrefix, e);
      }
    } else {
      log.info("{}, result: cancelled, reason: email sending disabled in configuration.", logPrefix);
    }
    
  }

  private void sendEmailNotification(String logPrefix)
      throws AddressException, MessagingException 
  {
    List<String> emailAddressList = this.recipients;
    
    if (emailAddressList == null || emailAddressList.isEmpty()) {
      log.warn("{}, result: cancelled, reason: no email addresses.", logPrefix);
    }
    else {
      InternetAddress[] internetAddresses = parseEmailAddressesToInternetAddresses(emailAddressList, logPrefix);
  
      if (internetAddresses.length == 0) {
        log.warn("{}, result: cancelled, reason: no valid or allowed email addresses.", logPrefix);
      }
      else {
        sendEmail(internetAddresses);
        log.info("{}, result: sent email to {} recipient(s).", logPrefix, internetAddresses.length);
      }
    }
  }

  private void sendEmail(InternetAddress[] internetAddresses) throws MessagingException, AddressException {
    Properties props = new Properties();
    props.put("mail.smtp.host", config.getHostName());
    props.put("mail.smtp.port", config.getHostPort());
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.ssl.protocols", "TLSv1.2");
    
    
    Session session = Session.getInstance(props);
    final var msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(config.getSenderEmail(), false));

    msg.setRecipients(MimeMessage.RecipientType.TO, internetAddresses);
    msg.setSubject(title);
    msg.setText(message);
    msg.setSentDate(new Date());
    msg.setHeader("Auto-Submitted", "auto-generated");
    msg.setHeader("X-Auto-Response-Suppress", "DR, RN, NRN, OOF, AutoReply");

    Transport.send(msg);
  }

  private InternetAddress[] parseEmailAddressesToInternetAddresses(List<String> emailAddressList, String logPrefix) {
    final EmailFilter filter = new EmailFilter(config);
    
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

}
