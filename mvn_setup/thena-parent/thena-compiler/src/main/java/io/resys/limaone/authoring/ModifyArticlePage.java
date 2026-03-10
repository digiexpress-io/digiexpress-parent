package io.resys.limaone.authoring;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface ModifyArticlePage {
  
  ModifyArticlePage props(ModifyArticlePageProps props);
  ModifyArticlePage props(Consumer<ImmutableModifyArticlePageProps.Builder> props);
  
  Uni<Model<ArticlePage>> build();
  Model<ArticlePage> buildSync();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableModifyArticlePageProps.class) @JsonDeserialize(as = ImmutableModifyArticlePageProps.class)
  interface ModifyArticlePageProps extends AuthoringModelProps {
    String getPageId();
    String getContent();
    String getLocale();
    
    @Nullable Boolean getDevMode();
  }
}