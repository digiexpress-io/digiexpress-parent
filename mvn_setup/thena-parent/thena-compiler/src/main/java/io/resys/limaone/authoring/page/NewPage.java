package io.resys.limaone.authoring.page;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewPage {

  NewPage props(NewPageProps props);
  NewPage props(Consumer<ImmutableNewPageProps.Builder> props);
  
  Uni<Model<ArticlePage>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewPageProps.class) @JsonDeserialize(as = ImmutableNewPageProps.class)
  interface NewPageProps {
    String getArticleId();
    String getLocale();
    
    @Nullable String getId();
    @Nullable String getContent();
    @Nullable Boolean getDevMode();
  }
}