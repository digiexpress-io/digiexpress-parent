package io.resys.limaone.authoring;

import java.util.List;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.LocaleLabel;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewArticleLink {

  NewArticleLink props(NewArticleLinkProps props);
  NewArticleLink props(Consumer<ImmutableNewArticleLinkProps.Builder> props);
  
  Uni<Model<ArticleLink>> build();
  Model<ArticleLink> buildSync();
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewArticleLinkProps.class) @JsonDeserialize(as = ImmutableNewArticleLinkProps.class)
  interface NewArticleLinkProps extends AuthoringModelProps {
    String getValue(); 
    String getType();
    List<String> getArticles();
    List<LocaleLabel> getLabels();
    
    @Nullable String getId();
    @Nullable Boolean getDevMode();
  }
}