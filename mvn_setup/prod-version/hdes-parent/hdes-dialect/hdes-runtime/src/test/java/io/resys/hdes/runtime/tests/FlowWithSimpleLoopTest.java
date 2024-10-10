package io.resys.hdes.runtime.tests;

/*-
 * #%L
 * hdes-runtime
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

import static io.resys.hdes.runtime.tests.TestUtil.yaml;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.hdes.executor.api.Trace.TraceEnd;

public class FlowWithSimpleLoopTest {
  
  @Test
  public void loop() {
    TraceEnd output = TestUtil.runtime().src(fileSrc("loop")).build("ScoringClassifiersFlow")
        .accepts()
        .value("classifiers", new ArrayList<>(Arrays.asList(10, 12)))
        .build();
    
    yaml(output.getBody());
    
    Assertions.assertLinesMatch(fileYaml("loop").lines(), yaml(output.getBody()).lines());
    
  }

  private static String fileSrc(String file) {
    String value = TestUtil.file("FlowWithSimpleLoopTest/" + file + ".hdes");
    return value;
  }
  
  private static String fileYaml(String file) {
    String value = TestUtil.file("FlowWithSimpleLoopTest/" + file + ".yml");
    return value;
  } 
}
