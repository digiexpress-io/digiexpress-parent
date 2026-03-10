package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.DeleteAny;
import io.resys.limaone.authoring.ImmutableDeleteAnyProps;
import io.resys.limaone.authoring.ImmutableDeleteAnyProps.Builder;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.WorldPersistence.NextWorld;
import io.smallrye.mutiny.Uni;


public class DeleteAnyImpl extends AuthoringTemplate<DeleteAnyImpl, Model<?>> implements DeleteAny {

  private DeleteAnyProps props;

  public DeleteAnyImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public DeleteAny props(DeleteAnyProps props) {
    this.props = props;
    return this;
  }

  @Override
  public DeleteAny props(Consumer<Builder> props) {
    final var builder = ImmutableDeleteAnyProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<?>> build() {
    final BodyType[] docsToLoad = switch (props.getBodyType()) {
      case LOCALE -> new BodyType[] { BodyType.LOCALE, BodyType.ARTICLE, BodyType.ARTICLE_LINK, BodyType.ARTICLE_WORKFLOW, BodyType.ARTICLE_PAGE };
      case ARTICLE -> new BodyType[] { BodyType.ARTICLE, BodyType.ARTICLE_LINK, BodyType.ARTICLE_WORKFLOW };
      case ARTICLE_LINK -> new BodyType[] { BodyType.ARTICLE_LINK, BodyType.ARTICLE };
      case ARTICLE_WORKFLOW -> new BodyType[] { BodyType.ARTICLE_WORKFLOW, BodyType.ARTICLE };
      case ARTICLE_PAGE -> new BodyType[] { BodyType.ARTICLE_PAGE };
      case ARTICLE_TEMPLATE -> new BodyType[] { BodyType.ARTICLE_TEMPLATE };
      case FLOW -> new BodyType[] { BodyType.FLOW };
      case FLOW_TASK -> new BodyType[] { BodyType.FLOW_TASK };
      case DECISION_TABLE -> new BodyType[] { BodyType.DECISION_TABLE };
      case DEPLOYMENT -> new BodyType[] { BodyType.DEPLOYMENT };
      default -> new BodyType[] { };
    };
    
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(docsToLoad)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.deleteModel(props.getId(), body);
      });
  }
  
  private Model.Body internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();
    
    return switch (props.getBodyType()) {
      case LOCALE -> {
        final var model = world.getLocales().get(props.getId());
        if(model == null) {
          throw new AuthoringException(props, "Locale with id: '" + props.getId() + "' not found!");
        }
        sanitizeLocale(world, props.getId());
        yield model.getBody();
      }
      case ARTICLE -> {
        final var model = world.getArticles().get(props.getId());
        if(model == null) {
          throw new AuthoringException(props, "Article with id: '" + props.getId() + "' not found!");
        }
        sanitizeArticle(world, props.getId());
        yield model.getBody();
      }
      case ARTICLE_LINK -> {
        final var model = world.getArticleLinks().get(props.getId());
        if(model == null) {
          throw new AuthoringException(props, "Article link with id: '" + props.getId() + "' not found!");
        }
        yield model.getBody();
      }
      case ARTICLE_WORKFLOW -> {
        final var model = world.getArticleWorkflows().get(props.getId());
        if(model == null) {
          throw new AuthoringException(props, "Article workflow with id: '" + props.getId() + "' not found!");
        }
        yield model.getBody();
      }
      case ARTICLE_PAGE -> {
        final var model = world.getArticlePages().get(props.getId());
        if(model == null) {
          throw new AuthoringException(props, "Article page with id: '" + props.getId() + "' not found!");
        }
        yield model.getBody();
      }
      case ARTICLE_TEMPLATE -> {
        final var model = world.getArticleTemplates().get(props.getId());
        if(model == null) {
          throw new AuthoringException(props, "Article template with id: '" + props.getId() + "' not found!");
        }
        yield model.getBody();
      }
      case FLOW -> {
        final var model = world.getFlows().get(props.getId());
        if(model == null) {
          throw new AuthoringException(props, "Flow with id: '" + props.getId() + "' not found!");
        }
        yield model.getBody();
      }
      case FLOW_TASK -> {
        final var model = world.getFlowTasks().get(props.getId());
        if(model == null) {
          throw new AuthoringException(props, "Flow task with id: '" + props.getId() + "' not found!");
        }
        yield model.getBody();
      }
      case DECISION_TABLE -> {
        final var model = world.getDecisionTables().get(props.getId());
        if(model == null) {
          throw new AuthoringException(props, "Decision table with id: '" + props.getId() + "' not found!");
        }
        yield model.getBody();
      }
      case DEPLOYMENT -> {
        final var model = world.getDeployments().get(props.getId());
        if(model == null) {
          throw new AuthoringException(props, "Deployment with id: '" + props.getId() + "' not found!");
        }
        yield model.getBody();
      }
      default -> throw new AuthoringException(props, "Unexpected value: " + props.getBodyType());
    };
  }
  
  private void sanitizeLocale(ModelWorld world, String localeId) {
    // Check if locale is used in article pages
    final var usedInPages = world.getArticlePages().values().stream()
        .filter(page -> page.getBody().getLocale().equals(localeId))
        .findFirst();
    if(usedInPages.isPresent()) {
      throw new AuthoringException(props, "Locale '" + localeId + "' is used in article page: '" + usedInPages.get().getId() + "'!");
    }
    
    // Check if locale is used in article workflow labels
    final var usedInWorkflows = world.getArticleWorkflows().values().stream()
        .filter(workflow -> workflow.getBody().getLabels().stream()
            .anyMatch(label -> label.getLocale().equals(localeId)))
        .findFirst();
    if(usedInWorkflows.isPresent()) {
      throw new AuthoringException(props, "Locale '" + localeId + "' is used in article workflow: '" + usedInWorkflows.get().getId() + "'!");
    }
    
    // Check if locale is used in article link labels  
    final var usedInLinks = world.getArticleLinks().values().stream()
        .filter(link -> link.getBody().getLabels().stream()
            .anyMatch(label -> label.getLocale().equals(localeId)))
        .findFirst();
    if(usedInLinks.isPresent()) {
      throw new AuthoringException(props, "Locale '" + localeId + "' is used in article link: '" + usedInLinks.get().getId() + "'!");
    }
  }
  
  private void sanitizeArticle(ModelWorld world, String articleId) {
    // Check if article is used in article links
    final var usedInLinks = world.getArticleLinks().values().stream()
        .filter(link -> link.getBody().getArticles().contains(articleId))
        .findFirst();
    if(usedInLinks.isPresent()) {
      throw new AuthoringException(props, "Article '" + articleId + "' is used in article link: '" + usedInLinks.get().getId() + "'!");
    }
    
    // Check if article is used in article workflows
    final var usedInWorkflows = world.getArticleWorkflows().values().stream()
        .filter(workflow -> workflow.getBody().getArticles().contains(articleId))
        .findFirst();
    if(usedInWorkflows.isPresent()) {
      throw new AuthoringException(props, "Article '" + articleId + "' is used in article workflow: '" + usedInWorkflows.get().getId() + "'!");
    }
    
    // Check for parent/children relationships
    final var usedAsParent = world.getArticles().values().stream()
        .filter(article -> article.getBody().getParentId() != null && article.getBody().getParentId().equals(articleId))
        .findFirst();
    if(usedAsParent.isPresent()) {
      throw new AuthoringException(props, "Article '" + articleId + "' is used as parent in article: '" + usedAsParent.get().getId() + "'!");
    }
  }

}