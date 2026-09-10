package io.digiexpress.eveli.client.spi.attachments;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

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
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.util.UriUtils;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.ImmutableAttachment;
import io.digiexpress.eveli.client.api.ImmutableAttachmentUpload;
import io.digiexpress.eveli.client.spi.asserts.AttachmentAssert;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Filesystem based attachment commands
 * Mainly for local testing.
 * 
 */
@RequiredArgsConstructor
@Slf4j
public class AttachmentCommandsFileSystem implements AttachmentCommands {
  private final String rootDirectory;
  private final String attachmentUrlPath;
  private final String attachmentServer;
  
  private final ResourceLoader resourceLoader;

  @Override
  public AttachmentQuery query() {
    return new AttachmentQuery() {
      @Override
      public List<Attachment> taskId(String taskId) {
        final var pathString = String.format("%s/tasks/%s/files/", rootDirectory, taskId);
        try {
          return getAttachments(pathString, Optional.empty(), Optional.of(taskId));
        } catch (IOException e) {
          log.error("Exception listing attachments", e);
          return List.of();
        }
      }

      @Override
      public List<Attachment> processId(String processId) {
        final var pathString = String.format("%s/processes/%s/files/", rootDirectory, processId);
        try {
          return getAttachments(pathString, Optional.of(processId), Optional.empty());
        } catch (IOException e) {
          log.error("Exception listing attachments", e);
          return List.of();
        }
      }
    };
  }

