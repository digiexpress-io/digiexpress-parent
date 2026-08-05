package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableNewPropertyObjectProps;
import io.resys.limaone.authoring.ImmutableNewPropertyObjectProps.Builder;
import io.resys.limaone.authoring.NewPropertyObject;
import io.resys.limaone.model.ImmutablePropertyObject;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.PropertyObject;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;



public class NewPropertyObjectImpl extends AuthoringTemplate<NewPropertyObjectImpl, Model<PropertyObject>> implements NewPropertyObject {

  private NewPropertyObjectProps props;

  public NewPropertyObjectImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public NewPropertyObject props(NewPropertyObjectProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewPropertyObject props(Consumer<Builder> props) {
    final var builder = ImmutableNewPropertyObjectProps.builder();
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
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body.getObjectType() + "_" + body.getName(), body);
      });
  }

  private PropertyObject internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var propertyObject = ImmutablePropertyObject.builder()
        .content(Optional.ofNullable(props.getContent()).orElse(""))
        .name(props.getName())
        .objectType(props.getObjectType());

    final var duplicate = world.getPropertyObjects().values().stream()
        .filter(p -> p.getBody().getObjectType().equals(props.getObjectType()))
        .filter(p -> p.getBody().getName().equals(props.getName()))
        .findFirst();

    if(duplicate.isPresent()) {
      throw new AuthoringException(props, "PropertyObject for objectType: '" + props.getObjectType() + "' and name: '" + props.getName() + "' already exists!");
    }

    return propertyObject.build();
  }
}
