package suomifi.notification.wsdl;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;


@Configuration
public class CommonConfiguration {

  @Bean
  LogInterceptor logInterceptor() {
    return new LogInterceptor();
  }

  

  @Bean
  RequestIdGenerator requestIdGenerator() {
    return new RequestIdGenerator();
  }
  public class RequestIdGenerator {
    public String generateRequestId() {
      return UUID.randomUUID().toString();
    }
  }

  
  
  @Bean
  public Jaxb2Marshaller marshaller() {
    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
    marshaller.setContextPath("io.digiexpress.notification.wsdl");
    return marshaller;
  }

  
  @Bean
  public ObjectFactory objectFactory() {
    return new ObjectFactory();
  }
}
