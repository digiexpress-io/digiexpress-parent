package io.digiexpress.eveli.client.test.feedback;

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

import java.time.Duration;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.test.task.TaskEnvirSetupDebugDb;



@Disabled
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
public class DebugTaskData extends TaskEnvirSetupDebugDb {

  @Autowired 
  TaskClient taskClient;
  
  @Test
  void run() {
    final var diff = taskClient.queryTasks().getOneTaskDiff("106", "960a35a182a6a88eb058fefe33f482d5").await().atMost(Duration.ofMinutes(1));
    
    
    System.out.print(String.join("\r\n", diff.getValues().stream().map(e -> e.getPath()).toList()));
    
    
    taskClient.taskBuilder()
      .userId("blaaa", "blaaaax")
      .addWorkerCommitViewer("106")
      .await().atMost(Duration.ofMinutes(1));
  }
}
