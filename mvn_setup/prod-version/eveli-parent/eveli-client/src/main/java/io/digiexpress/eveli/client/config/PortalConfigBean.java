package io.digiexpress.eveli.client.config;

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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties
public class PortalConfigBean {
  
  @Value("${app.dialob.questionnaires-url}")
  private String formQuestionnairesUrl;

  @Value("${app.dialob.forms-url}")
  private String formsUrl;

  @Value("${app.dialob.authorization:v2vKvgBqR6KvHKWbDzMNgX0AMcVwESZ8ZeonceXz}")
  private String formAuthorization;
  
  @Value("${app.dialob.submit-callback-url}")
  private String submitCallbackUrl;
  
  @Value("${app.dialob.tenant-id}")
  private String dialobTenantId;
  
  @Value("${app.dialob.submit-message-delay:0}")
  private Long submitMessageDelay;

  @Value("${app.notification.url:}")
  private String notificationUrl;

  @Value("${app.notification.testmessage:}")
  private Boolean notificationTestMessage;

  @Value("${app.notification.group-membership-url:}")
  private String groupMembershipUrl;
  
  @Value("${app.attachment-config.downloadBucket:}")
  private String downloadBucket;

  @Value("${app.wrench.local-asset-api-url:#{null}}")
  private String wrenchAssetApiUrl;

  @Value("${app.portal.local-asset-api-url:#{null}}")
  private String contentAssetApiUrl;

  @Value("${app.portal.anonymous-user-id:anon}")
  private String portalAnonymousUserId;
  
  @Value("${app.printout.service-url}")
  private String printoutServiceUrl;
  

  @Value("${app.workflow.db.enabled:true}")
  private Boolean workflowAssetsPgEnabled;
  @Value("${app.workflow.json.enabled:false}")
  private Boolean workflowJsonEnabled;
  @Value("${app.workflow.json.location}")
  private Resource workflowJsonLocation;
}
