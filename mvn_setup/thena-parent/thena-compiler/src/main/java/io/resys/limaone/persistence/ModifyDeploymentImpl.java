package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableModifyDeploymentProps;
import io.resys.limaone.authoring.ImmutableModifyDeploymentProps.Builder;
import io.resys.limaone.authoring.ModifyDeployment;
import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.ImmutableDeployment;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.WorldPersistence.NextWorld;
import io.smallrye.mutiny.Uni;


public class ModifyDeploymentImpl extends AuthoringTemplate<ModifyDeploymentImpl, Model<Deployment>> implements ModifyDeployment {

  private ModifyDeploymentProps props;

  public ModifyDeploymentImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public ModifyDeployment props(ModifyDeploymentProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyDeployment props(Consumer<Builder> props) {
    final var builder = ImmutableModifyDeploymentProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Deployment>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.DEPLOYMENT)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.mergeModel(props.getId(), body.getName(), body);
      });
  }
  
  private Deployment internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();
    
    final var start = world.getDeployments().get(props.getId());
    if(start == null) {
      throw new AuthoringException(props, "Deployment with id: '" + props.getId() + "' not found!");
    }

    return ImmutableDeployment.builder()
      .from(start.getBody())
      .status(props.getStatus())
      .name(props.getName() == null ? start.getBody().getName() : props.getName())
      .description(props.getDescription() == null ? start.getBody().getDescription() : props.getDescription())
      .startsAt(props.getStartsAt() == null ? start.getBody().getStartsAt() : props.getStartsAt())
      .build();
  }
}