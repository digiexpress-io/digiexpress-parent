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

public interface NewArticlePage {

  NewArticlePage props(NewArticlePageProps props);
  NewArticlePage props(Consumer<ImmutableNewArticlePageProps.Builder> props);
  
  Uni<Model<ArticlePage>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewArticlePageProps.class) @JsonDeserialize(as = ImmutableNewArticlePageProps.class)
  interface NewArticlePageProps extends AuthoringModelProps {
    String getArticleId();
    String getLocale();
    
    @Nullable String getId();
    @Nullable String getContent();
    @Nullable Boolean getDevMode();
  }
}