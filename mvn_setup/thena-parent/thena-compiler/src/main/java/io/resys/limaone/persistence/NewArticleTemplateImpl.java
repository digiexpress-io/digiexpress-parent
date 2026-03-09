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
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;



public class NewArticleTemplateImpl extends AuthoringTemplate<NewArticleTemplateImpl, Model<ArticleTemplate>> implements NewArticleTemplate {

  private NewArticleTemplateProps props;

  public NewArticleTemplateImpl(AuthoringConfig config) {
    super(config);
  }

  
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
    return config.getPersistence().worldBuilder()
      .docs(BodyType.ARTICLE_TEMPLATE)
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body.getName(), body);
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