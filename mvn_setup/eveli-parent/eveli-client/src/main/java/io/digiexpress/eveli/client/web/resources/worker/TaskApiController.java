package io.digiexpress.eveli.client.web.resources.worker;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.spi.mq.MqEventPublisher;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/*
 * Task controller for frontdesk UI
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/worker/rest/api/tasks")
@Slf4j
public class TaskApiController {    
  private final AuthClient securityClient;
  private final TaskClient taskClient;
  private final DialobClient dialobClient;
  private final MqEventPublisher mqEventPublisher;
  
  
  @GetMapping
  public Uni<Page<Task>> taskSearch(
      @RequestParam(name="subject", defaultValue = "") String subject, 
      @RequestParam(name="additionalInfo", defaultValue = "") String additionalInfo, 
      @RequestParam(name="clientIdentificator", defaultValue = "") String clientIdentificator, 
      @RequestParam(name="assignedUser", defaultValue = "") String assignedUser, 
      @RequestParam(name="assignedRoles", defaultValue = "") String searchRole,
      @RequestParam(name="dueDate", required = false) String dueDate,
      @RequestParam(name="status", required = false) List<TaskStatus> status,
      @RequestParam(name="priority", required = false) List<TaskPriority> priority,
      Pageable pageable) {
    
    final var worker = securityClient.getUser();
    
    final var query = taskClient.paginateTasks()
        .subject(subject)
        .clientIdentificator(clientIdentificator)
        .additionalInfo(additionalInfo)
        .assignedUser(assignedUser)
        .role(searchRole)
        .dueDate(dueDate)
        .status(status)
        .priority(priority)
        .page(pageable);
    
    if (worker.getPrincipal().isAdmin()) {
      return query.findAll();
    }
    return query.requireAnyRoles(worker.getPrincipal().getRoles()).findAll();
  }
  
  @GetMapping("/all")
  public Uni<List<Task>> all() {
    final var worker = securityClient.getUser();
    if (worker.getPrincipal().isAdmin()) {
      return taskClient.queryTasks().findAll();
    }
    return taskClient.queryTasks().requireAnyRoles(worker.getPrincipal().getRoles()).findAll();
  }


  @GetMapping("/{id}")
  public Uni<ResponseEntity<Task>> getTaskById(@PathVariable("id") String id) {
    
    final var worker = securityClient.getUser();
    
    return taskClient.queryTasks().getOneById(id).onItem().transform(task -> {
      if (worker.getPrincipal().isAdmin()) {
        return ResponseEntity.ok(task);
      }
      final var isWorkerInAssignedRoles = worker.getPrincipal().isAccessGranted(task.getAssignedRoles());
      if(isWorkerInAssignedRoles) {
        return ResponseEntity.ok(task);
      }

      // alarm clocks
      return ResponseEntity.status(403).build();
      
    });
 
  }

  @PostMapping
  public Uni<ResponseEntity<TaskClient.Task>> createTask(@RequestBody TaskClient.CreateTaskCommand command) {
    final var worker = securityClient.getUser().getPrincipal();
    return taskClient.taskBuilder()
        .userId(worker.getUsername(), worker.getEmail())
        .createTask(command)
        .onItem().invoke(newTask -> {
          mqEventPublisher.publishMqEvent(newTask);
        })
        .onItem().transform(newTask -> {
          return new ResponseEntity<>(newTask, HttpStatus.CREATED);
        });
    
  }
  
  @PutMapping("/{id}")
  public Uni<ResponseEntity<TaskClient.Task>> saveTask(@PathVariable("id") String id, @RequestBody TaskClient.ModifyTaskCommand command) {
    final var worker = securityClient.getUser().getPrincipal();
    return taskClient.taskBuilder()
      .userId(worker.getUsername(), worker.getEmail())
      .modifyTask(id, command)
      .onItem().invoke(modifiedTask -> mqEventPublisher.publishMqEvent(modifiedTask))
      .onItem().transform(modifiedTask -> {
        return new ResponseEntity<>(modifiedTask, HttpStatus.OK);
      });
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Uni<TaskClient.Task> deleteTask(@PathVariable("id") String id) {
    final var worker = securityClient.getUser().getPrincipal();
    return taskClient.taskBuilder()
        .userId(worker.getUsername(), worker.getEmail())
        .deleteTask(id);
  }
  
  @GetMapping(value="/unread")
  public Uni<List<String>> getUnreadTasks() {
    final var worker = securityClient.getUser().getPrincipal();
    
    if (worker.isAdmin()) {
      return taskClient.queryUnreadUserTasks()
          .userId(worker.getUsername())
          .findAll();
    } 
    return taskClient.queryUnreadUserTasks()
        .userId(worker.getUsername())
        .requireAnyRoles(worker.getRoles())
        .findAll();
  }
  
  @GetMapping(value="/{id}/comments")
  public Uni<List<TaskClient.TaskComment>> getTaskComments(@PathVariable("id") String id)
  {
    final var authentication = securityClient.getUser();    
    return taskClient.queryTaskComments().findAllByTaskId(id)
        .onItem().transformToUni(comments -> {
          
          return taskClient.taskBuilder()
            .userId(authentication.getPrincipal().getUsername(), null)
            .addWorkerCommitViewer(id)
            .onItem().transform(junk -> comments);
          
        });
    
  }
  
  @PostMapping(value="/{id}/comments")
  public Uni<TaskClient.TaskComment> createComment(@RequestBody TaskClient.CreateTaskCommentCommand command) 
  {
    final var worker = securityClient.getUser().getPrincipal();
    return taskClient.taskBuilder()
        .userId(worker.getUsername(), worker.getEmail())
        .createTaskComment(command)
        .onItem().invoke(newComment -> {
          mqEventPublisher.publishMqEvent(newComment.getTaskId(), newComment.getVersion());
        });
  }
  
  @GetMapping(value="/{id}/reviews")
  public Uni<ResponseEntity<?>> getTaskFormReview(@PathVariable("id") String id)
  {
    return taskClient.queryTasks().getOneById(id)
    .onItem().transform(task -> {
      
      if(task.getQuestionnaireId() != null) {
        final var questionnaire = dialobClient.getQuestionnaireById(task.getQuestionnaireId());
        final var form = dialobClient.getFormById(questionnaire.getMetadata().getFormId());
        final var result = Map.of(
            "form", form,
            "session", questionnaire
            );
        return new ResponseEntity<>(result, HttpStatus.OK);
      }
      //{ form: any, session: any }
     
      return ResponseEntity.notFound().build();
      
    });
  }
  
  @Data
  @AllArgsConstructor
  private static class KeyWordsResponse { List<String> keyWords; }
  
  @GetMapping("/keywords")
  public Uni<KeyWordsResponse> getKeyWords() {
    return taskClient.queryTaskKeywords().findAllKeywords()
        .onItem().transform(resp -> new KeyWordsResponse(resp));
  }
}
