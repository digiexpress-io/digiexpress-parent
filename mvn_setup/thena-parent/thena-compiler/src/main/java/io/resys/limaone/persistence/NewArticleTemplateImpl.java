package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableNewArticleTemplateProps;
import io.resys.limaone.authoring.ImmutableNewArticleTemplateProps.Builder;
import io.resys.limaone.authoring.NewArticleTemplate;
import io.resys.limaone.model.ArticleTemplate;
import io.resys.limaone.model.ImmutableArticleTemplate;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class NewArticleTemplateImpl implements NewArticleTemplate {
  private final WorldPersistence persistence;
  private NewArticleTemplateProps props;
  
  @Override
  public NewArticleTemplate props(NewArticleTemplateProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewArticleTemplate props(Consumer<Builder> props) {
    final var builder = ImmutableNewArticleTemplateProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<ArticleTemplate>> build() {
    return persistence.worldBuilder()
      .docs(BodyType.ARTICLE_TEMPLATE)
      .lock().build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body);
      });
  }
  
  private ArticleTemplate internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var template = ImmutableArticleTemplate.builder()
        .name(props.getName())
        .description(props.getDescription())
        .type(props.getType())
        .content(props.getContent());
    
    final var duplicate = world.getArticleTemplates().values().stream()
        .filter(p -> p.getBody().getName().equals(props.getName()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      final var msg = "Template: '" + props.getName() + "' already exists!";
      throw new AuthoringException(props, msg);
    }
    
    return template.build();
  }
}