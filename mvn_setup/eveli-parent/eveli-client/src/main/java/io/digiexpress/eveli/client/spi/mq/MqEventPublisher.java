package io.digiexpress.eveli.client.spi.mq;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import org.springframework.context.ApplicationEventPublisher;

import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import lombok.AllArgsConstructor;
import lombok.Data;


public class MqEventPublisher {
  
  private final ApplicationEventPublisher publisher;
  
  public MqEventPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }
  
  public void publishMqEvent(Task task, TaskCommentSource source) {
    publisher.publishEvent(new MqEvent(task.getId(), task.getVersion(), source));
  }
  
  public void publishMqEvent(String taskId, String version, TaskCommentSource source) {
    publisher.publishEvent(new MqEvent(taskId, version, source));
  }
  
  @Data
  @AllArgsConstructor
  public static class MqEvent {
    private final String taskId;
    private final String commitId;
    private final TaskCommentSource source;
  }
}
