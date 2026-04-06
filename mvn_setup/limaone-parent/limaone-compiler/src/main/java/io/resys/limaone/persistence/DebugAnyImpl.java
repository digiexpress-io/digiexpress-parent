package io.resys.limaone.persistence;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.resys.limaone.authoring.DebugAny;
import io.resys.limaone.authoring.ImmutableDebugAnyProps;
import io.resys.limaone.authoring.ImmutableDebugAnyProps.Builder;
import io.resys.limaone.authoring.ImmutableDebugResult;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Parameter.Direction;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.program.DecisionProgram;
import io.resys.limaone.program.DecisionProgram.DecisionResult;
import io.resys.limaone.program.FlowProgram;
import io.resys.limaone.program.FlowProgram.FlowResult;
import io.resys.limaone.program.FlowTaskProgram;
import io.resys.limaone.program.FlowTaskProgram.FlowTaskResult;
import io.resys.limaone.program.Program;
import io.resys.limaone.program.Program.ProgramResult;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.spi.ast.CsvParserImpl;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.limaone.spi.program.input.DefaultProgramInput;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class DebugAnyImpl implements DebugAny {
  private final AuthoringConfig config;
  private DebugAnyProps props;
  
  @Override
  public DebugAny props(DebugAnyProps props) {
    this.props = Objects.requireNonNull(props, () -> "props must be defined");
    return this;
  }

  @Override
  public DebugAny props(Consumer<Builder> props) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final var builder = ImmutableDebugAnyProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @SuppressWarnings("unchecked")
  @Override
  public Uni<DebugResult> build() {
    Objects.requireNonNull(props, () -> "props must be defined");
    assertIsTrue(props.getInput() != null || props.getInputCSV() != null, () -> "input or inputCSV must be defined!");
    assertIsTrue(props.getInput() == null || props.getInputCSV() == null, () -> "input and inputCSV can't be both defined!");

    return config.getPersistence().worldQuery()
        .docs(BodyType.FLOW, BodyType.FLOW_TASK, BodyType.DECISION_TABLE)
        .findAll()
        .onItem()
        .transform(nextWorld -> {

          final var runtime = new CompilerImpl(config.getEnvir()).compile(nextWorld).build();
          final var bundle = runtime.getBundle();
          final var program = bundle.findAnyProgram(props.getId()).orElse(null);
          
          assertIsTrue(program != null, () -> "Entity was not found by id: '" + props.getId() + "'!");
          assertIsTrue(program.getStatus() == ProgramStatus.UP, () -> "Program status: '" + program.getStatus() + "' is not runnable!");

          if (props.getInputCSV() != null) {
            final var types = program.getHeaders().stream().filter(p -> p.getDirection() == Direction.IN).toList();
            final var rows = new CsvParserImpl().castTo(types).csv(props.getInputCSV()).parse();
            final var csv = rows.forEach(input -> {
              final ProgramResult result = run(input, program);
              return toMap(program, result);
            });
            
            return ImmutableDebugResult.builder().id(props.getId()).bodyCsv(csv + props.getInputCSV()).build();
          }

          final Map<String, Serializable> input = props.getInput() == null ? Collections.emptyMap() : new JsonObject(props.getInput()).mapTo(Map.class);
          final ProgramResult result = run(input, program);
          return ImmutableDebugResult.builder().id(props.getId()).body(result).build();
        });
  }

  @Override
  public DebugResult buildSync() {
    return build()
        .runSubscriptionOn(config.getEnvir().getWorkerPool())
        .await().atMost(config.getEnvir().getWorkerPoolMaxTimeout());
  }
  
  @SuppressWarnings("unchecked")
  private List<Map<String, Serializable>> toMap(Program program, ProgramResult result) {
    
    switch (program.getType()) {
      case FLOW: {
        final var wrapped = (FlowResult) result;
        return wrapped.isReturnsCollection() ? (List<Map<String, Serializable>>) wrapped.getReturns().values().iterator().next() : Arrays.asList(wrapped.getReturns());
      }
      case FLOW_TASK: {
        final var wrapped = (FlowTaskResult) result;
        return Arrays.asList(JsonObject.mapFrom(wrapped).mapTo(Map.class));
      }
      case DECISION_TABLE: {
        final var wrapped = (DecisionResult) result;
        return wrapped.getMatches().stream().map(match -> match.getReturnsMap()).toList();
      }
      default: throw new DebugException("Can't debug: '" + program.getType() + "'!");
    }
  }
  
  private ProgramResult run(Map<String, Serializable> input, Program program) {
    
    switch (program.getType()) {
      case FLOW: {
        final var programInput = DefaultProgramInput.of(input);
        return ((FlowProgram) program).run(programInput).andGetBody();
      }
      case FLOW_TASK: {
        final var programInput = DefaultProgramInput.of(input);
        return ((FlowTaskProgram) program).run(programInput).andGetBody();
      }
      case DECISION_TABLE: {
        final var programInput = DefaultProgramInput.of(input);
        return ((DecisionProgram) program).run(programInput).andGetBody();
      }
      default: throw new DebugException("Can't debug: '" + program.getType() + "'!");
    }
  }

  public static void assertIsTrue(boolean expression, Supplier<String> message) {
    if (!expression) {
      throw new DebugException(message.get());
    }
  }
  
  public static class DebugException extends RuntimeException {
    private static final long serialVersionUID = -1398646745215966745L;

    public DebugException(String message) {
      super(message);
    }
  }
}
