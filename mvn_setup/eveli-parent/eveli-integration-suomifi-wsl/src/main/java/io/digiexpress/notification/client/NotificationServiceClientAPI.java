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

import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;

import fi.suomi.asiointitili.HaeAsiakkaitaResponse;
import fi.suomi.asiointitili.LisaaKohteitaResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Client for web service.
 * Executes Web API requests for 
 * <ul>
 *   <li> haeAsiakkaita - verifies if client accepts electronic notifications
 *   <li> lisaaKohteita - sends notification message
 * </ul>
 * See https://palveluhallinta.suomi.fi/fi/tuki/artikkelit/6231a819e014bf0100455b70 for documentation
 * @author vahur
 *
 */
public interface NotificationServiceClientAPI {
  
  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public class NotificationRequest {
    private Client client;
    private String notificationId;
    
    private String notificationTitle; 
    private String notificationMessage;
    
    // Optional email content. if specified then this is used to send custom email message about notification
    // for suomi.fi it should be provided in 'fi', 'sv' and 'en' languages where key is language.
    private Map<String, String> emailTitle; 
    private Map<String, String> emailMessage;
  }
  
  
  public enum ClientType {
    SSN,
    CRN
  }
  
  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Client {
    private String clientId;
    private ClientType clientType;
  }
  
  HaeAsiakkaitaResponse getClient(Client client, String requestId);
  
  LisaaKohteitaResponse sendClientNotification(NotificationRequest request, String requestId) throws DatatypeConfigurationException;

}
