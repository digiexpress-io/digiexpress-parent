package io.resys.limaone.authoring;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;

public interface DeleteArticleWorkflow {

  DeleteArticleWorkflow props(DeleteArticleWorkflowProps props);
  DeleteArticleWorkflow props(Consumer<ImmutableDeleteArticleWorkflowProps.Builder> props); 
  Uni<Model<ArticleWorkflow>> build();
  Model<ArticleWorkflow> buildSync();
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableDeleteArticleWorkflowProps.class)
  @JsonDeserialize(as = ImmutableDeleteArticleWorkflowProps.class)
  interface DeleteArticleWorkflowProps extends AuthoringModelProps {
    String getWorkflowId();
    String getArticleId();
  }
  
}