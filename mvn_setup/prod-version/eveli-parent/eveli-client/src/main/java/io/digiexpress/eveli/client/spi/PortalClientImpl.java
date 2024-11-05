package io.digiexpress.eveli.client.spi;

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

import java.util.function.Supplier;

import org.springframework.util.Assert;

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.HdesCommands;
import io.digiexpress.eveli.client.api.NotificationCommands;
import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.ProcessCommands;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.persistence.entities.TaskRefGenerator;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.spi.HdesCommandsImpl.TransactionWrapper;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
@Slf4j
public class PortalClientImpl implements PortalClient {
  private final ProcessCommands process;
  private final AttachmentCommands attachments;
  private final HdesCommands hdes;
  private final TransactionWrapper transactionWrapper;
  private final NotificationCommands notification;
  
  public static Builder builder() {
    return new Builder();
  }

  @Setter
  @Accessors(chain = true, fluent = true)
  @NoArgsConstructor
  public static class Builder {
    
    private AttachmentCommands attachmentCommands;
    private NotificationCommands notificationCommands;
    private ProcessRepository processRepository;
    private HdesClient hdesClient;
    private TransactionWrapper transactionWrapper;
    private TaskRepository taskRepository;
    private TaskNotificator taskNotificator;
    private Supplier<ProgramEnvir> programEnvir;
    private EveliAssetClient assetClient;
    private TaskRefGenerator taskRefGenerator;
    
    public PortalClientImpl build() {
      Assert.notNull(notificationCommands, () -> "notificationCommands can't be null!");
      Assert.notNull(attachmentCommands, () -> "attachmentCommands can't be null!");
      Assert.notNull(processRepository, () -> "processRepository can't be null!");
      Assert.notNull(hdesClient, () -> "hdesClient can't be null!");
      Assert.notNull(programEnvir, () -> "programEnvir can't be null!");
      Assert.notNull(transactionWrapper, () -> "transactionWrapper can't be null!");
      Assert.notNull(taskRepository, () -> "taskRepository can't be null!");
      Assert.notNull(taskNotificator, () -> "taskNotificator can't be null!");
      Assert.notNull(assetClient, () -> "assetClient can't be null!");
      Assert.notNull(taskRefGenerator, () -> "taskRefGenerator can't be null!");
      

      final var process = ProcessCommandsImpl.builder().processJPA(processRepository).build();
      final var hdes = HdesCommandsImpl.builder().hdesClient(hdesClient).transactionWrapper(transactionWrapper)
            .process(process).programEnvir(programEnvir).workflow(assetClient).build();
      
      return new PortalClientImpl(process, attachmentCommands, hdes, transactionWrapper, notificationCommands);
    }
  }
}
