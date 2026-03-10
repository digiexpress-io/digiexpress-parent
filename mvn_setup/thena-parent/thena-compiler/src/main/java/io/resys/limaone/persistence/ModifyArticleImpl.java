package io.resys.limaone.persistence;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableModifyArticleProps;
import io.resys.limaone.authoring.ImmutableModifyArticleProps.Builder;
import io.resys.limaone.authoring.ModifyArticle;
import io.resys.limaone.model.Article;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.ImmutableArticle;
import io.resys.limaone.model.ImmutableArticleLink;
import io.resys.limaone.model.ImmutableArticleWorkflow;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.WorldPersistence.NextWorld;
import io.smallrye.mutiny.Uni;


public class ModifyArticleImpl extends AuthoringTemplate<ModifyArticleImpl, Model<Article>> implements ModifyArticle {

  private ModifyArticleProps props;

  public ModifyArticleImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public ModifyArticle props(ModifyArticleProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyArticle props(Consumer<Builder> props) {
    final var builder = ImmutableModifyArticleProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Article>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.ARTICLE, BodyType.ARTICLE_LINK, BodyType.ARTICLE_WORKFLOW)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.mergeModel(props.getArticleId(), body.getName(), body);
      });
  }
  
  private Article internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();
    

    final var start = world.getArticles().get(props.getArticleId());
    if(props.getArticleId().equals(props.getParentId())) {
      throw new AuthoringException(props, "Article: '" + props.getName() + "' parent can't be itself!");      
    }
    
    final var duplicate = world.getArticles().values().stream()
        .filter(p -> !p.getId().equals(props.getArticleId()))
        .filter(p -> p.getBody().getName().equals(props.getName()))
        .findFirst();

    if(duplicate.isPresent()) {
      throw new AuthoringException(props, "Article: '" + props.getName() + "' already exists!");
    }
    
    // update article links
    if(props.getLinks() != null) {
      for(final var link : world.getArticleLinks().values()) {
        
        final var isArticleInLink = link.getBody().getArticles().contains(props.getArticleId());
        final var isLinkInChanges = props.getLinks().contains(link.getId());
        
        // link already defined for article
        if(isArticleInLink &&  isLinkInChanges) {
          continue;
        }
        
        // add link
        if(isLinkInChanges && !isArticleInLink) {
          final var newLink = ImmutableModel.<ArticleLink>builder().from(link)
              .body(ImmutableArticleLink.builder().from(link.getBody())
                  .addArticles(props.getArticleId())
                  .build())
              .build(); 
          nextWorld.mergeModel(newLink.getId(), newLink.getBody().getValue(), newLink.getBody());
        }
        
        // remove link
        if(isArticleInLink && !isLinkInChanges) {
          final var articles = new ArrayList<>(link.getBody().getArticles());
          articles.remove(props.getArticleId());
          
          final var newLink = ImmutableModel.<ArticleLink>builder().from(link)
              .body(ImmutableArticleLink.builder().from(link.getBody())
                  .articles(articles)
                  .build())
              .build();
          
          nextWorld.mergeModel(newLink.getId(), newLink.getBody().getValue(), newLink.getBody());
        }
      }
    }
    
    // update article workflows
    if(props.getWorkflows() != null) {
      for(final var workflow : world.getArticleWorkflows().values()) {
        
        final var isArticleInWorkflow = workflow.getBody().getArticles().contains(props.getArticleId());
        final var isWorkflowInChanges = props.getWorkflows().contains(workflow.getId());
        
        // workflow already defined for article
        if(isArticleInWorkflow &&  isWorkflowInChanges) {
          continue;
        }
        
        // add workflow
        if(isWorkflowInChanges && !isArticleInWorkflow) {
          
          
          final var newWorkflow = ImmutableModel.<ArticleWorkflow>builder().from(workflow)
              .body(ImmutableArticleWorkflow.builder().from(workflow.getBody())
                  .addArticles(props.getArticleId())
                  .build())
              .build(); 
          nextWorld.mergeModel(newWorkflow.getId(), newWorkflow.getBody().getValue(), newWorkflow.getBody());
        }
        
        // remove link
        if(isArticleInWorkflow && !isWorkflowInChanges) {
          final var articles = new ArrayList<>(workflow.getBody().getArticles());
          articles.remove(props.getArticleId());
          
          final var newWorkflow = ImmutableModel.<ArticleWorkflow>builder().from(workflow)
              .body(ImmutableArticleWorkflow.builder().from(workflow.getBody())
                  .articles(articles)
                  .build())
              .build();
          nextWorld.mergeModel(newWorkflow.getId(), newWorkflow.getBody().getValue(), newWorkflow.getBody());
        }
      }
    }


    return ImmutableArticle.builder()
      .from(start.getBody())
      .authOnly(props.getAuthOnly())
      .devMode(props.getDevMode())
      .name(props.getName())
      .order(props.getOrder())
      .parentId(props.getParentId())
      .build();
    
  }
}