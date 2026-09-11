package io.digiexpress.eveli.client.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;

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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.spi.attachments.AttachmentCommandsFileSystem;
import io.digiexpress.eveli.client.web.resources.worker.AttachmentFilesystemController;



@Configuration
@ConditionalOnBooleanProperty(matchIfMissing = false, havingValue = true, prefix = "eveli.attachment.fs", name = "enabled")
public class EveliAutoConfigAttachmentFs {
  
  @Bean
  public AttachmentCommands attachmentCommandFs(EveliPropsAttachmentFs props) {
    return new AttachmentCommandsFileSystem(props.getRootDirectory(), props.getAttachmentUrlBase(), props.getAttachmentServer());
  }
  
  @Bean
  public AttachmentFilesystemController attachmentFilesystemController(AttachmentCommands client, WorkerAuthClient securityClient) {
    return new AttachmentFilesystemController(client, securityClient);
  }
}
