package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.authoring.ImmutableNewFlowTaskProps;
import io.resys.limaone.authoring.ImmutableNewFlowTaskProps.Builder;
import io.resys.limaone.authoring.NewFlowTask;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.ImmutableFlowTask;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class NewFlowTaskImpl implements NewFlowTask {
  private final WorldPersistence persistence;
  private final AST_Parser parser;
  private NewFlowTaskProps props;
  
  @Override
  public NewFlowTask props(NewFlowTaskProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewFlowTask props(Consumer<Builder> props) {
    final var builder = ImmutableNewFlowTaskProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<FlowTask>> build() {
    return persistence.worldBuilder()
      .docs(BodyType.FLOW_TASK)
      .lock().build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body);
      });
  }
  
  private FlowTask internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");
    Objects.requireNonNull(props, () -> "props must be defined");

    final var syntax = """
// custom ref example - @ServiceRef(value="newService", type=BodyType.DT)
public class {name} {

  public Output execute(Input input) {
    Output output = new Output();
    output.sum = input.a + input.b;
    return output;
  }
  
  
  @ServiceData
  public static class Input implements Serializable {
    Integer a;
    Integer b;
  }
  
  @ServiceData
  public static class Output implements Serializable {
    Integer sum;
  }
}
"""
    .replace("{name}", Optional.ofNullable(props.getName()).orElse("MyFirstTask"));
    
    final var flow = parser.parseFlowTask().syntax(syntax).parse();
    return ImmutableFlowTask.builder()
        .taskName(flow.getName())
        .taskValue(syntax)
        .build();
  }
}