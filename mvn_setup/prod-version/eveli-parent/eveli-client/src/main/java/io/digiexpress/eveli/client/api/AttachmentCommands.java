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

import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public interface AttachmentCommands {
  AttachmentQuery query();
  AttachmentUploadBuilder upload();
  AttachmentUrlBuilder url();
  
  interface AttachmentQuery {
    List<Attachment> processId(String processId);
    List<Attachment> taskId(String taskId);
  }
  
  interface AttachmentUrlBuilder {
    AttachmentUrlBuilder filename(String filename);
    AttachmentUrlBuilder encodePath(String filename);
    Optional<URL> taskId(String taskId) throws URISyntaxException;
    Optional<URL> processId(String processId) throws URISyntaxException;
  }
  
  interface AttachmentUploadBuilder {
    AttachmentUploadBuilder filename(String filename);
    AttachmentUploadBuilder encodePath(String filename);
    Optional<AttachmentUpload> taskId(String taskId);
    Optional<AttachmentUpload> processId(String processId);
  }
  

  @Value.Immutable
  @JsonSerialize(as = ImmutableAttachmentUpload.class)
  @JsonDeserialize(as = ImmutableAttachmentUpload.class)
  interface AttachmentUpload {
    String getPutRequestUrl();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAttachment.class)
  @JsonDeserialize(as = ImmutableAttachment.class)
  interface Attachment {
    String getName();
    ZonedDateTime getCreated();
    ZonedDateTime getUpdated();
    Long getSize();
    AttachmentStatus getStatus();
  }
  
  enum AttachmentStatus { OK, QUARANTINED, UPLOADED }
}
