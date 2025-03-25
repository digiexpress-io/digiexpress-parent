package io.digiexpress.notification.client;

/*-
 * #%L
 * eveli-integration-suomifi-wsl
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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import fi.suomi.asiointitili.ArrayOfAsiakas;
import fi.suomi.asiointitili.ArrayOfKohdeWS2;
import fi.suomi.asiointitili.Asiakas;
import fi.suomi.asiointitili.HaeAsiakkaita;
import fi.suomi.asiointitili.HaeAsiakkaitaResponse;
import fi.suomi.asiointitili.KohdeWS2;
import fi.suomi.asiointitili.KyselyWS1;
import fi.suomi.asiointitili.KyselyWS2;
import fi.suomi.asiointitili.LisaaKohteita;
import fi.suomi.asiointitili.LisaaKohteitaResponse;
import fi.suomi.asiointitili.ObjectFactory;
import fi.suomi.asiointitili.Viranomainen;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Client implementation class for web service.
 * Executes Web API requests for 
 * <ul>
 *   <li> haeAsiakkaita - verifies if client accepts electronic notifications
 *   <li> lisaaKohteita - sends notification message
 * </ul>
 * See https://palveluhallinta.suomi.fi/fi/tuki/artikkelit/6231a819e014bf0100455b70 for documentation
 * @author vahur
 *
 */
@Slf4j
@RequiredArgsConstructor
public class NotificationWebServiceClient extends WebServiceGatewaySupport implements NotificationServiceClientAPI {
  
  private final SuomiFiWSLProperties orgProperties;
  private final ObjectFactory factory;

  @Override
  public HaeAsiakkaitaResponse getClient(Client client, String requestId) {
    HaeAsiakkaita request = factory.createHaeAsiakkaita();
    request.setViranomainen(createRequestOrganization(requestId));
    request.setKysely(createClientRequestBody(client));
    return sendClientRequest(request);
  }

  @Override
  public LisaaKohteitaResponse sendClientNotification(NotificationRequest request, String requestId) throws DatatypeConfigurationException {
    LisaaKohteita notificationRequest = factory.createLisaaKohteita();
    notificationRequest.setViranomainen(createRequestOrganization(requestId));
    notificationRequest.setKysely(createNotificationBody(request));
    return sendNotification(notificationRequest);
  }


  private KyselyWS2 createNotificationBody(NotificationRequest request) throws DatatypeConfigurationException {
    KyselyWS2 result = factory.createKyselyWS2();
    result.setKohdeMaara(1);
    ArrayOfKohdeWS2 notificationArray = factory.createArrayOfKohdeWS2();
    KohdeWS2 notification = createNotification(request);
    notificationArray.getKohde().add(notification);
    result.setKohteet(notificationArray);
    return result;
  }


  private KohdeWS2 createNotification(NotificationRequest request) throws DatatypeConfigurationException {
    KohdeWS2 notification = factory.createKohdeWS2();
    Asiakas client = createClient(request.getClient());
    notification.getAsiakas().add(client);
    notification.setViranomaisTunniste(request.getNotificationId());
    notification.setLahetysPvm(createTimestamp());
    notification.setLahettajaNimi(orgProperties.getOrganizationName());
    notification.setNimeke(request.getNotificationTitle());
    notification.setKuvausTeksti(request.getNotificationMessage());
    notification.setViestityyppi(2);
    return notification;
  }

  private XMLGregorianCalendar createTimestamp() throws DatatypeConfigurationException {
    return DatatypeFactory.newInstance().newXMLGregorianCalendar();
  }

  private KyselyWS1 createClientRequestBody(Client requestClient) {
    KyselyWS1 result = factory.createKyselyWS1();
    result.setKyselyLaji("Asiakkaat");
    ArrayOfAsiakas clientsArray = factory.createArrayOfAsiakas();
    Asiakas client = createClient(requestClient);
    clientsArray.getAsiakas().add(client);
    result.setAsiakkaat(clientsArray);
    return result;
  }


  private Asiakas createClient(Client requestClient) {
    Asiakas client = factory.createAsiakas();
    client.setAsiakasTunnus(requestClient.getClientId());
    client.setTunnusTyyppi(requestClient.getClientType().name());
    return client;
  }

  private Viranomainen createRequestOrganization(String requestId) {
    Viranomainen result = factory.createViranomainen();
    result.setViranomaisTunnus(orgProperties.getId());
    result.setPalveluTunnus(orgProperties.getServiceId());
    result.setSanomaTunniste(requestId);
    result.setSanomaVersio(orgProperties.getMessageVersion());
    result.setSanomaVarmenneNimi(orgProperties.getMessageCertCName());
    return result;
  }
  
  protected HaeAsiakkaitaResponse sendClientRequest(HaeAsiakkaita request) {
    return (HaeAsiakkaitaResponse) sendWebApiRequest(request);
  }
  
  protected LisaaKohteitaResponse sendNotification(LisaaKohteita notificationRequest) {
    return (LisaaKohteitaResponse) sendWebApiRequest(notificationRequest);
  }
  
  protected Object sendWebApiRequest(Object notificationRequest) {
    return getWebServiceTemplate().marshalSendAndReceive(notificationRequest);
  }
}
