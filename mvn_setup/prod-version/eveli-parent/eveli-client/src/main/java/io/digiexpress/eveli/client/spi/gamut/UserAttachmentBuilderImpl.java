package io.digiexpress.eveli.client.spi.gamut;

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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.AttachmentCommands.AttachmentUpload;
import io.digiexpress.eveli.client.api.GamutClient.Attachment;
import io.digiexpress.eveli.client.api.GamutClient.AttachmentUploadUrlException;
import io.digiexpress.eveli.client.api.GamutClient.ProcessNotFoundException;
import io.digiexpress.eveli.client.api.GamutClient.UserAttachmentBuilder;
import io.digiexpress.eveli.client.api.GamutClient.UserAttachmentUploadInit;
import io.digiexpress.eveli.client.api.ImmutableAttachment;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class UserAttachmentBuilderImpl implements UserAttachmentBuilder {
  private final ProcessClient processRepository;
  private final AttachmentCommands attachmentCommands;
  private final List<UserAttachmentUploadInit> attachments = new ArrayList<>();
  private String actionId;
  

  @Override
  public UserAttachmentBuilder addAll(List<UserAttachmentUploadInit> init) {
    TaskAssert.notNull(init, () -> "init can't be null!");
    this.attachments.addAll(init);
    return this;
  }

  @Override
  public UserAttachmentBuilder actionId(String actionId) {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    this.actionId = actionId;
    return this;
  }

  @Override
  public List<Attachment> createMany() throws ProcessNotFoundException, AttachmentUploadUrlException {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    
    final var process = processRepository.queryInstances().findOneById(actionId)
        .orElseThrow(() -> new ProcessNotFoundException("Process not found by id: " + actionId + "!"));
    
    final var result = new ArrayList<Attachment>();
    for(final var file : this.attachments) {
      final var att = visitAttachment(process, file);
      result.add(att);
    }
    
    return Collections.unmodifiableList(result);
  }

  
  private Attachment visitAttachment(ProcessInstance process, UserAttachmentUploadInit file) throws AttachmentUploadUrlException {
    final var taskId = process.getTaskId();
    final var filename = file.getName();
    final Optional<AttachmentUpload> uploadUrl = taskId == null ?
        attachmentCommands.upload().encodePath(filename).processId(actionId) :
        attachmentCommands.upload().encodePath(filename).taskId(taskId.toString());

    if(uploadUrl.isEmpty()) {
      throw new AttachmentUploadUrlException("Can't create upload url for: " + filename + "!");
    }

    return ImmutableAttachment.builder()
        .id(attachmentId(filename, process))
        .created(LocalDateTime.now().toString())
        .size(0L)
        .name(filename)
        .upload(uploadUrl.get().getPutRequestUrl())
        .processId(actionId)
        .taskId(Optional.ofNullable(taskId).map(e -> e.toString()).orElse(null))
        .status("OK")
        .build();
  }

  public static String attachmentId(String name, ProcessInstance process) {
    return Hashing
    .murmur3_128()
    .hashString(name + "::" + process.getTaskId() + "::" + process.getId(), Charsets.UTF_8)
    .toString();

  }
}
