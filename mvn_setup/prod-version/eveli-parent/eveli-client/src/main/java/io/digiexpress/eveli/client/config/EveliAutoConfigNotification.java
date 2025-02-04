package io.digiexpress.eveli.client.config;

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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import io.digiexpress.eveli.client.api.NotificationCommands;
import io.digiexpress.eveli.client.spi.notification.JakartaEmailNotificationBuilder;
import io.digiexpress.eveli.client.spi.notification.JakartaEmailNotificationBuilder.EmailFilter;
import io.digiexpress.eveli.client.spi.notification.RestEmailNotificationBuilder;
import io.digiexpress.eveli.client.spi.notification.RestNotificationBuilder;

@Configuration
public class EveliAutoConfigNotification {

  @Bean
  public NotificationCommands notificationCommands(EveliPropsNotification notificationProps, EveliPropsEmail emailProps, RestTemplate client) {
    return new NotificationCommands() {
      
      @Override
      public GroupMembershipQuery createMembershipQuery() {
        // TODO Auto-generated method stub
        return null;
      }
      
      @Override
      public EmailNotificationBuilder createEmail() {
        if (StringUtils.isNotBlank(emailProps.getServiceUrl())) {
          return new RestEmailNotificationBuilder(emailProps, client);
        }
        return new JakartaEmailNotificationBuilder(emailProps, new EmailFilter(emailProps));
      }
      
      @Override
      public NotificationBuilder create() {
        return new RestNotificationBuilder(notificationProps, client);
      }
    };
  }
}
