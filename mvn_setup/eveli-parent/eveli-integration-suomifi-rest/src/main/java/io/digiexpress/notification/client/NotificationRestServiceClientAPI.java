package io.digiexpress.notification.client;

/*-
 * #%L
 * eveli-integration-suomifi-rest
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

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Client for REST API service.
 * Executes REST API requests for 
 * <ul>
 *   <li> /v1/token - to receive token
 *   <li> /v1/messages - to send message
 * </ul>
 *
 */
public interface NotificationRestServiceClientAPI{
  
  @Getter
  @AllArgsConstructor
  public class ValidationError {
    String error;
  }
  
  @Data
  public class NotificationRestResponse {
    boolean success;
    Long messageId;
    // HTTP response code, 200 = success
    Integer code; 
    // in case of errors
    String reason;
    List<ValidationError> validationErrors;
  }
  
  
  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public class NotificationRequest {
    private String clientId;
    
    private String notificationTitle; 
    private String notificationMessage;
    
    // Optional email content. if specified then this is used to send custom email message about notification
    // for suomi.fi it should be provided in 'fi', 'sv' and 'en' languages where key is language.
    private Map<String, String> emailTitle; 
    private Map<String, String> emailMessage;
  }
  
  
  NotificationRestResponse sendClientNotification(NotificationRequest request, String requestId);

}
