package io.resys.limaone.tests;

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.hash.Hashing;

import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.FlowTask.ServiceData;
import io.resys.limaone.model.ImmutableFlowTask;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.ImmutableModelWorld;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.program.Compiler;
import io.resys.limaone.program.FlowTaskProgram;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.vertx.core.json.JsonObject;

public class FlowTaskTest {
  private final Compiler compiler = CompilerImpl.builder().build();

  public FlowTaskProgram compile(String fullPath) {
    try {
      final var taskValue = IOUtils.toString(DecisionTest.class.getClassLoader().getResource(fullPath), StandardCharsets.UTF_8);
      final var model = ImmutableModel.<FlowTask>builder()
          .id(fullPath)
          .bodyHash(Hashing.murmur3_128().hashString(taskValue, StandardCharsets.UTF_8).toString())
          .bodyType(BodyType.FLOW_TASK)
          .body(ImmutableFlowTask.builder()
              .taskName(fullPath)
              .taskValue(taskValue)
              .build())
          .build();
      
      final var world = ImmutableModelWorld.builder().name("FlowTaskTest")
          .putFlowTasks(model.getId(), model)
          .build();
      return compiler.compile(world).id(fullPath).build().queryFlowTasks().name(fullPath).getOne();
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Test
  public void type0() throws IOException {
    final var envir = compile("flow-task/Type1Service.txt");

    // map conversion
    final var result = envir.run(Map.of("a", 5, "b", 10)).andGetBody();
    Assertions.assertEquals("{\"sum\":15}", JsonObject.mapFrom(result.getValue()).encode());
  }

  @Test
  public void type2() throws IOException {
    final var envir = compile("flow-task/Type2Service.txt");

    // map conversion
    final var result = envir.run(Map.of("a", 5, "b", 10)).andGetBody();
    Assertions.assertEquals("{\"sum\":15}", JsonObject.mapFrom(result.getValue()).encode());
  }

  @ServiceData
  @Value.Immutable
  public interface TestServiceInput {
    Integer getA();
    Integer getB();
  }
}
