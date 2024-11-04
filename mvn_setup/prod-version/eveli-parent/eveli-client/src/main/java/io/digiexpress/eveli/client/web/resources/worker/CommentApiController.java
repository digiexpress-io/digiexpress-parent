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


import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@Transactional
@RequestMapping("/rest/api/worker")
@Slf4j
@RequiredArgsConstructor
public class CommentApiController
{
  private final TaskClient taskClient;
  private final AuthClient securityClient;
  private final TaskAccessRepository taskAccessRepository;
  private final TaskRepository taskRepository;
  
  @GetMapping(value="/tasks/{id}/comments")
  public ResponseEntity<List<TaskClient.TaskComment>> getTaskComments(@PathVariable("id") Long id)
  {
    final var authentication = securityClient.getUser();
    new TaskControllerBase(taskAccessRepository).registerUserTaskAccess(id, Optional.of(taskRepository.getOneById(id)), authentication.getPrincipal().getUsername());
    final var comments = taskClient.queryComments().findAllByTaskId(id);
 
    return new ResponseEntity<>(comments, HttpStatus.OK);
  }

  @GetMapping("/comments/{id}")
  public ResponseEntity<TaskClient.TaskComment> getCommentById(@PathVariable("id") Long id) 
  {
    return ResponseEntity.ok(taskClient.queryComments().getOneById(id));
  }
  
  @PostMapping("/comments")
  public ResponseEntity<TaskClient.TaskComment> createComment(@RequestBody TaskClient.CreateTaskCommentCommand command) 
  {
    final var worker = securityClient.getUser().getPrincipal();
    final var newComment = taskClient.commandTaskBuilder()
        .userId(worker.getUsername(), worker.getEmail())
        .createTaskComment(command);
    return new ResponseEntity<>(newComment, HttpStatus.CREATED);
  }
}
