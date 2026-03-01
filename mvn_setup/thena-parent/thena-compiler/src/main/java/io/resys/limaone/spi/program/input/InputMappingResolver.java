package io.resys.limaone.spi.program.input;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class InputMappingResolver {

  private final Map<String, String> mapping;
  private final List<Map<String, String>> expanded = new ArrayList<>();
  private final Function<String, Serializable> getVariable;
  private boolean isOk = true;
  private boolean isExpanded = false;
  
  
  public List<Map<String, String>> accept() {
    // split paths
    mapping.entrySet().forEach(this::visitMapping);
    
    if(isOk && isExpanded) {
      return expanded; 
    }
    
    return Arrays.asList(this.mapping);
  }

  private void visitMapping(Map.Entry<String, String> entry) {
    final var name = entry.getValue();
    final var paths = name.split("\\.");
    if(paths.length <= 1) {
      return;
    }

    final var parentPath = String.join(".", Arrays.copyOf(paths, paths.length-1));
    final var someList = getCollectionType(getVariable.apply(parentPath));
    
    if(someList == null) {
      return;
    }
    
    if(someList.isEmpty()) {
      isExpanded = true;
      return;
    }
    
    int index = 0;
    for(final var value : someList) {
      
      if(!isOk) {
        return;
      }
      
      if(!(value instanceof Map)) {
        isOk = false;
        return;
      }
      isExpanded = true;
      visitArrayExpansion(index, parentPath);
      index++;
    }
  }
  
  private void visitArrayExpansion(int index, String parentPath) {
    final Map<String, String> expandedMapping = new HashMap<>();
    for(final var entry : mapping.entrySet()) {
      
      final var target = parentPath + ".";
      final Map.Entry<String, String> expanded;
      if(entry.getValue().startsWith(target)) {
        
        final var start = entry.getValue().substring(0, target.length());
        final var end = entry.getValue().substring(target.length());
        expanded = Map.entry(entry.getKey(), start + "[" + index + "]." + end);
      } else {
        expanded = entry;
      }
      
      expandedMapping.put(expanded.getKey(), expanded.getValue());
    }
    if(expanded.size() < index + 1) {
      expanded.add(expandedMapping);      
    } else {
      expanded.get(index).putAll(expandedMapping);
    }
  }

  @SuppressWarnings("rawtypes")
  public static List getCollectionType(Object possiblyList) {
    if(!(possiblyList instanceof Map)) {
      return null;
    }
    final var someMap = ((Map) possiblyList).values();
    if(someMap.size() != 1) {
      return null;
    }
    final var firstValue = someMap.iterator().next();
    if(!(firstValue instanceof List)) {
      return null;
    }
    return (List) firstValue;
  }
}
