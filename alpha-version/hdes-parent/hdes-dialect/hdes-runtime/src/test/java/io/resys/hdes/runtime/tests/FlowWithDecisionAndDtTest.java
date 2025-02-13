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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.hdes.executor.api.Trace.TraceEnd;


public class FlowWithDecisionAndDtTest {
  @Test
  public void flowWithNoSteps() {
    TraceEnd output = TestUtil.runtime().src(fileSrc("flowWithNoSteps")).build("EmptyFlow")
        .accepts()
        .value("arg1", 11)
        .value("arg2", 2)
        .build();

    Assertions.assertLinesMatch(fileYaml("flowWithNoSteps").lines(), yaml(output.getBody()).lines());
  }
  
  @Test
  public void flowWithDecisionAndSplit() {
    
    TraceEnd output = TestUtil.runtime().src(fileSrc("flowWithDecisionAndSplit")).build("SimpleFlow")
        .accepts()
        .value("arg1", 11)
        .value("arg2", 2)
        .build();
    
    Assertions.assertLinesMatch(fileYaml("flowWithDecisionAndSplit").lines(), yaml(output.getBody()).lines());
  }
  
  @Test
  public void flowWithParalDecisionAndSplit() {
    TraceEnd output = TestUtil.runtime().src(fileSrc("flowWithParalDecisionAndSplit")).build("SimpleFlow")
        .accepts()
        .value("arg1", 11)
        .value("arg2", 2)
        .build();
    
    Assertions.assertLinesMatch(fileYaml("flowWithParalDecisionAndSplit").lines(), yaml(output.getBody()).lines());
  }
  
  private static String fileSrc(String file) {
    String value = TestUtil.file("FlowWithDecisionAndDtTest/" + file + ".hdes");
    return value;
  }
  
  private static String fileYaml(String file) {
    String value = TestUtil.file("FlowWithDecisionAndDtTest/" + file + ".yml");
    return value;
  } 
}
