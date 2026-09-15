package io.digiexpress.eveli.client.web.resources.worker;

import java.io.IOException;

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
import java.net.URLConnection;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import jakarta.activation.MimetypesFileTypeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("${eveli.attachment.fs.attachment-url-base:/worker/rest/api/attachments/fs}")
@Slf4j
@RequiredArgsConstructor
/**
 * API controller for file attachments.
 */
public class AttachmentFilesystemController {
  
  private final AttachmentCommands client;
  private final WorkerAuthClient securityClient;
  private static final MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();

  /**
   * Returns given file from path.
   * @param taskId id for task to find attachment.
   * @param filename attachment file name.
   * @throws URISyntaxException
   */
  @GetMapping("/tasks/{taskId}/files/{filename}")
  public ResponseEntity<byte[]> getTaskAttachment(
      @PathVariable String taskId, 
      @PathVariable String filename 
      ) 
      throws URISyntaxException 
  {
    final var authentication = securityClient.getUser();
    log.debug("Attachment file GET API call for task id: {}, file: {}, from user {}", taskId, filename, authentication.getPrincipal().getUsername());
    
    var content = client.contentDownload().taskId(taskId).filename(filename).build();
    return ResponseEntity.ok().contentType(MediaType.valueOf(fileTypeMap.getContentType(filename)))
        .contentLength(content.length).body(content);
  }
  
  /**
   * Returns given file from path.
   * @param taskId id for task to find attachment.
   * @param filename attachment file name.
   * @throws URISyntaxException
   */
  @GetMapping("/processes/{processId}/files/{filename}")
  public ResponseEntity<byte[]> getProcessAttachment(
      @PathVariable String processId, 
      @PathVariable String filename 
      ) 
          throws URISyntaxException 
  {
    final var authentication = securityClient.getUser();
    log.debug("Attachment file GET API call for proces id: {}, file: {}, from user {}", processId, filename, authentication.getPrincipal().getUsername());
    
    var content = client.contentDownload().processId(processId).filename(filename).build();
    String contentType = URLConnection.guessContentTypeFromName(filename);
    return ResponseEntity.ok().contentType(MediaType.valueOf(contentType))
        .header("Content-Disposition", "attachment; filename=" + filename)
        .contentLength(content.length).body(content);
  }
  
  @PutMapping("/tasks/{taskId}/files/{filename}")
  public ResponseEntity<Void> uploadTaskAttachment(
      @PathVariable String taskId, 
      @PathVariable String filename, 
      @RequestBody byte[] file)
      throws URISyntaxException 
  {
    try {
      client.contentUpload().filename(filename).taskId(taskId).build(file);
    } catch (Exception e) {
      log.error("Error uploading task file {}", filename, e);
    }
    return ResponseEntity.ok().build();
  }
  
  @PutMapping("/processes/{processId}/files/{filename}")
  public ResponseEntity<Void> uploadProcessAttachment(
      @PathVariable String processId, 
      @PathVariable String filename, 
      @RequestBody byte[] file)
          throws URISyntaxException 
  {
    try {
      client.contentUpload().filename(filename).processId(processId).build(file);
    } catch (Exception e) {
      log.error("Error uploading process file {}", filename, e);
    }
    return ResponseEntity.ok().build();
  }

}
