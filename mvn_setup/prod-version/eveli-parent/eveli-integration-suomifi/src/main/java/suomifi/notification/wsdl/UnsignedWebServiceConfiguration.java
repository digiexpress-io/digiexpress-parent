package suomifi.notification.wsdl;

import org.springframework.boot.webservices.client.WebServiceTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;

import io.digiexpress.eveli.client.config.SoapClientProperties;
import io.digiexpress.notification.client.NotificationServiceClient;
import io.digiexpress.notification.client.spi.DefaultNotificationServiceClient;
import io.digiexpress.notification.component.LogInterceptor;

@Configuration
@Profile("unsigned")
public class UnsignedWebServiceConfiguration {

  @Bean
  public NotificationServiceClient notificationClient(WebServiceTemplate template) {
    DefaultNotificationServiceClient client = new DefaultNotificationServiceClient();
    client.setWebServiceTemplate(template);
    return client;
  }

  @Bean
  public Wss4jSecurityInterceptor securityInterceptor(){
      Wss4jSecurityInterceptor wss4jSecurityInterceptor = new Wss4jSecurityInterceptor();
      wss4jSecurityInterceptor.setValidationActions("NoSecurity");
      wss4jSecurityInterceptor.setSecurementActions("NoSecurity");
      return wss4jSecurityInterceptor;
  }
  
  @Bean
  public WebServiceTemplate webServiceTemplate(WebServiceTemplateBuilder builder, 
      Jaxb2Marshaller marshaller,
      SoapClientProperties clientConfig,
      Wss4jSecurityInterceptor wss4jSecurityInterceptor,
      LogInterceptor logInterceptor)
      throws Exception {
    return builder
        .setDefaultUri(clientConfig.getServiceUri())
        .setMarshaller(marshaller)
        .setUnmarshaller(marshaller)
        .additionalInterceptors(wss4jSecurityInterceptor, logInterceptor)
        .build();
  }
}
