package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.authoring.ImmutableNewFlowProps;
import io.resys.limaone.authoring.ImmutableNewFlowProps.Builder;
import io.resys.limaone.authoring.NewFlow;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.ImmutableFlow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class NewFlowImpl implements NewFlow {
  private final WorldPersistence persistence;
  private final AST_Parser parser;
  private NewFlowProps props;
  
  @Override
  public NewFlow props(NewFlowProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewFlow props(Consumer<Builder> props) {
    final var builder = ImmutableNewFlowProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Flow>> build() {
    return persistence.worldBuilder()
      .docs(BodyType.FLOW)
      .lock().build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body);
      });
  }
  
  private Flow internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var id = OidUtils.gen();
    final var syntax = """
id: {id}
description: {desc}

inputs:
  id:
    required: true
    type: STRING
    debugValue: "1"
tasks:
  - First task:
    id: "first_task"
    then: end
"""
    .replace("{id}", Optional.ofNullable(props.getName()).orElse("first_flow"))
    .replace("{desc}", Optional.ofNullable(props.getDesc()).orElse("my first flow"));
    
    final var flow = parser.parseFlow().id(id).syntax(syntax).parse();
    return ImmutableFlow.builder()
        .flowName(flow.getName())
        .flowValue(syntax)
        .build();
  }
}