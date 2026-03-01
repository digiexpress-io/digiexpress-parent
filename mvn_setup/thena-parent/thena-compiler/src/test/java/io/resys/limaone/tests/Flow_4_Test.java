package io.resys.limaone.tests;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.programs.FlowProgram.FlowExecutionStatus;
import io.resys.hdes.client.test.config.TestUtils;


public class Flow_4_Test {
  @Test
  public void runAll() throws IOException {
    
    final var envir = TestUtils.client.envir().tagName("say-hello-world")
        .addCommand().id("HelloWorldText").service(service()).build()
        .addCommand().id("say-hello-world").flow(flow(
"""
id: say-hello-world
inputs:
  isHello:
    required: true
    type: BOOLEAN

tasks:
  - decide if to print hello world:
      id: "decision-1"
      switch:
        - greet the user:
            when: "isHello == true"
            then: say_hello
        - dont say anything:
            when: "isHello == false"
            then: format_result

  - map events to queues:
      id: "say_hello"
      then: "format_result"
      service:
        ref: HelloWorldText
        collection: false
        inputs:
          event: isHello

  - format result:
      id: "format_result"
      then: "end"
      returns:
        collection: false
        inputs:
          event: say_hello.text
"""))
        .build()
        .build();
    
    
    {
      final var result = TestUtils.client.executor(envir)
        .inputMap(Map.of(
          "isHello", false
        ))
        .flow("say-hello-world").andGetBody();
  
      Assertions.assertEquals(result.getStatus(), FlowExecutionStatus.COMPLETED);
      Assertions.assertEquals(null, result.getReturns().get("event"));
    }
    
    {
      final var result = TestUtils.client.executor(envir)
        .inputMap(Map.of(
          "isHello", true
        ))
        .flow("say-hello-world").andGetBody();
  
      Assertions.assertEquals(result.getStatus(), FlowExecutionStatus.COMPLETED);
      Assertions.assertEquals("Hello world", result.getReturns().get("event"));
    }
  }


  public String flow(String value) throws JsonProcessingException {
    return TestUtils.objectMapper.writeValueAsString(Arrays.asList(ImmutableAstCommand.builder()
      .type(AstCommandValue.SET_BODY)
      .value(value)
      .build()));
  }
  
  
  public String service() throws JsonProcessingException {
    return TestUtils.objectMapper.writeValueAsString(Arrays.asList(ImmutableAstCommand.builder()
        .type(AstCommandValue.SET_BODY)
        .value(
"""
package io.resys.wrench.assets.bundle.groovy;

import io.resys.hdes.client.api.programs.Program.ProgramContext;
import io.resys.hdes.client.api.programs.ServiceData;
import java.io.Serializable;
import io.resys.hdes.client.spi.util.HdesAssert;


public class HelloWorldText {

  public Output execute(Input input, ProgramContext ctx) {
    Output output = new Output();
    output.text = "Hello world";
    return output;
  }
  
  @ServiceData
  public static class Input implements Serializable {
    Boolean event; 
  }
  
  @ServiceData
  public static class Output implements Serializable {
    String text;
  }
}
""").build()));
  }
}
