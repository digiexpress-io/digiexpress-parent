package io.digiexpress.eveli.client.google;

/*-
 * #%L
 * eveli-integration-google
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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.util.UriUtils;

import com.google.cloud.spring.storage.GoogleStorageResource;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.Storage.SignUrlOption;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.ImmutableAttachment;
import io.digiexpress.eveli.client.api.ImmutableAttachmentUpload;
import io.digiexpress.eveli.client.spi.asserts.AttachmentAssert;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public class AttachmentCommandsImpl implements AttachmentCommands {

  private final String downloadBucket;
  private final Storage storage;
  private final ResourceLoader resourceLoader;

  @Override
  public AttachmentQuery query() {
    return new AttachmentQuery() {
      @Override
      public List<Attachment> taskId(String taskId) {
        final var pathString = String.format("tasks/%s/files/", taskId);
        return getAttachments(pathString, Optional.empty(), Optional.of(taskId));
      }

      @Override
      public List<Attachment> processId(String processId) {
        final var pathString = String.format("processes/%s/files/", processId);
        return getAttachments(pathString, Optional.of(processId), Optional.empty());
      }
    };
  }

  @Override
  public AttachmentUrlBuilder url() {
    return new AttachmentUrlBuilder() {
      private String filename;

      @Override
      public Optional<URL> taskId(String taskId) throws URISyntaxException {
        AttachmentAssert.notEmpty(filename, () -> "filename must be defiend!");
        AttachmentAssert.notEmpty(taskId, () -> "taskId must be defiend!");

        final var gsFile = String.format("gs://%s/tasks/%s/files/%s", downloadBucket, taskId, filename);
        return Optional.ofNullable(getAttachmentUrl(gsFile));
      }

      @Override
      public Optional<URL> processId(String processId) throws URISyntaxException {
        AttachmentAssert.notEmpty(filename, () -> "filename must be defiend!");
        AttachmentAssert.notEmpty(processId, () -> "processId must be defiend!");

        final var gsFile = String.format("gs://%s/processes/%s/files/%s", downloadBucket, processId, filename);
        return Optional.ofNullable(getAttachmentUrl(gsFile));
      }

      @Override
      public AttachmentUrlBuilder filename(String filename) {
        this.filename = filename;
        return this;
      }

      @Override
      public AttachmentUrlBuilder encodePath(String filename) {
        this.filename = UriUtils.encodePath(filename, "UTF-8");
        return this;
      }
    };
  }

  @Override
  public AttachmentUploadBuilder upload() {
    return new AttachmentUploadBuilder() {
      private String filename;

      @Override
      public Optional<AttachmentUpload> taskId(String taskId) {
        AttachmentAssert.notEmpty(filename, () -> "filename must be defiend!");
        AttachmentAssert.notEmpty(taskId, () -> "taskId must be defiend!");

        final var gsFile = String.format("gs://%s/tasks/%s/files/%s", downloadBucket, taskId, filename);
        final var result = getAttachmentUploadUrl(gsFile);

        return result == null ? Optional.empty()
            : Optional.of(ImmutableAttachmentUpload.builder().putRequestUrl(result.toString()).build());
      }

      @Override
      public Optional<AttachmentUpload> processId(String processId) {
        AttachmentAssert.notEmpty(filename, () -> "filename must be defiend!");
        AttachmentAssert.notEmpty(processId, () -> "processId must be defiend!");

        final var gsFile = String.format("gs://%s/processes/%s/files/%s", downloadBucket, processId, filename);
        final var result = getAttachmentUploadUrl(gsFile);

        return result == null ? Optional.empty()
            : Optional.of(ImmutableAttachmentUpload.builder().putRequestUrl(result.toString()).build());
      }

      @Override
      public AttachmentUploadBuilder filename(String filename) {
        this.filename = filename;
        return this;
      }

      @Override
      public AttachmentUploadBuilder encodePath(String filename) {
        this.filename = UriUtils.encodePath(filename, "UTF-8");
        return this;
      }
    };
  }

  private List<Attachment> getAttachments(String pathString, Optional<String> processId, Optional<String> taskId) {
    final var result = new ArrayList<Attachment>();
    final var blobs = storage.list(downloadBucket, BlobListOption.currentDirectory(),
        BlobListOption.prefix(pathString));
    for (final var blob : blobs.iterateAll()) {
      final var filenameFromPath = getFilenameFromPath(blob.getName());
      if (!StringUtils.isEmpty(filenameFromPath)) {
        result.add(ImmutableAttachment.builder().name(filenameFromPath)
            .processId(processId)
            .taskId(taskId)
            .created(ZonedDateTime.ofInstant(Instant.ofEpochMilli(blob.getCreateTime()), ZoneOffset.UTC))
            .updated(ZonedDateTime.ofInstant(Instant.ofEpochMilli(blob.getUpdateTime()), ZoneOffset.UTC))
            .size(blob.getSize()).status(AttachmentStatus.OK).build());
      }
    }
    return result;
  }

  private String getFilenameFromPath(String name) {
    return FilenameUtils.getName(name);
  }

  private URL getAttachmentUrl(String gsFile) {
    URL result = null;
    Resource file = resourceLoader.getResource(gsFile);
    if (file.exists() && file instanceof GoogleStorageResource) {
      GoogleStorageResource storage = (GoogleStorageResource) file;
      result = storage.createSignedUrl(TimeUnit.MINUTES, 5, SignUrlOption.withV4Signature());
    }
    return result;
  }

  private URL getAttachmentUploadUrl(String gsFile) {
    URL result = null;
    Resource file = resourceLoader.getResource(gsFile);
    if (file instanceof GoogleStorageResource) {
      GoogleStorageResource storage = (GoogleStorageResource) file;
      result = storage.createSignedUrl(TimeUnit.MINUTES, 5, SignUrlOption.withV4Signature(),
          SignUrlOption.httpMethod(HttpMethod.PUT));
    }
    return result;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Setter
  @Accessors(fluent = true)
  public static class Builder {
    private String downloadBucket;
    private Storage storage;
    private ResourceLoader resourceLoader;

    public AttachmentCommandsImpl build() {
      AttachmentAssert.notEmpty(downloadBucket, () -> "downloadBucket must be defiend!");
      AttachmentAssert.notNull(storage, () -> "storage must be defiend!");
      AttachmentAssert.notNull(resourceLoader, () -> "resourceLoader must be defiend!");
      return new AttachmentCommandsImpl(downloadBucket, storage, resourceLoader);
    }
  }
}
