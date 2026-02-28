package io.resys.limaone.spi.bundler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.program.Program;


public class BundleGroup<T extends Program> {
  private final BodyType type;
  private final Map<String, T> programById;
  private final Map<String, T> programByName;
  
  private BundleGroup(
      BodyType type, 
      Map<String, T> programById,
      Map<String, T> programByName) {
    super();
    this.type = type;
    this.programById = Collections.unmodifiableMap(programById);
    this.programByName = Collections.unmodifiableMap(programByName);
  }

  public BundleGroup(BodyType type) {
    super();
    this.type = type;
    this.programById = new HashMap<>();
    this.programByName = new HashMap<>();
  }
  
  public BundleGroup<T> close() {
    return new BundleGroup<T>(type, programById, programByName);
  }
  
  
  @SuppressWarnings("unchecked")
  public void accept(Program program) {
    if(type == program.getType()) {
      programById.put(program.getId(), (T) program);
      programByName.put(program.getName(), (T) program);
    }
  }
  
  public Optional<T> findOne() {
    if(programById.size() > 1) {
      throw new ProgramQueryException("Query#findOne failed, expected: 0..1, actual: " + programById.size() + " programs!");
    }
    return programById.values().stream().findFirst();
  }
  public Optional<T> findOne(String anyId) {
    if(programById.containsKey(anyId)) {
      return Optional.of(programById.get(anyId));
    }
    if(programByName.containsKey(anyId)) {
      return Optional.of(programByName.get(anyId));
    }
    return Optional.empty();
  }
  
  public static class ProgramQueryException extends RuntimeException {

    private static final long serialVersionUID = -7154685569622201632L;

    public ProgramQueryException(String message) {
      super(message);
    }
    public ProgramQueryException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
