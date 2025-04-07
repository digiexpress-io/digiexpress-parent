package io.digiexpress.eveli.client.spi.comms;

import java.io.UnsupportedEncodingException;

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

import org.apache.commons.lang3.StringUtils;

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

/**
 * Email notification builder implementation based on Jakarta mail transport.
 */

@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class EmailBuilderJakarta implements CommsClient.EmailBuilder {
  private final EveliPropsEmail config;
  private final EmailBuilderLogger logger = new EmailBuilderLogger();
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
    try {
      visitAllRecipients(this.recipients);
    } finally {
      logger.close();
    }
  }

  private void visitAllRecipients(List<String> receiverEmails) {
    if(!Boolean.TRUE.equals(config.getEnabled())) {
      logger.emailDisabled();
      return;
    }
    
    RepoAssert.notEmpty(title, () -> "title must be defined!");
    RepoAssert.notEmpty(refId, () -> "refId must be defined!");
    RepoAssert.notEmpty(message, () -> "message must be defined!");
    
    logger.emailCreated(recipients, title, refId);
    if (receiverEmails.isEmpty()) {
      logger.noRecipients();
      return;
    } 
    final var internetAddresses = visitInternetAddresses(receiverEmails);

    if (internetAddresses.isEmpty()) {
      logger.noValidRecipients();
      return;
    }
    
    try {
      sendEmail(internetAddresses);
      logger.emailSent(internetAddresses);
    } catch(Exception e) {
      logger.emailFailed(internetAddresses, e);
    }
  }

  private void sendEmail(List<InternetAddress> internetAddresses) throws MessagingException, AddressException, UnsupportedEncodingException {
    
    Properties props = new Properties();
    props.put("mail.smtp.host", config.getHostName());
    props.put("mail.smtp.port", config.getHostPort());
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.ssl.protocols", "TLSv1.2");
    
    
    Session session = Session.getInstance(props);
    final var msg = new MimeMessage(session);
    if (StringUtils.isAllBlank(config.getSenderName())) {
      msg.setFrom(new InternetAddress(config.getSenderEmail(), false));
    }
    else {
      msg.setFrom(new InternetAddress(config.getSenderEmail(), config.getSenderName()));
    }
    msg.setRecipients(MimeMessage.RecipientType.TO, internetAddresses.toArray(new InternetAddress[internetAddresses.size()]));
    msg.setSubject(title);
    msg.setText(message);
    msg.setSentDate(new Date());
    msg.setHeader("Auto-Submitted", "auto-generated");
    msg.setHeader("X-Auto-Response-Suppress", "DR, RN, NRN, OOF, AutoReply");

    Transport.send(msg);
  }

  private List<InternetAddress> visitInternetAddresses(List<String> emailAddresses) {
    final EmailFilter filter = new EmailFilter(config);
    final var internetAddresses = new ArrayList<InternetAddress>();
    for (final var emailAddress : emailAddresses) {
      if (!filter.isValidEmail(emailAddress)) {
        logger.invalidRecipientSkipped(emailAddress);
        continue;
      }
      try {
        for (InternetAddress address : InternetAddress.parse(emailAddress)) {
          if (filter.isEnabledEmail(address)) {
            internetAddresses.add(address);
          } else {
            logger.blockedRecipientSkipped(address);
          }
        }
      } catch (AddressException e) {
        logger.corrupRecipientSkipped(emailAddress, e);
      }
    }
    return internetAddresses;
  }

}
