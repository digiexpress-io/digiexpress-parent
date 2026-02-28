package io.resys.limaone.spi.bundler;

import java.util.Objects;
import java.util.Optional;

import io.resys.limaone.program.Compiler.BundleQuery;
import io.resys.limaone.spi.bundler.BundleGroup.ProgramQueryException;
import io.vertx.core.json.JsonObject;
import io.resys.limaone.program.Program;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BundleQueryImpl<T extends Program> implements BundleQuery<T> {

  private final BundleGroup<T> programs;
  private String name;
  private String externalId;
  private String id;
  
  @Override
  public BundleQuery<T> name(String name) {
    this.name = Objects.requireNonNull(name, () -> "name must be defined!");
    return this;
  }
  @Override
  public BundleQuery<T> externalId(String externalId) {
    this.externalId = Objects.requireNonNull(externalId, () -> "externalId must be defined!");
    return this;
  }
  @Override
  public BundleQuery<T> id(String id) {
    this.id = Objects.requireNonNull(id, () -> "id must be defined!");
    return this;
  }

  @Override
  public Optional<T> findOne() {
    Optional<T> result = Optional.empty();
    if(id != null) {
      result = programs.findOne(id);
    }
    if(result.isPresent()) {
      return result;
    }
    
    if(name != null) {
      result = programs.findOne(name);
    }
    if(result.isPresent()) {
      return result;
    }
    
    if(externalId != null) {
      result = programs.findOne(externalId);
    }
    if(result.isPresent()) {
      return result;
    }
    
    return programs.findOne();
  }

  @Override
  public T getOne() {
    return findOne().orElseThrow(() -> new ProgramQueryException(
        "Query#findOne failed, expected: 1, actual: 0 programs, predicate: " + 
        JsonObject.of(
          "id", id, 
          "name", name,
          "externalId", externalId
        ).encodePrettily()));
  }
}
