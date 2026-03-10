package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableModifyFlowProps;
import io.resys.limaone.authoring.ImmutableModifyFlowProps.Builder;
import io.resys.limaone.authoring.ModifyFlow;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.ImmutableFlow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.WorldPersistence.NextWorld;
import io.smallrye.mutiny.Uni;


public class ModifyFlowImpl extends AuthoringTemplate<ModifyFlowImpl, Model<Flow>> implements ModifyFlow {

  private ModifyFlowProps props;

  public ModifyFlowImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public ModifyFlow props(ModifyFlowProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyFlow props(Consumer<Builder> props) {
    final var builder = ImmutableModifyFlowProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Flow>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.FLOW)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.mergeModel(props.getFlowId(), body.getFlowName(), body);
      });
  }
  
  private Flow internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();
    
    final var start = world.getFlows().get(props.getFlowId());
    if(start == null) {
      throw new AuthoringException(props, "Flow with id: '" + props.getFlowId() + "' not found!");
    }
    
    final var flow = config.getAstParser().parseFlow().syntax(props.getFlowValue()).parse();

    // Check for duplicate name only if the name is actually being changed
    if(!start.getBody().getFlowName().equals(flow.getName())) {
      final var duplicate = world.getFlows().values().stream()
          .filter(p -> !p.getId().equals(props.getFlowId()))
          .filter(p -> p.getBody().getFlowName().equalsIgnoreCase(flow.getName()))
          .findFirst();

      if(duplicate.isPresent()) {
        throw new AuthoringException(props, "Flow with name: '" + flow.getName() + "' already exists!");
      }
    }

    return ImmutableFlow.builder()
      .from(start.getBody())
      .flowName(flow.getName())
      .flowValue(props.getFlowValue())
      .build();
  }
}