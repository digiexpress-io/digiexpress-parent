package io.digiexpress.eveli.app;
 

import org.springframework.boot.autoconfigure.AutoConfiguration;

/*-
 * #%L
 * eveli-app
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.spi.attachments.AttachmentCommandsDummy;

/**
 * AttachmentCommands dummy bean is in separate autoconfiguration class to ensure its initialization 
 * only if no other beans of same type are not initialized. @Configuration annotated classes are initialized before auto-configuration.
 */
@AutoConfiguration
public class MockAttachmentServicesProvider {

  @Bean
  @ConditionalOnMissingBean(AttachmentCommands.class)
  public AttachmentCommands attachmentCommands() {
    return new AttachmentCommandsDummy();
  }
}
