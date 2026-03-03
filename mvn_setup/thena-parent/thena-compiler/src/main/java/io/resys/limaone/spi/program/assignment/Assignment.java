package io.resys.limaone.spi.program.assignment;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import io.resys.limaone.spi.program.stack.StackFrame;
import io.resys.limaone.spi.program.stack.StackFrame.StackFrameBody;

public class Assignment {
  public static final String ARRAY_KEY = "";
  public static final String RESERVED_INPUT_TO_OUTPUT_KEY = "_";
  private final StackFrame frame;
  private Map<String, Serializable> built;
  
  public Assignment(StackFrame frame) {
    super();
    this.frame = frame;
  }

  public static Map<String, Serializable> toArrayMap(Stream<?> object) {
    return Map.<String, Serializable>of(ARRAY_KEY, (Serializable) object.toList());
  }
  
  public static Assignment of(StackFrame frame) {
    return new Assignment(frame);
  }

  public Map<String, Serializable> getValue() {
    if(built != null) {
      return built;
    }
    
    final Map<String, Serializable> built;
    if(frame.getStatement().isCollection()) {
      built = toArrayMap(frame.getMatches().stream().map(this::toRow));
    } else {
      built = frame.getMatches().stream()
          .map(this::toRow)
          .findFirst().orElse(Collections.emptyMap());
    }
    
    
    this.built = Collections.unmodifiableMap(built);
    return this.built;
  }
  
  private Map<String, Serializable> toRow(StackFrameBody match) {
    final var row = new HashMap<String, Serializable>();
    match.getInputs().entrySet().forEach(e -> row.put(RESERVED_INPUT_TO_OUTPUT_KEY + e.getKey(), e.getValue()));  
    match.getOutputs().entrySet().forEach(e -> row.put(e.getKey(), e.getValue()));
    return Collections.unmodifiableMap(row);
  }
}
