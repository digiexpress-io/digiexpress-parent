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

public interface ModifyArticleLink {
  
  ModifyArticleLink props(ModifyArticleLinkProps props);
  ModifyArticleLink props(Consumer<ImmutableModifyArticleLinkProps.Builder> props);
  
  Uni<Model<ArticleLink>> build();
  Model<ArticleLink> buildSync();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableModifyArticleLinkProps.class) @JsonDeserialize(as = ImmutableModifyArticleLinkProps.class)
  interface ModifyArticleLinkProps extends AuthoringModelProps {
    String getLinkId();
    String getValue();
    String getType();
    
    @Nullable List<LocaleLabel> getLabels();
    @Nullable List<String> getArticles();
    @Nullable Boolean getDevMode();
  }
}