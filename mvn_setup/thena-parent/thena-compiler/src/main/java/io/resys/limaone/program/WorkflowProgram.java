package io.resys.limaone.program;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.ModelError;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;



public interface WorkflowProgram extends Program {

  WorkflowFormResult runForm(Runtime runtime, WorkflowUser identity, WorkflowProps programInput);  
  WorkflowFlowResult runFlow(Runtime runtime, WorkflowForm form, ProgramInput programInput);

  
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
    Boolean getAssignment();
    
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

  
  @Value.Immutable @JsonSerialize(as = ImmutableWorkflowUser.class) @JsonDeserialize(as = ImmutableWorkflowUser.class)
  interface WorkflowUser {
    // even anon user needs to have some anon identity and some primitive roles
    String getIdentity();
    List<String> getIdentityRoles();
    Boolean getAnon(); 
    
    
    // Anything relevant to pass downstream
    @Nullable JsonObject getAdditionalProps();
    
    // Loose data    
    @Nullable Boolean getProtectionOrder();
    @Nullable String getCompanyName();
    @Nullable String getFirstName();
    @Nullable String getLastName();
    @Nullable String getLanguage();
    @Nullable String getEmail();
    @Nullable String getAddress();

    @Nullable String getRepresentativeFirstName();
    @Nullable String getRepresentativeLastName();
    @Nullable String getRepresentativeIdentity(); 
  }
}
