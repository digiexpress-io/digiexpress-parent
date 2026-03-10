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
        // Sanity check if used in articles, links, workflows, pages
        // TODO: Add validation logic
        yield model.getBody();
      }
      case ARTICLE -> {
        final var model = world.getArticles().get(props.getId());
        if(model == null) {
          throw new AuthoringException(props, "Article with id: '" + props.getId() + "' not found!");
        }
        // Sanity check if used in links, workflows, parent/children relationships
        // TODO: Add validation logic
        yield model.getBody();
      }
      case ARTICLE_LINK -> {
        final var model = world.getArticleLinks().get(props.getId());
        if(model == null) {
          throw new AuthoringException(props, "Article link with id: '" + props.getId() + "' not found!");
        }
        // Sanity check for articles if used
        // TODO: Add validation logic
        yield model.getBody();
      }
      case ARTICLE_WORKFLOW -> {
        final var model = world.getArticleWorkflows().get(props.getId());
        if(model == null) {
          throw new AuthoringException(props, "Article workflow with id: '" + props.getId() + "' not found!");
        }
        // Sanity check for articles if used
        // TODO: Add validation logic
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
}