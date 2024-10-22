package io.digiexpress.eveli.client.web.resources;

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

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.AttachmentCommands.Attachment;
import io.digiexpress.eveli.client.api.AttachmentCommands.AttachmentUpload;
import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.iam.PortalAccessValidator;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/attachments")
@Slf4j
/**
 * Internal controller for attachments, for use from portal backend.
 */
public class PortalAttachmentController {
  
  private final PortalClient client;
  private final PortalAccessValidator validator;
  
  public PortalAttachmentController(PortalClient client, PortalAccessValidator validator) {
    this.client = client;
    this.validator = validator;
  }
  
  /**
   * Returns list of process attachments. In case of no attachments returns empty list.
   * @param processId id for process.
   * @return process attachments.
   * @throws URISyntaxException
   */
  @GetMapping("/process/{processId}/files/")
  public ResponseEntity<List<Attachment>> listAttachments(@PathVariable String processId,
      @AuthenticationPrincipal Jwt principal) 
    throws URISyntaxException 
  {
    validator.validateProcessIdAccess(processId, principal);
    return ResponseEntity.ok(client.attachments().query().processId(processId));
  }

  /**
   * Returns Signed URL for downloading attachment file in location header in HTTP response with status FOUND (302).
   * If specified file does not exist, then return code is NOT FOUND (404).
   * @param processId id for process to find attachment.
   * @param filename attachment file name.
   * @return FOUND Http status code with redirection link.
   * @throws URISyntaxException
   * @throws UnsupportedEncodingException 
   */
  @GetMapping("/process/{processId}/files/{filename}")
  public ResponseEntity<Void> getAttachment(
    @PathVariable String processId, 
    @PathVariable String filename,
    @AuthenticationPrincipal Jwt principal) 
    throws URISyntaxException 
  {
    validator.validateProcessIdAccess(processId, principal);
    final var attachmentUrl = client.attachments().url().encodePath(filename).processId(processId);
    if (attachmentUrl.isPresent()) {
      return ResponseEntity.status(HttpStatus.FOUND).location(attachmentUrl.get().toURI()).build();
    }
    return ResponseEntity.notFound().build();
  }
  
  /**
   * Creates signed URL for PUT request to upload specified attachment file to process.
   * @param processId - id for process
   * @param filename - filename to upload
   * @return - signed url in response field "putRequestUrl". In case of error returns NOT FOUND status response.
   * @throws URISyntaxException
   * @throws UnsupportedEncodingException 
   */
  @PostMapping("/process/{processId}/files/")
  public ResponseEntity<AttachmentUpload> getUploadUrl(
    @PathVariable String processId, 
    @RequestParam(name="filename") String filename,
    @AuthenticationPrincipal Jwt principal) 
    throws URISyntaxException 
  {
    validator.validateProcessIdAccess(processId, principal);
    final var uploadUrl = client.attachments().upload().encodePath(filename).processId(processId);
    if (uploadUrl.isPresent()) {
      return ResponseEntity.ok(uploadUrl.get());
    }
    return ResponseEntity.notFound().build();
  }
  
  /**
   * Returns list of task attachments. 
   * If task is associated with process then process-bound attachments are returned.
   * For stand-alone tasks (manual tasks) task-bound attachments are returned.
   * In case of no attachments returns empty list.
   * @param taskId id of task.
   * @return task attachments.
   * @throws URISyntaxException
   */
  @GetMapping("/task/{taskId}/files/")
  public ResponseEntity<List<Attachment>> listTaskAttachments(@PathVariable String taskId,
      @AuthenticationPrincipal Jwt principal) 
      throws URISyntaxException 
  {
    final var processId = getProcessIdFromTask(taskId);
    validator.validateProcessIdAccess(processId, principal);
    List<Attachment> result = processId != null ?
        client.attachments().query().processId(processId) : client.attachments().query().taskId(taskId);
    return ResponseEntity.ok(result);
  }
  

  /**
   * Returns Signed URL for downloading attachment file in location header in HTTP response with status FOUND (302).
   * If task is associated with process then process-bound attachment is returned.
   * For stand-alone tasks (manual tasks) task-bound attachment is returned.
   * If specified file does not exist, then return code is NOT FOUND (404).
   * @param taskId id for task to find attachment.
   * @param filename attachment file name.
   * @return FOUND Http status code with redirection link.
   * @throws URISyntaxException
   */
  @GetMapping("/task/{taskId}/files/{filename}")
  public ResponseEntity<Void> getTaskAttachment(
      @PathVariable String taskId, 
      @PathVariable String filename,
      @AuthenticationPrincipal Jwt principal) 
      throws URISyntaxException 
  {
    String processId = getProcessIdFromTask(taskId);
    validator.validateProcessIdAccess(processId, principal);
    final var attachmentUrl = processId != null ?
        client.attachments().url().encodePath(filename).processId(processId) : 
        client.attachments().url().encodePath(filename).taskId(taskId);
    if (attachmentUrl.isPresent()) {
      return ResponseEntity.status(HttpStatus.FOUND).location(attachmentUrl.get().toURI()).build();
    }
    return ResponseEntity.notFound().build();
  }
  
  /**
   * Creates signed url to upload task attachment with PUT request.
   * If task is associated with process then attachment is uploaded for process.
   * For stand-alone tasks (manual tasks) then attachment is uploaded for task.
   * @param taskId id for task
   * @param filename attachment file name. 
   * @return Signed url for upload with OK (200) response code. In case of error NOT FOUND response code.
   * @throws URISyntaxException
   */
  @PostMapping("/task/{taskId}/files/")
  public ResponseEntity<AttachmentUpload> getTaskAttachmentUploadUrl(
      @PathVariable String taskId, 
      @RequestParam(name="filename") String filename,
      @AuthenticationPrincipal Jwt principal) 
      throws URISyntaxException 
  {
    
    final var processId = getProcessIdFromTask(taskId);
    validator.validateProcessIdAccess(processId, principal);
    final var uploadUrl = processId != null ?
        client.attachments().upload().encodePath(filename).processId(processId) :
        client.attachments().upload().encodePath(filename).taskId(taskId);
    if (uploadUrl.isPresent()) {
      return ResponseEntity.ok(uploadUrl.get());
    }
    return ResponseEntity.notFound().build();
  }
  
  
  private String getProcessIdFromTask(String taskId) {
    if (taskId == null) {
      return null;
    }
    return client.process().query().getByTaskId(taskId).map(e -> e.getId().toString()).orElse(null);
  }
}
