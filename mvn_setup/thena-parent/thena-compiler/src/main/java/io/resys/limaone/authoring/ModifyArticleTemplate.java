package io.resys.limaone.authoring;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.ArticleTemplate;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;

public interface ModifyArticleTemplate {
  
  ModifyArticleTemplate props(ModifyArticleTemplateProps props);
  ModifyArticleTemplate props(Consumer<ImmutableModifyArticleTemplateProps.Builder> props);
  
  Uni<Model<ArticleTemplate>> build();
  Model<ArticleTemplate> buildSync();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableModifyArticleTemplateProps.class) @JsonDeserialize(as = ImmutableModifyArticleTemplateProps.class)
  interface ModifyArticleTemplateProps extends AuthoringModelProps {
    String getTemplateId();
    String getName();
    String getDescription();
    String getContent();
    String getType();
  }
}