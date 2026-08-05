package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableModifyPropertyObjectProps;
import io.resys.limaone.authoring.ImmutableModifyPropertyObjectProps.Builder;
import io.resys.limaone.authoring.ModifyPropertyObject;
import io.resys.limaone.model.ImmutablePropertyObject;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.PropertyObject;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.ModelWorldDb.NextWorld;
import io.smallrye.mutiny.Uni;


public class ModifyPropertyObjectImpl extends AuthoringTemplate<ModifyPropertyObjectImpl, Model<PropertyObject>> implements ModifyPropertyObject {

  private ModifyPropertyObjectProps props;

  public ModifyPropertyObjectImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public ModifyPropertyObject props(ModifyPropertyObjectProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyPropertyObject props(Consumer<Builder> props) {
    final var builder = ImmutableModifyPropertyObjectProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<PropertyObject>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.PROPERTY_OBJECT)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.mergeModel(props.getPropertyObjectId(), body.getObjectType() + "_" + body.getName(), body);
      });
  }

  private PropertyObject internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();

    final var start = world.getPropertyObjects().get(props.getPropertyObjectId());
    if(start == null) {
      throw new AuthoringException(props, "Can't find property object: '" + props.getPropertyObjectId() + "' to update!");
    }

    return ImmutablePropertyObject.builder()
        .from(start.getBody())
        .content(props.getContent() == null ? start.getBody().getContent() : props.getContent())
        .name(start.getBody().getName())
        .objectType(start.getBody().getObjectType())
        .build();

  }
}
