package io.digiexpress.eveli.client.config;

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
