package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableModifyFlowTaskProps;
import io.resys.limaone.authoring.ImmutableModifyFlowTaskProps.Builder;
import io.resys.limaone.authoring.ModifyFlowTask;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.ImmutableFlowTask;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.WorldPersistence.NextWorld;
import io.smallrye.mutiny.Uni;


public class ModifyFlowTaskImpl extends AuthoringTemplate<ModifyFlowTaskImpl, Model<FlowTask>> implements ModifyFlowTask {

  private ModifyFlowTaskProps props;

  public ModifyFlowTaskImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public ModifyFlowTask props(ModifyFlowTaskProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyFlowTask props(Consumer<Builder> props) {
    final var builder = ImmutableModifyFlowTaskProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<FlowTask>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.FLOW_TASK)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.mergeModel(props.getFlowTaskId(), body.getTaskName(), body);
      });
  }
  
  private FlowTask internalBuild(NextWorld nextWorld) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final ModelWorld world = nextWorld.getCurrentWorld();
    
    final var start = world.getFlowTasks().get(props.getFlowTaskId());
    if(start == null) {
      throw new AuthoringException(props, "Flow task with id: '" + props.getFlowTaskId() + "' not found!");
    }
    
    final var flow = config.getAstParser().parseFlowTask().syntax(props.getTaskValue()).parse();
    
    // Check for duplicate name only if the name is actually being changed
    if(!start.getBody().getTaskName().equals(flow.getName())) {
      final var duplicate = world.getFlowTasks().values().stream()
          .filter(p -> !p.getId().equals(props.getFlowTaskId()))
          .filter(p -> p.getBody().getTaskName().equalsIgnoreCase(flow.getName()))
          .findFirst();

      if(duplicate.isPresent()) {
        throw new AuthoringException(props, "Flow task with name: '" + flow.getName() + "' already exists!");
      }
    }

    return ImmutableFlowTask.builder()
      .from(start.getBody())
      .taskName(flow.getName())
      .taskValue(props.getTaskValue())
      .build();
  }
}