package io.digiexpress.eveli.client.web.resources.worker;

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
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.AttachmentCommands.Attachment;
import io.digiexpress.eveli.client.api.AttachmentCommands.AttachmentUpload;
import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/rest/api/worker")
@Slf4j
@RequiredArgsConstructor
/**
 * API controller for attachments, for use from frontdesk UI.
 */
public class AttachmentApiController {
  
  private final AttachmentCommands client;
  private final TaskClient taskClient;  
  private final AuthClient securityClient;
  private final ProcessClient processClient;
  
  /**
   * Returns list of task attachments. 
   * If task is associated with process then process-bound attachments are returned.
   * For stand-alone tasks (manual tasks) task-bound attachments are returned.
   * In case of no attachments returns empty list.
   * @param taskId id of task.
   * @return task attachments.
   * @throws URISyntaxException
   */
  @GetMapping("/tasks/{taskId}/files/")
  public ResponseEntity<List<Attachment>> listTaskAttachments(@PathVariable String taskId) 
      throws URISyntaxException 
  {
    final var authentication = securityClient.getUser();
    log.info("Attachment list GET API call for task id: {} from user {}", taskId, authentication.getPrincipal().getUsername());
    if (!checkTaskAccess(taskId, authentication)) {
      return ResponseEntity.notFound().build();
    }
    final var processId = getProcessIdFromTask(taskId);
    final var result = processId != null ? client.query().processId(processId) : client.query().taskId(taskId);
    
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
  @GetMapping("/tasks/{taskId}/files/{filename}")
  public ResponseEntity<Void> getTaskAttachment(
      @PathVariable String taskId, 
      @PathVariable String filename 
      ) 
      throws URISyntaxException 
  {
    final var authentication = securityClient.getUser();
    log.info("Attachment file GET API call for task id: {}, file: {}, from user {}", taskId, filename, authentication.getPrincipal().getUsername());
    if (!checkTaskAccess(taskId, authentication)) {
      return ResponseEntity.notFound().build();
    }
    final var processId = getProcessIdFromTask(taskId);
    final var attachmentUrl = processId != null ?
        client.url().encodePath(filename).processId(processId) : 
        client.url().encodePath(filename).taskId(taskId);
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
  @PostMapping("/tasks/{taskId}/files/")
  public ResponseEntity<AttachmentUpload> getTaskAttachmentUploadUrl(
      @PathVariable String taskId, 
      @RequestParam(name="filename") String filename) 
      throws URISyntaxException 
  {
    final var authentication = securityClient.getUser();
    log.info("Attachment file POST API call for task id: {}, file: {}, from user {}", taskId, filename, authentication.getPrincipal().getUsername());
    if (!checkTaskAccess(taskId, authentication)) {
      return ResponseEntity.notFound().build();
    }
    final var processId = getProcessIdFromTask(taskId);
    final var uploadUrl = processId != null ?
        client.upload().encodePath(filename).processId(processId) :
        client.upload().encodePath(filename).taskId(taskId);
    if (uploadUrl.isPresent()) {
      return ResponseEntity.ok(uploadUrl.get());
    }
    return ResponseEntity.notFound().build();
  }

  private boolean checkTaskAccess(String taskId, AuthClient.User authentication) {
    log.debug("Checking task {} access for user {}", taskId, authentication.getPrincipal().getUsername());
    List<String> roles = authentication.getPrincipal().getRoles();
    
    final var task = taskClient.queryTasks().getOneById(Long.parseLong(taskId));
    if(!authentication.getPrincipal().isAdmin() && !authentication.getPrincipal().isAccessGranted(task.getAssignedRoles())) {
      log.warn("Access to task {} disabled for roles {} or task not found", taskId, roles);
      return false;
    }
    
    log.debug("Check for task {} access PASSED", taskId);
    return true;
  }

  private String getProcessIdFromTask(String taskId) {
    return processClient.queryInstances().findOneByTaskId(Long.parseLong(taskId)).map(e -> e.getId().toString()).orElse(null);
  }
}
