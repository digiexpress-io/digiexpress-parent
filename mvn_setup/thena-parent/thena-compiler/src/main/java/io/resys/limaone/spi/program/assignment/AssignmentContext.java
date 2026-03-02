package io.resys.limaone.spi.program.assignment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.resys.limaone.ast.Flow_AST.InputsStatement;
import io.resys.limaone.ast.Flow_AST.ManyTasksStatement;
import io.resys.limaone.ast.Flow_AST.MappingStatement;
import io.resys.limaone.ast.Flow_AST.OneTaskStatement;
import io.resys.limaone.program.DecisionProgram.DecisionResult;
import io.resys.limaone.program.FlowTaskProgram.FlowTaskResult;
import io.resys.limaone.program.ProgramInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class AssignmentContext {
  
  private final io.resys.limaone.program.Runtime runtime;
  private final ProgramInput input;
  private Map<String, Initializer> initalizers = new HashMap<>();
  private ManyTasksStatement initalizers_tasks;
  
  private Map<String, Assignment> assigned = new HashMap<>();
  

  
  // program input... ie constructor args
  public void initalizers(InputsStatement statement) {

    List<String> required = new ArrayList<>();
    for(final var dataType : statement.getParameters()) {
      Serializable value = input.getValue(dataType);
      if(value != null) {
        initalizers.put(dataType.getName(), new Initializer(value));
      }
      if(dataType.isRequired() && value == null) {
        required.add(dataType.getName());
      }
    }
    if(!required.isEmpty()) {
      throw new NullPointerException("Flow can't have null inputs: " + String.join(", ", required) + "!");
    }
  }
  
  public void initalizers(ManyTasksStatement tasks) {
    this.initalizers_tasks = Objects.requireNonNull(tasks, "tasks can't be null!");
  }
  
  public void assignFromTask(OneTaskStatement task, DecisionResult result) {
    
  }

  public void assignFromTask(OneTaskStatement task, FlowTaskResult result) {
    
  }

  /*
  public void assignFromTask(OneTaskStatement task, ReturnsResult result) {
    
  }
  */
  
  
  public List<Map<String, Serializable>> assignTo(String taskId, MappingStatement statement) {
    final Map<String, Serializable> deconstructed = new HashMap<>();
    for(final var deconstruct : statement.getDeconstructors()) {
      deconstructed.putAll(Optional.ofNullable(assigned.get(deconstruct))
          .map(a -> a.getValue())
          .orElse(Collections.<String, Serializable>emptyMap()));
      
      deconstructed.putAll(Optional.ofNullable(initalizers.get(deconstruct))
          .map(a -> a.getAnyMap())
          .orElse(Collections.<String, Serializable>emptyMap()));
    }

    final List<Map<String, Serializable>> exploded = new ArrayList<>();
    
    for(final var mapping : AssignmentExpander.from(statement, this::findParameter)) {
      final Map<String, Serializable> result = new HashMap<>(deconstructed);
      for(final var entry : mapping.entrySet()) {
        final String nameOnService = entry.getKey();
        
        try {
          // Flat mapping
          Serializable value = findParameter(entry.getValue());
          
          if(value != null) {
            result.put(nameOnService, value);
          }
        } catch(Exception e) {
          throw new IllegalArgumentException(
              "Failed to get parameter: '" + entry.getKey() + ":" + entry.getValue() + "' while mapping step: '" + taskId + "'" + System.lineSeparator() + 
              e.getMessage(), e);
        }
      }
      
      exploded.add(Collections.unmodifiableMap(result));
    }
    
    if(exploded.isEmpty()) {
      exploded.add(Collections.unmodifiableMap(deconstructed));
    }
    
    return Collections.unmodifiableList(exploded);
  }
  
  @SuppressWarnings("unchecked")
  private Serializable findParameter(String name) {
    final var paths = name.split("\\.");
    if(paths.length == 0) {
      return null;
    }
    final var fullName = new StringBuilder();
    
    Map<String, Serializable> prev = null;
    
    int index = 0;
    for(String path : paths) {
      index++;
      final var isLast = index == paths.length;
      if(fullName.length() > 0) {
        fullName.append(".");
      }
      fullName.append(path);
      
      
      // first parameter
      if(prev == null) {
        // resolve based on accepted
        if(initalizers.containsKey(path)) {
          final var target = initalizers.get(path);
          
          if(target.isMap()) {
            prev = target.getMap();
          } else if(isLast) {
            return (Serializable) target.getRaw();
          } else {
            prev = target.getExploded();
          }
          continue;
        }
        
        // referring to step but it was not executed
        if(initalizers_tasks.getTasks().containsKey(path) && paths.length > 1 && !assigned.containsKey(path)) {
          return null;
        }
        
        // resolve from executed steps
        if(assigned.containsKey(path)) {
          prev = assigned.get(path).getValue();
          if(isLast) {
            return (Serializable) prev;
          }
          continue; 
        }
        
        // root context
        final var runtimeProp = runtime.getParameters().findValueWithMeta(path);
        if(runtimeProp.getFound()) {
          final var result = runtimeProp.getValue();
          if(isLast) {
            return result;
          } else if(result instanceof Map) {
            prev = (Map<String, Serializable>) result;
          }
        }
      }
      
      if(prev == null) {
        // if parameter isn't found, return the provided value, or null if the value is null
        if (path == null || path.equals("null")) {
          return null;
        }
        return path;
      }
      
      if(isArray(path)) {
        final var arrayIndex = getArrayIndex(path);
        final var array = AssignmentExpander.getCollectionType(prev);
        
        if(array.size() < arrayIndex) {
          return null;  
        }
        
        try {
          prev = (Map<String, Serializable>) array.get(arrayIndex);
          continue; 
        } catch(Exception e) {
          log.error("Can't find parameter with name: '" + name + "' from: '" + fullName + "'!");
          return null;  
        }
      }
      
      if(prev.containsKey(path) && isLast) {
        return prev.get(path);
      } else if(prev.containsKey(path) && !isLast) {
        prev = (Map<String, Serializable>) prev.get(path);
      } else {
        log.error("Can't find parameter with name: '" + name + "' from: '" + fullName + "'!");
        return null;
      }
    }
    return null;
  }
  

  private static boolean isArray(String path) {
    try {
      Integer.parseInt(path.substring(1, path.length() -1));
      return true;
    } catch(Exception e) {
      return false;
    }
  }
  private static int getArrayIndex(String path) {
    return Integer.parseInt(path.substring(1, path.length() -1));
  }

  // creates new instance of inputs 
  public ProgramInput withInputs(Map<String, Serializable> nextInputs) {
    return input.withInputs(nextInputs);
  }
}
