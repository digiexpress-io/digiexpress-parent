package io.resys.limaone.authoring.template;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.ArticleTemplate;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewTemplate {

  NewTemplate props(NewTemplateProps props);
  NewTemplate props(Consumer<ImmutableNewTemplateProps.Builder> props);
  
  Uni<Model<ArticleTemplate>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewTemplateProps.class) @JsonDeserialize(as = ImmutableNewTemplateProps.class)
  interface NewTemplateProps {
    String getName();
    String getDescription();
    String getContent();
    String getType();
    
    @Nullable String getId();
  }
}