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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import io.digiexpress.eveli.client.config.EveliPropsEmail;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class EmailFilter {
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
    if (properties.getAllowedRecipients() != null && properties.getAllowedRecipients().size() > 0 
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
