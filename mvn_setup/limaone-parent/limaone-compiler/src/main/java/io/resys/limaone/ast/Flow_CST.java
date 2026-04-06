package io.resys.limaone.ast;

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

import java.io.Serializable;
import java.util.Map;

import io.resys.limaone.ast.Yaml_CST.Yaml;
import jakarta.annotation.Nullable;

public interface Flow_CST extends Serializable {


  interface YamlFlow extends Yaml {
    Yaml getId();
    Yaml getDescription();
    Map<String, YamlInput> getInputs();
    Map<String, YamlTask> getTasks();
  }

  interface YamlTask extends Yaml {
    Yaml getId();
    int getOrder(); // 0 = first task
    Yaml getThen();
    @Nullable YamlTaskBody getRef();
    @Nullable YamlTaskBody getUserTask();
    @Nullable YamlTaskBody getDecisionTable();
    @Nullable YamlTaskBody getService();
    @Nullable YamlTaskBody getReturns();
    Map<String, YamlSwitch> getSwitch();
  }
  
  interface YamlTaskBody extends Yaml {
    Yaml getRef();
    Yaml getCollection();
    Yaml getInputsNode();
    Map<String, Yaml> getInputs();
    @Nullable String getObjectInput();
  }

  interface YamlSwitch extends Yaml {
    int getOrder();
    Yaml getWhen();
    Yaml getThen();
  }

  interface YamlInput extends Yaml {
    /*
     * valid input types: 
     *   - ValueType.STRING, 
     *   - ValueType.BOOLEAN, 
     *   - ValueType.INTEGER, 
     *   - ValueType.LONG, 
     *   - ValueType.DECIMAL, 
     *   - ValueType.DATE, 
     *   - ValueType.DATE_TIME
     */
    Yaml getRequired();
    Yaml getType();
    Yaml getDebugValue();
  }
}
