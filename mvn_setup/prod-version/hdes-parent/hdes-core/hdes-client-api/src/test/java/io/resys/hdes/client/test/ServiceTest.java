package io.resys.hdes.client.test;

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
import java.util.Arrays;
import java.util.Map;

import org.immutables.value.Value;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.programs.ServiceData;
import io.resys.hdes.client.api.programs.ServiceProgram.ServiceResult;
import io.resys.hdes.client.spi.util.FileUtils;
import io.resys.hdes.client.test.config.TestUtils;

public class ServiceTest {

  @Test
  public void type0() throws IOException {
    final String src = TestUtils.objectMapper.writeValueAsString(Arrays.asList(ImmutableAstCommand.builder()
        .type(AstCommandValue.SET_BODY)
        .value(FileUtils.toString(getClass(), "service/Type1Service.txt"))
        .build()));
    final var envir = TestUtils.client.envir().addCommand().id("Type1Service.txt").service(src).build().build();

    
  
    // map conversion
    ServiceResult result = TestUtils.client.executor(envir)
      .inputMap(Map.of("a", 5, "b", 10))
      .service("Type1Service")
      .andGetBody();
    
    Assertions.assertEquals("{\"sum\":15}", TestUtils.objectMapper.writeValueAsString(result.getValue()));
    
    // data object conversion
    result = TestUtils.client.executor(envir)
        .inputEntity(ImmutableTestServiceInput.builder().a(5).b(10).build())
        .service("Type1Service")
        .andGetBody();
    // object to object conversion
    Assertions.assertEquals("{\"sum\":15}", TestUtils.objectMapper.writeValueAsString(result.getValue()));
  }
  
  
  @Test
  public void type2() throws IOException {
    final String src = TestUtils.objectMapper.writeValueAsString(Arrays.asList(ImmutableAstCommand.builder()
        .type(AstCommandValue.SET_BODY)
        .value(FileUtils.toString(getClass(), "service/Type2Service.txt"))
        .build()));
    final var envir = TestUtils.client.envir().addCommand().id("Type2Service.txt").service(src).build().build();

    // map conversion
    ServiceResult result = TestUtils.client.executor(envir)
      .inputMap(Map.of("a", 5, "b", 10))
      .service("Type2Service")
      .andGetBody();
    
    Assertions.assertEquals("{\"sum\":15}", TestUtils.objectMapper.writeValueAsString(result.getValue()));
    
    // data object conversion
    result = TestUtils.client.executor(envir)
        .inputEntity(ImmutableTestServiceInput.builder().a(5).b(10).build())
        .service("Type2Service")
        .andGetBody();
    // object to object conversion
    Assertions.assertEquals("{\"sum\":15}", TestUtils.objectMapper.writeValueAsString(result.getValue()));
  }
  
  @ServiceData
  @Value.Immutable
  public interface TestServiceInput {
    Integer getA();
    Integer getB();
  }
}
