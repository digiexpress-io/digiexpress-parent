package io.resys.hdes.interpreter.spi;

/*-
 * #%L
 * hdes-interpreter
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.interpreter.api.HdesAcceptsSupplier.HdesAcceptsMapSupplier;

public class InterpreterTest {
  private static final ObjectMapper om = new ObjectMapper(new YAMLFactory()).registerModule(new Jdk8Module());

  @Test
  public void matchAll() {
    TraceEnd execution = ImmutableHdesInterpreter.builder().build().executor()
      .src(file("test-dt-matches-all.hdes"))
      .accepts(HdesAcceptsMapSupplier.builder()
          .put("name", "android")
          .put("version", 100)
          .build())
      .build();
    assertExecution(execution, "test-dt-matches-all.yaml");
  }
  
  public void assertExecution(TraceEnd execution, String yaml) {
    String actual = yaml(execution.getBody());
    //System.out.println(yaml(execution));
    String expected = file(yaml);
    Assertions.assertLinesMatch(expected.lines(), actual.lines());
  }
  
  public void format(TraceEnd end) {
  }
  

  public static String yaml(Object node) {
    try {
      return om.writeValueAsString(node);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  public static String file(String name) {
    try {
      return IOUtils.toString(InterpreterTest.class.getClassLoader().getResourceAsStream(name), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