  @Override
  public AttachmentUrlBuilder url() {
    return new AttachmentUrlBuilder() {
      private String filename;

      @Override
      public Optional<URL> taskId(String taskId) throws URISyntaxException {
        AttachmentAssert.notEmpty(filename, () -> "filename must be defined!");
        AttachmentAssert.notEmpty(taskId, () -> "taskId must be defined!");

        final var file = String.format("%s/tasks/%s/files/%s", attachmentUrlPath, taskId, filename);
        return Optional.ofNullable(getAttachmentUrl(file));
      }

      @Override
      public Optional<URL> processId(String processId) throws URISyntaxException {
        AttachmentAssert.notEmpty(filename, () -> "filename must be defined!");
        AttachmentAssert.notEmpty(processId, () -> "processId must be defined!");

        final var file = String.format("%s/processes/%s/files/%s", attachmentUrlPath, processId, filename);
        return Optional.ofNullable(getAttachmentUrl(file));
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
        AttachmentAssert.notEmpty(filename, () -> "filename must be defined!");
        AttachmentAssert.notEmpty(taskId, () -> "taskId must be defined!");

        final var file = String.format("%s/tasks/%s/files/%s", attachmentUrlPath, taskId, filename);

        return Optional.of(ImmutableAttachmentUpload.builder().putRequestUrl(file).build());
      }

      @Override
      public Optional<AttachmentUpload> processId(String processId) {
        AttachmentAssert.notEmpty(filename, () -> "filename must be defined!");
        AttachmentAssert.notEmpty(processId, () -> "processId must be defined!");

        final var file = String.format("%s/processes/%s/files/%s", attachmentUrlPath, processId, filename);

        return Optional.of(ImmutableAttachmentUpload.builder().putRequestUrl(file).build());
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

  private List<Attachment> getAttachments(String pathString, Optional<String> processId, Optional<String> taskId) throws IOException {
    final var result = new ArrayList<Attachment>();
    
    Collection<File> files = FileUtils.listFiles(new File(pathString), null, true);
    for (final var file : files) {
      final var filenameFromPath = file.getName();
      if (!StringUtils.isEmpty(filenameFromPath)) {
        result.add(ImmutableAttachment.builder().name(filenameFromPath)
            .processId(processId)
            .taskId(taskId)
            .created(ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneOffset.UTC))
            .updated(ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneOffset.UTC))
            .size(file.length()).status(AttachmentStatus.OK).build());
      }
    }
    return result;
  }

  private URL getAttachmentUrl(String filename) {
    try {
      return new URL(attachmentServer + filename);
    } catch (MalformedURLException e) {
      log.error("Failed to create URL from filename {}", filename, e);
      return null;
    }
  }


  public static Builder builder() {
    return new Builder();
  }

  @Setter
  @Accessors(fluent = true)
  public static class Builder {
    private String rootDirectory;
    private ResourceLoader resourceLoader;
    private String attachmentUrlBase;
    private String attachmentServer;

    public AttachmentCommandsFileSystem build() {
      AttachmentAssert.notEmpty(rootDirectory, () -> "rootDirectory must be defined!");
      AttachmentAssert.notNull(resourceLoader, () -> "resourceLoader must be defined!");
      return new AttachmentCommandsFileSystem(rootDirectory, attachmentUrlBase, attachmentServer ,resourceLoader);
    }
  }

  @Override
  public AttachmentRemoveBuilder remove() {
    return new AttachmentRemoveBuilder() {
      private String fileName;
      @Override
      public void removeByTaskId(String taskId) {
        final var blobName = String.format("%s/tasks/%s/files/%s", rootDirectory, taskId, fileName);
        File file = FileUtils.getFile(blobName);
        try {
          FileUtils.delete(file);
        } catch (IOException e) {
          log.error("File {} deletion failed", blobName, e);
        }
      }
      
      @Override
      public void removeByProcessId(String processId) {
        final var blobName = String.format("%s/processes/%s/files/%s", rootDirectory, processId, fileName);
        File file = FileUtils.getFile(blobName);
        try {
          FileUtils.delete(file);
        } catch (IOException e) {
          log.error("File {} deletion failed", blobName, e);
        }
      }
      
      @Override
      public AttachmentRemoveBuilder filename(String filename) {
        this.fileName = filename;
        return this;
      }
    };
  }

  @Override
  public AttachmentContentUploadBuilder contentUpload() {
    return new AttachmentContentUploadBuilder() {
      private String filename = null;
      private String processId = null;
      private String taskId = null;
      @Override
      public AttachmentContentUploadBuilder taskId(String taskId) {
        this.taskId = taskId;
        return this;
      }
      
      @Override
      public AttachmentContentUploadBuilder processId(String processId) {
        this.processId = processId;
        return this;
      }
      
      @Override
      public AttachmentContentUploadBuilder filename(String filename) {
        this.filename = filename;
        return this;
      }
      
      @Override
      public Attachment build(byte[] content) {
        AttachmentAssert.notEmpty(filename, () -> "filename must be defined!");
        String fileWithPath = null;
        if (processId != null) {
          fileWithPath = String.format("%s/processes/%s/files/%s", rootDirectory, processId, filename);
        }
        else {
          AttachmentAssert.notEmpty(taskId, () -> "taskId or processId must be defined!");
          fileWithPath = String.format("%s/tasks/%s/files/%s", rootDirectory, taskId, filename);
        }
        try {
          File file = FileUtils.getFile(fileWithPath);
          FileUtils.writeByteArrayToFile(file, content);
          io.digiexpress.eveli.client.api.ImmutableAttachment.Builder builder = ImmutableAttachment.builder()
            .name(filename)
            .created(ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))
            .updated(ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC))
            .size((long)content.length)
            .status(AttachmentStatus.OK);
          if (processId != null) {            
            builder.processId(processId);
          }
          else if (taskId != null) {
            builder.taskId(taskId);
          }
          return builder.build();
        } catch (IOException e) {
          log.warn("Error writing attachment: ", e);
        }
        return null;
      }
    };
  }

  @Override
  public AttachmentContentDownloadBuilder contentDownload() {

    return new AttachmentContentDownloadBuilder() {
      private String filename = null;
      private String processId = null;
      private String taskId = null;      
      @Override
      public AttachmentContentDownloadBuilder taskId(String taskId) {
        this.taskId = taskId;
        return this;
      }
      
      @Override
      public AttachmentContentDownloadBuilder processId(String processId) {
        this.processId = processId;
        return this;
      }
      
      @Override
      public AttachmentContentDownloadBuilder filename(String filename) {
        this.filename = filename;
        return this;
      }
      
      @Override
      public byte[] build() {
        AttachmentAssert.notEmpty(filename, () -> "filename must be defined!");
        String fileWithPath = null;
        if (processId != null) {
          fileWithPath = String.format("%s/processes/%s/files/%s", rootDirectory, processId, filename);
        }
        else {
          AttachmentAssert.notEmpty(taskId, () -> "taskId or processId must be defined!");
          fileWithPath = String.format("%s/tasks/%s/files/%s", rootDirectory, taskId, filename);
        }
        try {
          File file = FileUtils.getFile(fileWithPath);
          return FileUtils.readFileToByteArray(file);
        } catch (IOException e) {
          log.warn("Error writing attachment: ", e);
        }
        return null;
      }
    };
  }
}
