package io.resys.limaone.persistence.fs;

import io.resys.limaone.fs.ImmutableArticleProps;
import io.resys.limaone.fs.WorldFsProps.ArticleProps;
import io.resys.limaone.model.Article;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class Props_ArticleBuilder {
  private final WorldFsState currentState;
  private final NodeAndBody node;
  
  
  public ArticleProps build() {
    final Article article = currentState.getBodyOfType(node);
    return ImmutableArticleProps.builder()
        .type(node.getBodyType())
        .orderNumber(article.getOrder())
        .build();
  }
  
  public static ArticleProps of(WorldFsState currentState, NodeAndBody node) {
    return new Props_ArticleBuilder(currentState, node).build();
  }
}
