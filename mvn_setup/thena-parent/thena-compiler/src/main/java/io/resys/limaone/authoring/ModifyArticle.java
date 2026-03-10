package io.resys.limaone.authoring;

import java.util.List;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.Article;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface ModifyArticle {
  
  ModifyArticle props(ModifyArticleProps props);
  ModifyArticle props(Consumer<ImmutableModifyArticleProps.Builder> props);
  
  Uni<Model<Article>> build();
  Model<Article> buildSync();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableModifyArticleProps.class) @JsonDeserialize(as = ImmutableModifyArticleProps.class)
  interface ModifyArticleProps extends AuthoringModelProps {
    String getArticleId();
    String getName();
    Integer getOrder();
    
    @Nullable String getParentId();
    
    @Nullable List<String> getLinks();
    @Nullable List<String> getWorkflows();
    @Nullable Boolean getDevMode();
    @Nullable Boolean getAuthOnly();
  }
}
