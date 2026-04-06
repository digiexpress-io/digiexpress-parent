package io.resys.limaone.yaml;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.resys.limaone.ast.Yaml_CST.Yaml;


public class YamlMapper {
  private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

  public static String getStringValue(Yaml node) {
    if (node == null || node.getValue() == null) {
      return null;
    }
    return node.getValue();
  }

  public static boolean getBooleanValue(Yaml node) {
    if (node == null || node.getValue() == null) {
      return false;
    }
    return Boolean.parseBoolean(node.getValue());
  }
  
  public ObjectMapper unwrap() {
    return yaml;
  }
}
