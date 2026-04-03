package io.digiexpress.eveli.client.spi.task.visitors;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import io.digiexpress.eveli.client.api.ImmutableFormAssignment;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.spi.task.TaskStore;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FormAssignmentVisitor {
  
  private final io.resys.limaone.program.Runtime envir;
  private final TaskStore ctx;
  private final String taskId;
  
  public Multi<TaskClient.FormAssignment> accept() {
    final var templates = new ArrayList<TaskClient.FormAssignment>();
    envir.getBundle().queryWorkflows().forEach(wk -> {
      if(!wk.getAssignable()) {
        return;
      }
      
      for(final var locale : wk.getLocales()) {
        final TaskClient.FormAssignment result = ImmutableFormAssignment.builder()
            .serviceName(wk.getName())
            .locale(locale)
            .formId(wk.getFormId())
            .formName(wk.getFormName())
            .formTag(wk.getFormTag())
            .id(wk.getId())
            .build();
        templates.add(result);
      }
    });
    
    return Multi.createFrom().items(templates.stream());    
  }
}
