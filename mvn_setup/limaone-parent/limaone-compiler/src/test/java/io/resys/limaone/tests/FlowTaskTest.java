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
import java.util.Map;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.model.FlowTask.ServiceData;
import io.resys.limaone.tests.support.TestTemplate;
import io.vertx.core.json.JsonObject;

public class FlowTaskTest {

  @Test
  public void type0() throws IOException {
    final var envir = TestTemplate.compileOneFlowTask("flow-task/Type1Service.txt");

    // map conversion
    final var result = envir.run(Map.of("a", 5, "b", 10)).andGetBody();
    Assertions.assertEquals("{\"sum\":15}", JsonObject.mapFrom(result.getValue()).encode());
  }

  @Test
  public void type2() throws IOException {
    final var envir = TestTemplate.compileOneFlowTask("flow-task/Type2Service.txt");

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
