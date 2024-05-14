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

import java.io.Serializable;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.executor.api.TraceBody.Await;
import io.resys.hdes.executor.api.TraceBody.Suspends;
import io.resys.hdes.runtime.tests.TestUtil.TestRunner;


@Slf4j
public class FlowWithWakeUpTest {
  
  @Test
  public void simpleFlow() {
    TestRunner runner = TestUtil.runtime().src(fileSrc("simpleFlow")).build("SimpleFlow");
    TraceEnd output = runner.accepts()
        .value("arg1", 11)
        .value("arg2", 2)
        .build();

    Suspends suspends = output.getSuspends();
    Assertions.assertNotNull(suspends);
    Assertions.assertNotNull(suspends.getValues().size() == 1);
    log.debug(yaml(suspends));

    Await await = suspends.getValues().iterator().next();
    
    HashMap<String, Serializable> promiseData = new HashMap<>();
    promiseData.put("userValue", 100);
    output = runner.wakeup().accepts(await.getDataId(), promiseData).build(output);
    Assertions.assertLinesMatch(fileYaml("simpleFlow").lines(), yaml(output.getBody()).lines());
      
  }
  
  private static String fileSrc(String file) {
    String value = TestUtil.file("FlowWithWakeUpTest/" + file + ".hdes");
    return value;
  }
  
  private static String fileYaml(String file) {
    String value = TestUtil.file("FlowWithWakeUpTest/" + file + ".yml");
    return value;
  } 
}
