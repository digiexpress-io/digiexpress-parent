package io.digiexpress.eveli.client.web.resources.assets;




import java.io.Serializable;

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

import org.immutables.value.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.dialob.api.DialobClient;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.spi.StencilComposerImpl;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequestMapping("/worker/rest/api/assets/workflows")
@RequiredArgsConstructor
@Slf4j
public class AssetsWorkflowController {

  private final StencilClient stencilClient;
  private final DialobClient dialobClient;
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflow.class)
  @JsonDeserialize(as = ImmutableWorkflow.class)
  public interface Workflow extends Serializable {
    String getId();
    WorkflowBody getBody();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflowBody.class)
  @JsonDeserialize(as = ImmutableWorkflowBody.class)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public interface WorkflowBody {
    String getName();
    @Nullable String getFormId();
    @Nullable String getFormName();
    @Nullable String getFormTag();
    @Nullable String getFlowName();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflowMutator.class)
  @JsonDeserialize(as = ImmutableWorkflowMutator.class)
  public interface WorkflowMutator {
    String getId();
    @Nullable String getName();
    @Nullable String getFormName();
    @Nullable String getFormTag();
    @Nullable String getFlowName();
  }
  
  
  @GetMapping
  public Uni<List<Workflow>> findAllWorkflows() {
    return stencilClient.getStore().query().head().onItem().transform(state -> state.getWorkflows().values().stream()
        .map(release -> {
          final WorkflowBody body = ImmutableWorkflowBody.builder()
              .formId(release.getBody().getFormId())
              .formName(release.getBody().getFormName())
              .formTag(release.getBody().getFormTag())
              .flowName(release.getBody().getFlowName())
              .name(release.getBody().getValue())
              .build();
          
          final Workflow result = ImmutableWorkflow.builder()
            .id(release.getId())
            .body(body)
            .build();
          
          return result;
        })
        .toList()
    );
  }
  
  @GetMapping("/{id}")
  public Uni<Workflow> get(@PathVariable("id") String id) {
    return findAllWorkflows().onItem().transform(values -> values.stream().filter(e -> e.getId().equals(id)).findFirst().get());
  }
  
  @PutMapping("/{id}")
  public Uni<Workflow> save(@PathVariable("id") String id, @RequestBody WorkflowMutator workflow) {
    final var formId = dialobClient.getFormByNameAndTag(workflow.getFormName(), workflow.getFormTag()).getId();
    final var command = io.thestencil.client.api.ImmutableWorkflowMutator.builder()
        .workflowId(id)
        .flowName(workflow.getFlowName())
        .value(workflow.getName())
        .formId(formId)
        .formTag(workflow.getFormTag())
        .formName(workflow.getFormName())
        .build();
    return new StencilComposerImpl(stencilClient)
        .update().workflow(command)
        .onItem().transformToUni(saved -> get(saved.getId()));

  }
}
