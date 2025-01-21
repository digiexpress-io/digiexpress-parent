package io.resys.hdes.client.spi.flow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    
    if(someList.isEmpty()) {
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
    expanded.add(expandedMapping);
  }

  @SuppressWarnings("rawtypes")
  public static List getCollectionType(Object possiblyList) {
    if(!(possiblyList instanceof Map)) {
      return Collections.emptyList();
    }
    final var someMap = ((Map) possiblyList).values();
    if(someMap.size() != 1) {
      return Collections.emptyList();
    }
    final var firstValue = someMap.iterator().next();
    if(!(firstValue instanceof List)) {
      return Collections.emptyList();
    }
    final var someList = (List) firstValue;
    if(someList.isEmpty()) {
      return Collections.emptyList();
    }
    
    return someList;
  }
}
