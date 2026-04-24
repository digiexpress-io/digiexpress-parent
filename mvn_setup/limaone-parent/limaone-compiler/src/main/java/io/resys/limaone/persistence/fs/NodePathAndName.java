package io.resys.limaone.persistence.fs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

import lombok.Value;

@Value
public class NodePathAndName {
  
  String path;
  String name;
  
  public String getFullPath() {
    final var path = Optional.ofNullable(this.path).map(e -> e.isEmpty() ? "" : e + "/").get();
    return path + name;
  }
  
  public static NodePathAndName of(String path, String name) {
    return new NodePathAndName(path, name);
  }
  
  public static NodePathAndName of(String fullPath) {
    final var pathSections = fullPath.split("/");
    final var name = pathSections[pathSections.length -1];
    final var path = pathSections.length > 1 ? fullPath.substring(0, fullPath.lastIndexOf("/")) : "";
    return new NodePathAndName(path, name);
  }
  
  public static List<NodePathAndName> explode(String fullPath) {
    final List<NodePathAndName> result = new ArrayList<>();
    
    String next = fullPath;
    do {
    
      final var pathAndName = NodePathAndName.of(next);
      result.add(pathAndName);
      next = pathAndName.getPath();
      
    } while(!next.isEmpty());
    
    return Collections.unmodifiableList(result);
  }
}
