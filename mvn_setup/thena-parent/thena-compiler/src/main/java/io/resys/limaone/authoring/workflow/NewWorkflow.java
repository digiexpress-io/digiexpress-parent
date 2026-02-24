package io.resys.limaone.authoring.workflow;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.LocaleLabel;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewWorkflow {

  NewWorkflow props(NewWorkflowProps props);
  NewWorkflow props(Consumer<ImmutableNewWorkflowProps.Builder> props);
  
  Uni<Model<ArticleWorkflow>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewWorkflowProps.class) @JsonDeserialize(as = ImmutableNewWorkflowProps.class)
  interface NewWorkflowProps {
    String getValue();
    List<String> getArticles();
    List<LocaleLabel> getLabels();
    
    String getFormName();
    String getFormTag();
    String getFormId();
    
    String getFlowName();
    
    @Nullable String getId();
    @Nullable Boolean getDevMode();
    @Nullable Boolean getAnon();
    @Nullable Boolean getDisabled();
    @Nullable Boolean getAssignable();
    
    @Nullable LocalDateTime getStartDate();
    @Nullable LocalDateTime getEndDate();
  }
}