package io.resys.limaone.program;

/*-
 * #%L
 * limaone-compiler
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

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.ModelError;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.program.ProgramInput.Participant;
import io.resys.limaone.program.ProgramInput.ParticipantForm;
import jakarta.annotation.Nullable;



public interface WorkflowProgram extends Program {

  Boolean getAssignable();

  String getFormId();
  String getFormName();
  String getFormTag();
  Map<String, String> getLabels();
  
  WorkflowFormResult runForm(Participant identity, WorkflowProps programInput);
  
  /** set up internally
    flowInput.put("questionnaireId", instance.getQuestionnaireId());
    flowInput.put("workflowName", instance.getWorkflowName());
   */
  WorkflowFlowResult runFlow(ParticipantForm form, ProgramInput programInput);

  
  @Value.Immutable @JsonSerialize(as = ImmutableWorkflowFlowResult.class) @JsonDeserialize(as = ImmutableWorkflowFlowResult.class)
  interface WorkflowFlowResult extends ProgramResult {
    Optional<FlowResult> getFlow();
    List<ModelError> getErrors();
    WorkflowExecutionStatus getStatus();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableWorkflowFormResult.class) @JsonDeserialize(as = ImmutableWorkflowFormResult.class)
  interface WorkflowFormResult extends ProgramResult {
    Boolean getAccessAllowed();
    Optional<WorkflowForm> getForm();
    
    List<ModelError> getErrors();
    WorkflowExecutionStatus getStatus();
  }
  
  enum WorkflowExecutionStatus {
    COMPLETED, ERROR
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableWorkflowForm.class) @JsonDeserialize(as = ImmutableWorkflowForm.class)
  interface WorkflowForm extends Serializable {
    Long getExpiresInSeconds();
    OffsetDateTime getExpiresAt();
    
    String getTagName();
    String getWorkflowName();
    String getFlowName();
    
    String getFormName();
    String getFormVersion();
    String getFormSessionId();   
  }
  
  
  
  // inputs
  interface WorkflowProps {
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableWorkflowDefaultProps.class) @JsonDeserialize(as = ImmutableWorkflowDefaultProps.class)
  interface WorkflowDefaultProps extends WorkflowProps {
    @Nullable String getArticleName();
    @Nullable String getParentArticleName();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableWorkflowAssignmentProps.class) @JsonDeserialize(as = ImmutableWorkflowAssignmentProps.class)
  interface WorkflowAssignmentProps extends WorkflowProps {
    String getTaskId(); // source of assignment
  }


}
