package io.digiexpress.eveli.client.web.resources;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(path = "/api/notification")
@Slf4j
public class NotificationController {
  
  /*
import io.digiexpress.notification.api.NotificationResponse.NotificationResponseBuilder;
import io.digiexpress.notification.client.NotificationServiceClient;
import io.digiexpress.notification.component.RequestIdGenerator;
import io.digiexpress.notification.wsdl.ArrayOfAsiakasJaTilaWS1;
import io.digiexpress.notification.wsdl.HaeAsiakkaitaResponse;
import io.digiexpress.notification.wsdl.LisaaKohteitaResponse;
import io.digiexpress.notification.wsdl.TilaKoodiWS;
  
  @Autowired
  NotificationServiceClient client;
  
  @Autowired
  RequestIdGenerator idGenerator;
  
  @Autowired
  NotificationProperties notificationProperties;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public NotificationResponse sendNotification(
      @RequestBody NotificationRequest request
      ) throws DatatypeConfigurationException {
    
    String notificationId = request.getNotificationId();
    log.info("Notification sending request, id: {}", notificationId);
    NotificationResponseBuilder result = NotificationResponse.builder();
    
    if (!notificationProperties.isEnabled()) {
      log.info("Notification sending request, id: {}: result: processing cancelled, cause: notification sending disabled by configuration", notificationId);
      return result.responseCode(307).message("notification sending disabled by configuration").build();
    }
    
    HaeAsiakkaitaResponse clientVerificationResult = client.getClient(request.getClient(), idGenerator.generateRequestId());
    TilaKoodiWS responseCode = clientVerificationResult.getHaeAsiakkaitaResult().getTilaKoodi();
    if (responseCode.getTilaKoodi() != 0) {
      result.responseCode(responseCode.getTilaKoodi());
      result.message(responseCode.getTilaKoodiKuvaus());
      log.warn("Notification sending request, id: {}: result: processing cancelled, cause: client verification failed, status code: {}", notificationId, responseCode.getTilaKoodi());
      return result.build();
    }
    ArrayOfAsiakasJaTilaWS1 clientsList = clientVerificationResult.getHaeAsiakkaitaResult().getAsiakkaat();
    if (clientsList.getAsiakas().size() != 1) {
      result.responseCode(500);
      result.message("Incorrect response");
      log.warn("Notification sending request, id: {}: result: processing cancelled, cause: multiple clients found", notificationId);
      return result.build();
    }
    else if (clientsList.getAsiakas().get(0).getTila() != 300) {
      result.responseCode(HttpStatus.NO_CONTENT.value());
      result.message("Notification not enabled");
      log.info("Notification sending request, id: {}: result: processing cancelled, cause: notification not enabled for client", notificationId);
      return result.build();
    }
    
    LisaaKohteitaResponse notificationResponse = client.sendClientNotification(
        request, idGenerator.generateRequestId());
    TilaKoodiWS notificationResponseCode = notificationResponse.getLisaaKohteitaResult().getTilaKoodi();
    result.responseCode(notificationResponseCode.getTilaKoodi());
    result.message(notificationResponseCode.getTilaKoodiKuvaus());
    log.info("Notification sending request, id: {}: result: processing completed", notificationId);
    return result.build();
  }


*/
}
