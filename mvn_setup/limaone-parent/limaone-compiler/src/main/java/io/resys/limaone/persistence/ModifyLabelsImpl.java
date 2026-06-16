package io.resys.limaone.persistence;

import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableModifyLabelsProps;
import io.resys.limaone.authoring.ModifyLabels;
import io.resys.limaone.model.ImmutableDescriptionLabels;
import io.resys.limaone.model.Model;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class ModifyLabelsImpl extends AuthoringTemplate<ModifyLabelsImpl, Model<?>> implements ModifyLabels {

  private ModifyLabelsProps props;

  public ModifyLabelsImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public ModifyLabelsImpl props(ModifyLabelsProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyLabelsImpl props(Consumer<ImmutableModifyLabelsProps.Builder> props) {
    final var builder = ImmutableModifyLabelsProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<?>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docsId(props.getId())
      .build(nextWorld -> nextWorld.mergeModel(props.getId(), ImmutableDescriptionLabels.builder().values(props.getValues()).build()));
  }
}
