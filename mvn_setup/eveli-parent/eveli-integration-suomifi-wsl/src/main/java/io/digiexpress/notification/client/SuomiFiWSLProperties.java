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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;


@Data
@Component
@ConfigurationProperties(prefix = "eveli.suomifi.wsl")
public class SuomiFiWSLProperties {
  private boolean enabled;
  // system id, old viranomaistunnus
  private String id;
  // old palvelutunnus
  private String serviceId;
  private String messageVersion;
  private String messageCertCName;
  private String organizationName;
}
