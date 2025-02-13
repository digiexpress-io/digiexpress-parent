package suomifi.notification.wsdl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.digiexpress.notification.client.NotificationServiceClient;
import io.digiexpress.notification.client.spi.LoggingNotificationServiceClient;

@Configuration
@Profile({"stub"})
public class StubConfiguration {

  @Bean
  public NotificationServiceClient notificationClient() {
    LoggingNotificationServiceClient client = new LoggingNotificationServiceClient();
    return client;
  }
}
