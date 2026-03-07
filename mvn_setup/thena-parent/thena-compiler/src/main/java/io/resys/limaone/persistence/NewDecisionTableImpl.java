package io.resys.limaone.persistence;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.authoring.ImmutableNewDecisionTableProps;
import io.resys.limaone.authoring.ImmutableNewDecisionTableProps.Builder;
import io.resys.limaone.authoring.NewDecisionTable;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.ImmutableDecisionTable;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class NewDecisionTableImpl implements NewDecisionTable {
  private final WorldPersistence persistence;
  private final AST_Parser parser;
  private NewDecisionTableProps props;
  
  @Override
  public NewDecisionTable props(NewDecisionTableProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewDecisionTable props(Consumer<Builder> props) {
    final var builder = ImmutableNewDecisionTableProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<DecisionTable>> build() {
    return persistence.worldBuilder()
      .docs(BodyType.DECISION_TABLE)
      .lock().build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body);
      });
  }
  
  private DecisionTable internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var syntax = """
name: {name}
description: {desc}
hitPolicy: FIRST
valueSets:
  riskLevel: low, medium, high
table: |
  | age:INTEGER    | income: INTEGER | -> | riskLevel:STRING   |
  |----------------|-----------------|----|--------------------|
  | < 25           | < 30000         |    | high               |
  | >= 25          | >= 50000        |    | low                |
"""
    .replace("{name}", Optional.ofNullable(props.getName()).orElse("first_dt"))
    .replace("{desc}", Optional.ofNullable(props.getDesc()).orElse("my dt"));
    
    final var decision = parser.parseDecisionTable().syntax(syntax).parse();
    return ImmutableDecisionTable.builder()
        .name(decision.getName())
        .build();
  }
}