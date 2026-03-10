package io.resys.limaone.authoring;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.LocaleLabel;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface ModifyArticleWorkflow {
  
  ModifyArticleWorkflow props(ModifyArticleWorkflowProps props);
  ModifyArticleWorkflow props(Consumer<ImmutableModifyArticleWorkflowProps.Builder> props);
  
  Uni<Model<ArticleWorkflow>> build();
  Model<ArticleWorkflow> buildSync();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableModifyArticleWorkflowProps.class) @JsonDeserialize(as = ImmutableModifyArticleWorkflowProps.class)
  interface ModifyArticleWorkflowProps extends AuthoringModelProps {
    String getWorkflowId();
    String getValue();
    
    @Nullable String getFormId();
    @Nullable String getFormName();
    @Nullable String getFormTag();
    @Nullable String getFlowName();
    @Nullable List<LocaleLabel> getLabels();
    @Nullable List<String> getArticles();
    
    @Nullable Boolean getDisabled();
    @Nullable Boolean getDevMode();
    @Nullable Boolean getAnon();
    @Nullable Boolean getAssignable();
    
    @Nullable OffsetDateTime getStartDate();
    @Nullable OffsetDateTime getEndDate();
  }
}