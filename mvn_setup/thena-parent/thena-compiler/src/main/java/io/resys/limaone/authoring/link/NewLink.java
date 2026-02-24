package io.resys.limaone.authoring.link;

import java.util.List;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.LocaleLabel;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewLink {

  NewLink props(NewLinkProps props);
  NewLink props(Consumer<ImmutableNewLinkProps.Builder> props);
  
  Uni<Model<ArticleLink>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewLinkProps.class) @JsonDeserialize(as = ImmutableNewLinkProps.class)
  interface NewLinkProps {
    String getValue(); 
    String getType();
    List<String> getArticles();
    List<LocaleLabel> getLabels();
    
    @Nullable String getId();
    @Nullable Boolean getDevMode();
  }
}