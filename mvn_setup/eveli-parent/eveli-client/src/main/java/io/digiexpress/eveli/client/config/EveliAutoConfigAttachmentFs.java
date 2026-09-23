package io.digiexpress.eveli.client.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
import io.digiexpress.eveli.client.spi.attachments.AttachmentCommandsDummy;
import io.digiexpress.eveli.client.spi.attachments.AttachmentCommandsFileSystem;
import io.digiexpress.eveli.client.web.resources.worker.AttachmentFilesystemController;



@Configuration
public class EveliAutoConfigAttachmentFs {
  
  @ConditionalOnBooleanProperty(matchIfMissing = false, havingValue = true, prefix = "eveli.attachment.fs", name = "enabled")
  @Bean
  public AttachmentCommands attachmentCommandFs(EveliPropsAttachmentFs props)  {
    String directory = props.getRootDirectory();
    final Path currentDirectory = Paths.get(directory);
    if (!currentDirectory.isAbsolute()) {
      final var tmpDirsLocation = System.getProperty("java.io.tmpdir");
      final var tempDirectory = Paths.get(tmpDirsLocation, directory);
      if (!Files.exists(tempDirectory)) {
        try {
          Files.createDirectory(tempDirectory);
        } catch(IOException e) {
          throw new RuntimeException("Failed to setup 'eveli.attachment.fs', failed to create temp dir: '" + tempDirectory.toAbsolutePath().toString() + "', error: " + e.getMessage(), e);
        }
      }
      directory = tempDirectory.toAbsolutePath().toString();
    }
    return new AttachmentCommandsFileSystem(directory, props.getAttachmentUrlBase(), props.getAttachmentServer());
  }
  
  @Bean
  public AttachmentFilesystemController attachmentFilesystemController(AttachmentCommands client, WorkerAuthClient securityClient) {
    return new AttachmentFilesystemController(client, securityClient);
  }
  
  
  @Bean
  @ConditionalOnBooleanProperty(matchIfMissing = true, havingValue = false, prefix = "eveli.attachment.fs", name = "enabled")
  public AttachmentCommands attachmentCommands() {
    return new AttachmentCommandsDummy();
  }
}
