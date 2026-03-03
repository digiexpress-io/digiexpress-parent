package io.resys.limaone.spi.program.assignment;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import io.resys.limaone.ast.Flow_AST.OneTaskStatement;

public class Assignment {
  public static final String ARRAY_KEY = "";
  public static final String RESERVED_INPUT_TO_OUTPUT_KEY = "_";
  private final OneTaskStatement owner;
  private final Map<String, Serializable> ownerInput;
  private final Map<String, Serializable> ownerOutput;
  private Map<String, Serializable> built;
  
  public Assignment(
      OneTaskStatement owner,
      Map<String, Serializable> ownerInput,
      Map<String, Serializable> ownerOutput) {
    super();
    this.owner = owner;
    this.ownerInput = ownerInput;
    this.ownerOutput = ownerOutput;
  }

  
  public static Map<String, Serializable> toArrayMap(Stream<Object> object) {
    return Map.<String, Serializable>of(ARRAY_KEY, (Serializable) object.toList());
  }

  @SuppressWarnings("unchecked")
  public Map<String, Serializable> getValue() {
    if(built != null) {
      return built;
    }
    
    final var built = new HashMap<String, Serializable>(ownerOutput);
    
    if(owner.getBody().isCollection()) {
      final var unwrapped = (Collection<?>) this.ownerOutput.getOrDefault(ARRAY_KEY, (Serializable) Collections.emptyList());
      
      final Serializable assignment = (Serializable) unwrapped.stream()
        .map(object -> (Map<String, Serializable>) object)
        .map(object -> new HashMap<>((Map<String, Serializable>) object))
        .map(object -> {
          ownerInput.entrySet().stream()
            .filter(e -> !object.containsKey(e.getKey()))
            .forEach(e -> object.put(RESERVED_INPUT_TO_OUTPUT_KEY + e.getKey(), e.getValue()));  
          return Collections.unmodifiableMap(object);
        }).toList();
      built.put(ARRAY_KEY, assignment);
    } else {
      ownerInput.entrySet().forEach(e -> built.put(RESERVED_INPUT_TO_OUTPUT_KEY + e.getKey(), e.getValue()));  
    }

    this.built = Collections.unmodifiableMap(built);
    return this.built;
  }
}
