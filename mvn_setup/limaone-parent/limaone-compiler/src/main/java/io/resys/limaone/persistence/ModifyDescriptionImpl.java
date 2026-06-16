package io.resys.limaone.persistence;

import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableModifyDescriptionProps;
import io.resys.limaone.authoring.ModifyDescription;
import io.resys.limaone.model.ImmutableDescription;
import io.resys.limaone.model.Model;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class ModifyDescriptionImpl extends AuthoringTemplate<ModifyDescriptionImpl, Model<?>> implements ModifyDescription {

  private ModifyDescriptionProps props;

  public ModifyDescriptionImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public ModifyDescriptionImpl props(ModifyDescriptionProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyDescriptionImpl props(Consumer<ImmutableModifyDescriptionProps.Builder> props) {
    final var builder = ImmutableModifyDescriptionProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<?>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docsId(props.getId())
      .build(nextWorld -> {
        
        return nextWorld.mergeModel(props.getId(), ImmutableDescription.builder().text(props.getText()).build());
      });
  }
}
