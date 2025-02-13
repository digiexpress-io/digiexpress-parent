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

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AttachmentCommandsDummy implements AttachmentCommands {

  @Override
  public AttachmentQuery query() {
    return new AttachmentQuery() {
      @Override
      public List<Attachment> taskId(String taskId) {
        return Collections.emptyList();
      }
      @Override
      public List<Attachment> processId(String processId) {
        return Collections.emptyList();
      }
    };
  }
  @Override
  public AttachmentUploadBuilder upload() {
    return new AttachmentUploadBuilder() {
      @Override
      public Optional<AttachmentUpload> taskId(String taskId) {
        return Optional.empty();
      }
      @Override
      public Optional<AttachmentUpload> processId(String processId) {
        return Optional.empty();
      }
      @Override
      public AttachmentUploadBuilder filename(String filename) {
        return this;
      }
      @Override
      public AttachmentUploadBuilder encodePath(String filename) {
        return this;
      }
    };
  }
  @Override
  public AttachmentUrlBuilder url() {
    return new AttachmentUrlBuilder() {
      @Override
      public Optional<URL> taskId(String taskId) throws URISyntaxException {
        return Optional.empty();
      }
      @Override
      public Optional<URL> processId(String processId) throws URISyntaxException {
        return Optional.empty();
      }
      @Override
      public AttachmentUrlBuilder filename(String filename) {
        return this;
      }
      @Override
      public AttachmentUrlBuilder encodePath(String filename) {
        return this;
      }
    };
  }
}
