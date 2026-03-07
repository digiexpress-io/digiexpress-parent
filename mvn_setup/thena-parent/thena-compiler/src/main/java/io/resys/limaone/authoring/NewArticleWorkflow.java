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

public interface NewArticleWorkflow {

  NewArticleWorkflow props(NewArticleWorkflowProps props);
  NewArticleWorkflow props(Consumer<ImmutableNewArticleWorkflowProps.Builder> props);
  
  Uni<Model<ArticleWorkflow>> build();
  Model<ArticleWorkflow> buildSync();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewArticleWorkflowProps.class) @JsonDeserialize(as = ImmutableNewArticleWorkflowProps.class)
  interface NewArticleWorkflowProps extends AuthoringModelProps {
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
    
    @Nullable OffsetDateTime getStartDate();
    @Nullable OffsetDateTime getEndDate();
  }
}