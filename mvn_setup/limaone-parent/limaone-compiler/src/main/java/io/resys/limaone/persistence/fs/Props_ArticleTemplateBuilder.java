package io.resys.limaone.persistence.fs;

import java.util.Collections;

import io.resys.limaone.fs.ImmutableArticleTemplateProps;
import io.resys.limaone.fs.WorldFsProps;
import io.resys.limaone.fs.WorldFsProps.ArticleTemplateProps;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Props_ArticleTemplateBuilder {
  
  @SuppressWarnings("unused")
  private final WorldFsState currentState;
  private final NodeAndBody node;
  
  public ArticleTemplateProps build() {


    return ImmutableArticleTemplateProps.builder()
        .id(node.getObjectId())
        .type(node.getBodyType())
        .locked(false)
        .assetDescription(node.getDescription().map(e -> e.getText()).orElse(null))
        .labels(node.getLabels().map(e -> e.getValues()).orElse(Collections.emptyList()))
        .build();
  }
  
  public static WorldFsProps of(WorldFsState curreState, NodeAndBody node) {
    return new Props_ArticleTemplateBuilder(curreState, node).build();
  }

}
