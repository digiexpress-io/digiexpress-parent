package io.digiexpress.eveli.client.web.resources;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.AttachmentCommands.Attachment;
import io.digiexpress.eveli.client.api.AttachmentCommands.AttachmentUpload;
import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.TaskCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/attachments")
@Slf4j
@RequiredArgsConstructor
/**
 * API controller for attachments, for use from frontdesk UI.
 */
public class AttachmentApiController {
  
  private final PortalClient client;
  
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
      Authentication authentication) 
      throws URISyntaxException 
  {
    log.info("Attachment list GET API call for task id: {} from user {}", taskId, authentication.getName());
    if (!checkTaskAccess(taskId, authentication)) {
      return ResponseEntity.notFound().build();
    }
    final var processId = getProcessIdFromTask(taskId);
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
      Authentication authentication) 
      throws URISyntaxException 
  {
    log.info("Attachment file GET API call for task id: {}, file: {}, from user {}", taskId, filename, authentication.getName());
    if (!checkTaskAccess(taskId, authentication)) {
      return ResponseEntity.notFound().build();
    }
    String processId = getProcessIdFromTask(taskId);
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
      Authentication authentication) 
      throws URISyntaxException 
  {
    log.info("Attachment file POST API call for task id: {}, file: {}, from user {}", taskId, filename, authentication.getName());
    if (!checkTaskAccess(taskId, authentication)) {
      return ResponseEntity.notFound().build();
    }
    final var processId = getProcessIdFromTask(taskId);
    final var uploadUrl = processId != null ?
        client.attachments().upload().encodePath(filename).processId(processId) :
        client.attachments().upload().encodePath(filename).taskId(taskId);
    if (uploadUrl.isPresent()) {
      return ResponseEntity.ok(uploadUrl.get());
    }
    return ResponseEntity.notFound().build();
  }

  private boolean checkTaskAccess(String taskId, Authentication authentication) {
    log.debug("Checking task {} access for user {}", taskId, authentication.getName());
    List<String> roles = getRoles(authentication);
    if (getTask(taskId, roles).isEmpty()) {
      log.warn("Access to task {} disabled for roles {} or task not found", taskId, roles);
      return false;
    }
    log.debug("Check for task {} access PASSED", taskId);
    return true;
  }

  private List<String> getRoles(Authentication authentication) {
    if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
      List<String> roles = authentication.getAuthorities().stream().map(auth->auth.getAuthority()).collect(Collectors.toList());
      return roles;
    }
    return Collections.emptyList();
  }
 
  
  private Optional<TaskCommands.Task> getTask(String id, List<String> roles) {
    return client.task().find(id, roles);
  }
  private String getProcessIdFromTask(String taskId) {
    return client.process().query().getByTaskId(taskId).map(e -> e.getId().toString()).orElse(null);
  }
}
