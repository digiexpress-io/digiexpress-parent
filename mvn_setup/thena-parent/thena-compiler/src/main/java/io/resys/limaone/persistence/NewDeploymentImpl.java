package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableNewDeploymentProps;
import io.resys.limaone.authoring.ImmutableNewDeploymentProps.Builder;
import io.resys.limaone.authoring.NewDeployment;
import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.Deployment.BundleStatus;
import io.resys.limaone.model.ImmutableDeployment;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class NewDeploymentImpl extends AuthoringTemplate<NewDeploymentImpl, Model<Deployment>> implements NewDeployment {

  private NewDeploymentProps props;
  
  public NewDeploymentImpl(AuthoringConfig config) {
    super(config);
  }
  @Override
  public NewDeployment props(NewDeploymentProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewDeployment props(Consumer<Builder> props) {
    final var builder = ImmutableNewDeploymentProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Deployment>> build() {
    return config.getPersistence().worldBuilder()
      .docs(BodyType.DEPLOYMENT)
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body.getName(), body);
      });
  }
  
  private Deployment internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var now = getCreatedAt();
    final var desc = Optional.ofNullable(props.getDescription()).orElse("") + System.lineSeparator() + releaseNotes();
    
    return ImmutableDeployment.builder()
      
      .fromCommitId(world.getCommitId())
      .fromRefId(world.getRefId())
      
      .name(Optional.ofNullable(props.getName()).orElse("release - " + now))
      .externalId(null) // nullable
      .cockpitId(null) // nullable
      .createdBy(getAuthor())
      .createdAt(now)
      .startsAt(Optional.ofNullable(props.getLiveDate()).orElse(now))
      .description(desc)
      .errors(null) // nullable
      .status(BundleStatus.UNKNOWN) // bundle status
      .external(false)
      .sources(null) // always transient
      .build();
  }
  
  private String releaseNotes() {
    return "release notes - not impl!";
  }
}