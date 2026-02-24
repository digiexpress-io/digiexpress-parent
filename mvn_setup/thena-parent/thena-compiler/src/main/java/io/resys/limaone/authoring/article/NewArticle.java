package io.resys.limaone.authoring.article;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Article;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewArticle {

  NewArticle props(NewArticleProps props);
  NewArticle props(Consumer<ImmutableNewArticleProps.Builder> props);
  
  Uni<Model<Article>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewArticleProps.class) @JsonDeserialize(as = ImmutableNewArticleProps.class)
  interface NewArticleProps {
    String getName();
    
    @Nullable String getId();
    @Nullable String getParentId();
    @Nullable Integer getOrder();
    @Nullable Boolean getDevMode();
    @Nullable Boolean getAuthOnly();
  }
}
