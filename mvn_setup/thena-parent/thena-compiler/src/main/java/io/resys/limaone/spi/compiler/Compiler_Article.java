package io.resys.limaone.spi.compiler;

import java.util.function.Function;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.program.ArticleProgram;
import io.resys.limaone.spi.program.article.ImmutableLinkData;
import io.resys.limaone.spi.program.article.ImmutableTopicData;
import io.resys.limaone.spi.program.article.LinkData;
import io.resys.limaone.spi.program.article.SiteVisitor;
import io.resys.limaone.spi.program.article.SiteVisitorDefault;
import io.resys.limaone.spi.program.article.SitesBuilder;
import io.resys.limaone.spi.program.article.TopicData;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Article implements CompilableUnit {

  private final AST_Parser parser;
  private final ModelWorld world;

  @Override
  public OpenProgram compile(NewArtifact resolution) {
    final var articleAST = parser.parseArticles().world(world).parse();
    
    
    
    // ArticleProgram
    final SiteVisitor visitor = new SiteVisitorDefault();
    
    
    articleAST.getValues()
      .forEach(value -> 
        topic(builder -> builder
          .auth(value.getAuth())
          .path(value.getPath())
          .locale(value.getLocale())
          .headings(value.getHeadings())
          .images(value.getImages())
          .value(value.getValue())
          .build()
        )
      );
    
    
    // 1. target date when running, 2. AUTH when running
    /**
     *         boolean requiredAuth = Boolean.TRUE.equals(topic.getAuth());
        boolean isUserAuthenticated = this.auth;
        if(requiredAuth) {
          return isUserAuthenticated;  
        }
     */
    
    return null;
  }

  
  
  private SitesBuilder topic(
      Function<ImmutableTopicData.Builder, TopicData> newTopic) {
    visitor.visitTopicData(newTopic.apply(ImmutableTopicData.builder()));
    return this;
  }
  private SitesBuilder link(
      Function<ImmutableLinkData.Builder, LinkData> newLink) {
    visitor.visitLinkData(newLink.apply(ImmutableLinkData.builder()));
    return this;
  }
  
}
