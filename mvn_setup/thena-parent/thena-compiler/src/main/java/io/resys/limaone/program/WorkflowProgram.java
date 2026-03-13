package io.resys.limaone.program;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.ModelError;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;



public interface WorkflowProgram extends Program {

  WorkflowExecutor run( 
      Runtime runtime,
      WorkflowIdentityProps identity,
      WorkflowInputProps programInput
  );
  
  interface WorkflowExecutor {
    WorkflowInstanceResult andGetForm();
    WorkflowResult andGetFlow();
  }
  
  interface WorkflowResult extends ProgramResult {
    
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableWorkflowInstanceResult.class) @JsonDeserialize(as = ImmutableWorkflowInstanceResult.class)
  interface WorkflowInstanceResult extends ProgramResult {
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
    String getUserIdentity();
    String getUserLocale();
    
    Long getExpiresInSeconds();
    OffsetDateTime getExpiresAt();
    
    Boolean getAnon();
    Boolean getAssignment();
    
    String getTagName();
    String getWorkflowName();
    String getFlowName();
    
    String getFormName();
    String getFormVersion();
    String getFormSessionId();
    
    @Nullable String getArticleName();
    @Nullable String getParentArticleName();    
  }
  
  
  
  // inputs
  interface WorkflowInputProps {}
  
  @Value.Immutable @JsonSerialize(as = ImmutableWorkflowDefaultProps.class) @JsonDeserialize(as = ImmutableWorkflowDefaultProps.class)
  interface WorkflowDefaultProps extends WorkflowInputProps {
    @Nullable String getArticleName();
    @Nullable String getParentArticleName();
    @Nullable String getLocale();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableWorkflowAssignmentProps.class) @JsonDeserialize(as = ImmutableWorkflowAssignmentProps.class)
  interface WorkflowAssignmentProps extends WorkflowInputProps {
    String getTaskId(); // source of assignment
    @Nullable String getLocale();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableWorkflowAnonProps.class) @JsonDeserialize(as = ImmutableWorkflowAnonProps.class)
  interface WorkflowAnonProps extends WorkflowInputProps {
    @Nullable String getLocale();
    @Nullable String getArticleName();
    @Nullable String getParentArticleName();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableWorkflowIdentityProps.class) @JsonDeserialize(as = ImmutableWorkflowIdentityProps.class)
  interface WorkflowIdentityProps {
    // even anon user needs to have some anon identity and some primitive roles
    String getIdentity();
    List<String> getIdentityRoles();
    
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
