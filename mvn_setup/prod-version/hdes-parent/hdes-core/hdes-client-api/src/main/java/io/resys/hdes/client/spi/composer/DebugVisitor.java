package io.resys.hdes.client.spi.composer;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer.DebugRequest;
import io.resys.hdes.client.api.HdesComposer.DebugResponse;
import io.resys.hdes.client.api.HdesStore.StoreState;
import io.resys.hdes.client.api.ImmutableDebugResponse;
import io.resys.hdes.client.api.exceptions.HdesBadRequestException;
import io.resys.hdes.client.api.programs.Program.ProgramResult;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramStatus;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramWrapper;
import io.resys.hdes.client.spi.util.HdesAssert;

import java.io.Serializable;
import java.util.Map;

public class DebugVisitor {
  private final HdesClient client;

  public DebugVisitor(HdesClient client) {
    super();
    this.client = client;
  }

  public DebugResponse visit(DebugRequest entity, StoreState state) {
    HdesAssert.isTrueOrBadRequest(entity.getInput() != null || entity.getInputCSV() != null, () -> "input or inputCSV must be defined!");
    HdesAssert.isTrueOrBadRequest(entity.getInput() == null || entity.getInputCSV() == null, () -> "input and inputCSV can't be both defined!");

    final var envir = ComposerEntityMapper.toEnvir(client.envir().tagName("test"), state).build();
    final var runnable = envir.getValues().get(entity.getId());
    HdesAssert.notFound(runnable, () -> "Entity was not found by id: '" + entity.getId() + "'!");
    HdesAssert.isTrueOrBadRequest(runnable.getStatus() == ProgramStatus.UP, () -> "Program status: '" + runnable.getStatus() + "' is not runnable!");

    if (entity.getInputCSV() != null) {
      final int noOfLines = entity.getInputCSV().split("\n").length;
      if (noOfLines <= 2) {
        final var csv = new DebugCSVVisitor(client, runnable, envir).visit(entity.getInputCSV());
        return ImmutableDebugResponse.builder().id(entity.getId()).body(csv).build();
      } else {
        final var csv = new DebugCSVVisitor(client, runnable, envir).visitMultiple(entity.getInputCSV());
        return ImmutableDebugResponse.builder().id(entity.getId()).bodyCsv(csv + entity.getInputCSV()).build();
      }
    }

    final var input = client.mapper().toMap(entity.getInput());
    final var json = visitProgram(input, runnable, envir);
    return ImmutableDebugResponse.builder().id(entity.getId()).body(json).build();
  }

  private ProgramResult visitProgram(Map<String, Serializable> input, ProgramWrapper<?, ?> wrapper, ProgramEnvir envir) {
    switch (wrapper.getType()) {
      case FLOW:
        return visitFlow(input, wrapper, envir);
      case FLOW_TASK:
        return visitFlowTask(input, wrapper, envir);
      case DT:
        return visitDecision(input, wrapper, envir);
      default:
        throw new HdesBadRequestException("Can't debug: '" + wrapper.getType() + "'!");
    }
  }

  private ProgramResult visitFlow(Map<String, Serializable> input, ProgramWrapper<?, ?> wrapper, ProgramEnvir envir) {
    final var body = client.executor(envir).inputMap(input).flow(wrapper.getId()).andGetBody();
    return body;
  }

  private ProgramResult visitFlowTask(Map<String, Serializable> input, ProgramWrapper<?, ?> wrapper, ProgramEnvir envir) {
    final var body = client.executor(envir).inputMap(input).service(wrapper.getId()).andGetBody();
    return body;
  }

  private ProgramResult visitDecision(Map<String, Serializable> input, ProgramWrapper<?, ?> wrapper, ProgramEnvir envir) {
    final var body = client.executor(envir).inputMap(input).decision(wrapper.getId()).andGetBody();
    return body;
  }
}
