package suomifi.notification.wsdl;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.xml.namespace.QName;

import org.apache.wss4j.common.crypto.CertificateStore;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.dom.WSConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.webservices.client.WebServiceTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean;

import io.digiexpress.eveli.client.config.SoapClientProperties;
import io.digiexpress.notification.client.NotificationServiceClient;
import io.digiexpress.notification.client.spi.DefaultNotificationServiceClient;
import io.digiexpress.notification.component.LogInterceptor;

@Configuration
@Profile({"dev","test","prod","unittest"})
public class SignedWebServiceConfiguration {

  @Bean
  public NotificationServiceClient notificationClient(WebServiceTemplate template) {
    DefaultNotificationServiceClient client = new DefaultNotificationServiceClient();
    client.setWebServiceTemplate(template);
    return client;
  }
  
  @Bean
  @Qualifier("keyStoreFactory")
  CryptoFactoryBean keyStoreFactory(SoapClientProperties clientConfig) throws IOException {
    CryptoFactoryBean result = new CryptoFactoryBean();
    result.setKeyStoreLocation(clientConfig.getSigningCertificate());
    result.setKeyStorePassword(clientConfig.getSigningStorePassword());
    return result;
  }

  @Bean
  @Qualifier("trustStoreCrypto")
  Crypto trustStoreCrypto(SoapClientProperties clientConfig) throws IOException, CertificateException {
    
    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
    try (InputStream is = new ByteArrayInputStream(clientConfig.getServerCertificatePem().getBytes())) {
      X509Certificate cer = (X509Certificate) certFactory.generateCertificate(is);
      X509Certificate[] certArray = new X509Certificate[1];
      certArray[0] = cer;
      CertificateStore store = new CertificateStore(certArray);
      return store;
    }
  }

  @Bean
  public Wss4jSecurityInterceptor securityInterceptor(SoapClientProperties clientConfig,
      @Qualifier("keyStoreFactory") CryptoFactoryBean keyStoreFactory, 
      @Qualifier("trustStoreCrypto") Crypto trustStore) 
      throws IOException, Exception
  {
    Wss4jSecurityInterceptor wss4jSecurityInterceptor = new Wss4jSecurityInterceptor();
    wss4jSecurityInterceptor.setSecurementActions("Timestamp Signature");
    wss4jSecurityInterceptor.setSecurementSignatureKeyIdentifier("DirectReference");
    wss4jSecurityInterceptor.setSecurementUsername(clientConfig.getSigningCertificateKeyAlias());
    wss4jSecurityInterceptor.setSecurementPassword(clientConfig.getSigningStorePassword());
    wss4jSecurityInterceptor.setSecurementSignatureCrypto(keyStoreFactory.getObject());
    wss4jSecurityInterceptor.setSecurementSignatureParts(createBodyTimestampSignaturePart());
    
    wss4jSecurityInterceptor.setValidationActions("Signature");
    wss4jSecurityInterceptor.setEnableSignatureConfirmation(true);
    wss4jSecurityInterceptor.setValidationSignatureCrypto(trustStore);
    return wss4jSecurityInterceptor;
  }

  private String createBodyTimestampSignaturePart() {
    String result = String.format("{}%s;{}%s", new QName(WSConstants.URI_SOAP11_ENV, WSConstants.ELEM_BODY).toString(), WSConstants.TIMESTAMP);
    return result;
  }


  @Bean
  public WebServiceTemplate webServiceTemplate(WebServiceTemplateBuilder builder, 
      Jaxb2Marshaller marshaller,
      SoapClientProperties clientConfig,
      Wss4jSecurityInterceptor wss4jSecurityInterceptor,
      LogInterceptor logInterceptor) throws Exception {
    return builder
        .setDefaultUri(clientConfig.getServiceUri())
        .setMarshaller(marshaller)
        .setUnmarshaller(marshaller)
        .additionalInterceptors(wss4jSecurityInterceptor, logInterceptor).build();
  }

}
