package io.digiexpress.eveli.client.spi.task.visitors;

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

import java.time.OffsetDateTime;
import java.util.List;

import io.digiexpress.eveli.client.api.ImmutableFormAssignment;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.spi.task.TaskStore;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FormAssignmentVisitor {
  
  private final EveliEnvirClient envir;
  private final TaskStore ctx;
  private final String taskId;
  
  public Multi<TaskClient.FormAssignment> accept() {
    return envir.runtimeQuery().getOne()
        .onItem().transform(this::mapToForms)
        .onItem().transformToMulti(items -> Multi.createFrom().items(items.stream()));    
  }
  
  private List<TaskClient.FormAssignment> mapToForms(EveliRuntime runtime) {
    return runtime.getStencil(OffsetDateTime.now())
      .getSites().values().stream()
      .flatMap(site -> {
        final var locale = site.getLocale();
        final var links = site.getLinks().values().stream()
          .filter(link -> Boolean.TRUE.equals(link.getWorkflow()))
          .filter(link -> Boolean.TRUE.equals(link.getAssignable()))
          .filter(link -> link.getFormId() != null)
          .filter(link -> link.getFormName() != null)
          .filter(link -> link.getFormTag() != null)
          .map(link -> {
            
            final TaskClient.FormAssignment result = ImmutableFormAssignment.builder()
                .serviceName(link.getName())
                .locale(locale)
                .formId(link.getFormId())
                .formName(link.getFormName())
                .formTag(link.getFormTag())
                .id(link.getId())
                .build();
            
            return result;
          })
          .toList();
        
        return links.stream();
      }).toList();
  }
}
