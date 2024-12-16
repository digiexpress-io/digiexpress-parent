package io.digiexpress.eveli.client.spi.task;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.event.TaskNotificator;
import io.digiexpress.eveli.client.persistence.entities.TaskRefGenerator;
import io.digiexpress.eveli.client.persistence.repositories.CommentRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskAccessRepository;
import io.digiexpress.eveli.client.persistence.repositories.TaskRepository;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskClientImpl implements TaskClient {

  private final JdbcTemplate jdbcTemplate;
  private final TaskRepository taskRepository;
  private final TaskRefGenerator taskRefGenerator;
  private final TaskNotificator notificator;
  private final TaskAccessRepository taskAccessRepository;
  private final CommentRepository commentRepository;
  
  @Override
  public PaginateTasks paginateTasks() {
    return new PaginateTasksImpl(taskRepository);
  }
  @Override
  public QueryTasks queryTasks() {
    return new QueryTasks() {
      @Override
      public Task getOneById(long taskId) {
        return PaginateTasksImpl.map(taskRepository.getOneById(taskId));
      }
    };
  }

  @Override
  public TaskCommandBuilder taskBuilder() {
    return new TaskCommandBuilder() {
      private String userId, userEmail;
      @Override
      public TaskCommandBuilder userId(String userId, String userEmail) {
        this.userId = userId;
        this.userEmail = userEmail;
        return this;
      }
      @Override
      public TaskComment createTaskComment(CreateTaskCommentCommand command) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        //TaskAssert.notEmpty(userEmail, () -> "userEmail can't be empty!");
        return new CreateOneTaskComment(userId, taskRepository, commentRepository, notificator, taskAccessRepository).create(command);
      }
      @Override
      public Task createTask(CreateTaskCommand command) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        //TaskAssert.notEmpty(userEmail, () -> "userEmail can't be empty!");
        return new CreateOneTask(userId, taskRepository, taskRefGenerator, notificator, taskAccessRepository).create(command);
      }
      @Override
      public Task modifyTask(Long taskId, ModifyTaskCommand command) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        //TaskAssert.notEmpty(userEmail, () -> "userEmail can't be empty!");
        return new ModifyOneTask(userId, userEmail, taskRepository, notificator, taskAccessRepository).modify(taskId, command);
      }
      @Override
      public Task deleteTask(Long taskId) {
        TaskAssert.notEmpty(userId, () -> "userId can't be empty!");
        //TaskAssert.notEmpty(userEmail, () -> "userEmail can't be empty!");
        return new DeleteOneTask(userId, userEmail, taskRepository, notificator, jdbcTemplate).delete(taskId);
      }
    };
  }

  @Override
  public QueryTaskComments queryComments() {
    return new QueryTaskComments() {
      
      @Override
      public TaskComment getOneById(long commentId) {
        return commentRepository.findById(commentId).map(CreateOneTaskComment::map).get();
      }
      
      @Override
      public List<TaskComment> findAllByTaskId(long taskId) {
        return commentRepository.findByTaskId(taskId).stream().map(CreateOneTaskComment::map).toList();
      }
    };
  }

  @Override
  public QueryTaskKeywords queryKeywords() {
    return new QueryTaskKeywords() {
      @Override
      public List<String> findAllKeywords() {

        final var result = new ArrayList<String>();
        jdbcTemplate.query(
            "SELECT distinct key_words from task_keywords order by 1",
            (rs, rowNum) -> rs.getString(1)
        ).forEach(keyword -> result.add(keyword));
        return Collections.unmodifiableList(result);
      }
    };
  }
  @Override
  public QueryUnreadUserTasks queryUnreadUserTasks() {
    return new QueryUnreadUserTasks() {
      private String userId;
      private final List<String> roles = new ArrayList<>();
      @Override
      public QueryUnreadUserTasks userId(String userId) {
        this.userId = userId;
        return this;
      }
      @Override
      public QueryUnreadUserTasks requireAnyRoles(List<String> roles) {
        TaskAssert.notEmpty("roles", () -> "roles can't be empty!");
        this.roles.addAll(roles);
        return this;
      }
      @Override
      public List<Long> findAll() {
        TaskAssert.notEmpty("userId", () -> "userId can't be empty!");
        if(roles.isEmpty()) {
          return taskRepository.findUnreadTasks(userId);
        } 
        return taskRepository.findUnreadTasksByRole(userId, roles);
      }
    };
  }

}
