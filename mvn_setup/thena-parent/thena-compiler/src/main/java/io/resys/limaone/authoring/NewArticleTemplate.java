package io.resys.limaone.authoring;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.ArticleTemplate;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewArticleTemplate {

  NewArticleTemplate props(NewArticleTemplateProps props);
  NewArticleTemplate props(Consumer<ImmutableNewArticleTemplateProps.Builder> props);
  
  Uni<Model<ArticleTemplate>> build();
  Model<ArticleTemplate> buildSync();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewArticleTemplateProps.class) @JsonDeserialize(as = ImmutableNewArticleTemplateProps.class)
  interface NewArticleTemplateProps extends AuthoringModelProps {
    String getName();
    String getDescription();
    String getContent();
    String getType();
    
    @Nullable String getId();
  }
}