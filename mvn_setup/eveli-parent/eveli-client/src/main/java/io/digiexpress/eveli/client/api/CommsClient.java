
package io.digiexpress.eveli.client.api;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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



public interface CommsClient {
  CustomerSmsBuilder createCustomerSms();  
  EmailBuilder createEmail();


  interface CustomerSmsBuilder {
    CustomerSmsBuilder title(String title);
    CustomerSmsBuilder content(String content);
    CustomerSmsBuilder userId(String userId);
    CustomerSmsBuilder ssn(String userId);
    CustomerSmsBuilder crn(String userId);
    CustomerSmsBuilder notificationId(String notificationId);
    void build();
  }

  interface EmailBuilder {
    EmailBuilder title(String title); 
    EmailBuilder message(String message);
    EmailBuilder refId(String refId);
    
    EmailBuilder recipientAddress(List<String> recipientAddress);
    EmailBuilder recipientAddress(String recipientAddress);
    void build();
  }
}
