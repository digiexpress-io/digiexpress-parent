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
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.AttachmentCommands.Attachment;
import io.digiexpress.eveli.client.api.AttachmentCommands.AttachmentUpload;
import io.digiexpress.eveli.client.api.ImmutableTaskAttachment;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskAttachment;
import io.digiexpress.eveli.client.api.TaskClient.TaskAttachment.AttachmentSource;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.smallrye.mutiny.Uni;
import jakarta.activation.MimetypesFileTypeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/worker/rest/api")
@Slf4j
@RequiredArgsConstructor
/**
 * API controller for attachments, for use from frontdesk UI.
 */
public class AttachmentApiController {
  
  private final AttachmentCommands client;
  private final TaskClient taskClient;  
  private final WorkerAuthClient securityClient;
  private static final Duration timeout = Duration.ofMillis(10000);
  private static final MimetypesFileTypeMap fileTypeMap = new MimetypesFileTypeMap();
  
  private record FileUploadUrlBody(String filename, Long size, String type) {}
  
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
    log.debug("Attachment list GET API call for task id: {} from user {}", taskId, authentication.getPrincipal().getUsername());
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
    log.debug("Attachment file GET API call for task id: {}, file: {}, from user {}", taskId, filename, authentication.getPrincipal().getUsername());
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
  
  @DeleteMapping("/tasks/{taskId}/files/{filename}")
  public Uni<ResponseEntity<Void>> deleteTaskAttachment(
      @PathVariable String taskId, 
      @PathVariable String filename 
      ) 
      throws URISyntaxException 
  {
    final var worker = securityClient.getUser().getPrincipal();
    final var authentication = securityClient.getUser();
    log.info("Attachment file DELETE API call for task id: {}, file: {}, from user {}", taskId, filename, authentication.getPrincipal().getUsername());
    if (!checkTaskAccess(taskId, authentication)) {
      return Uni.createFrom().item(ResponseEntity.notFound().build());
    }
    final var processId = getProcessIdFromTask(taskId);
    if (processId != null) {
      client.remove().filename(filename).removeByProcessId(processId);
    }
    else {
      client.remove().filename(filename).removeByTaskId(taskId);
    }
    return taskClient.taskBuilder()
      .userId(worker.getUsername(), worker.getEmail())
      .removeTaskAttachment(taskId, filename)
      .onItem().transform(task -> {
        return ResponseEntity.noContent().build();
      });
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
  public Uni<ResponseEntity<AttachmentUpload>> getTaskAttachmentUploadUrl(
      @PathVariable String taskId, 
      @RequestBody FileUploadUrlBody file)
      throws URISyntaxException 
  {
    final var worker = securityClient.getUser().getPrincipal();
    final var authentication = securityClient.getUser();
    log.info("Attachment file POST API call for task id: {}, file: {}, from user {}", taskId, file.filename, authentication.getPrincipal().getUsername());
    if (!checkTaskAccess(taskId, authentication)) {
      return Uni.createFrom().item(ResponseEntity.notFound().build());
    }
    final var processId = getProcessIdFromTask(taskId);
    
    TaskAttachment taskAttachment = ImmutableTaskAttachment.builder()
        .name(file.filename)
        .created(OffsetDateTime.now(ZoneId.of("UTC")))
        .creator(authentication.getPrincipal().getUsername())
        .size(file.size != null ? file.size : 0L)
        .source(AttachmentSource.FRONTDESK)
        .type(StringUtils.isAllBlank(file.type) ? fileTypeMap.getContentType(file.filename) : file.type)
        .build();
    return taskClient.taskBuilder()
      .userId(worker.getUsername(), worker.getEmail())
      .addTaskAttachment(taskId, taskAttachment)
      .onItem().transform(task -> {
        final var uploadUrl = processId != null ?
            client.upload().encodePath(file.filename).processId(processId) :
            client.upload().encodePath(file.filename).taskId(taskId);
        if (uploadUrl.isPresent()) {
          return ResponseEntity.ok(uploadUrl.get());
        }
        return ResponseEntity.notFound().build();
      });
  }

  private boolean checkTaskAccess(String taskId, WorkerAuthClient.User authentication) {
    log.debug("Checking task {} access for user {}", taskId, authentication.getPrincipal().getUsername());
    List<String> roles = authentication.getPrincipal().getRoles();
    
    final var task = taskClient.queryTasks().getOneById(taskId).await().atMost(timeout);
    if(!authentication.getPrincipal().isAdmin() && !authentication.getPrincipal().isAccessGranted(task.getAssignedRoles())) {
      log.warn("Access to task {} disabled for roles {} or task not found", taskId, roles);
      return false;
    }
    
    log.debug("Check for task {} access PASSED", taskId);
    return true;
  }

  private String getProcessIdFromTask(String taskId) {
    return taskClient
        .queryTaskProcesess()
        .findOneByTaskId(taskId)
        .map(e -> e.map(x ->  x.getId().toString()).orElse(null))
        .await().atMost(timeout);
  }
}
