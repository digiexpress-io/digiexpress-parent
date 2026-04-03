package io.resys.limaone.tests;

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

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.program.FlowProgram.FlowExecutionStatus;
import io.resys.limaone.tests.support.TestTemplate;
import io.resys.limaone.tests.support.TestTemplate.Deps;


public class Flow_4_Test {
  @Test
  public void runAll() {
    
    final var envir = TestTemplate.compileOneFlow(
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
""", Deps.ftx(service()));
    
    
    {
      final var result = envir.run(Map.of("isHello", false )).andGetBody();
      Assertions.assertEquals(result.getStatus(), FlowExecutionStatus.COMPLETED);
      Assertions.assertEquals(null, result.getReturns().get("event"));
    }
    
    {
      final var result = envir.run(Map.of("isHello", true )).andGetBody();
      Assertions.assertEquals(result.getStatus(), FlowExecutionStatus.COMPLETED);
      Assertions.assertEquals("Hello world", result.getReturns().get("event"));
    }
  }
  
  public String service() {
    return
"""
  public class HelloWorldText {

    public Output execute(Input input, Runtime ctx) {
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
""";
  }
}
