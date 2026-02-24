package io.resys.limaone.authoring.articlelink;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;

public interface DeleteArticleLink {
  
  DeleteArticleLink props(DeleteArticleLinkProps props);
  DeleteArticleLink props(Consumer<ImmutableDeleteArticleLinkProps.Builder> props);
  
  Uni<Model<ArticleLink>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableDeleteArticleLinkProps.class) @JsonDeserialize(as = ImmutableDeleteArticleLinkProps.class)
  interface DeleteArticleLinkProps {
    String getLinkId(); 
    String getArticleId();
  }
}
