package io.resys.limaone.spi.bundler;

import java.util.Objects;
import java.util.Optional;

import io.resys.limaone.model.Model;
import io.resys.limaone.spi.compiler.CompilableUnit.Validator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DependencyBuilder {
  private final ArtifactBuilder artifactBuilder;
  
  private String id;
  private Model.BodyType bodyType;
  private Validator validator;

  // Fluent setters
  public DependencyBuilder id(String id) {
    this.id = Objects.requireNonNull(id, () -> "id must be defined");
    return this;
  }
  public DependencyBuilder bodyType(Model.BodyType bodyType) {
    this.bodyType = Objects.requireNonNull(bodyType, () -> "bodyType must be defined");
    return this;
  }
  public DependencyBuilder validator(Validator validator) {
    this.validator = Objects.requireNonNull(validator, () -> "validator must be defined");
    return this;
  }
  public void init() {
    Objects.requireNonNull(id, () -> "id must be defined");
    Objects.requireNonNull(bodyType, () -> "bodyType must be defined");
    Objects.requireNonNull(validator, () -> "validator must be defined");
  }
  public Validator getValidator() {
    return validator;
  }
  public String getId() {
    return id;
  }
  public Model.BodyType getBodyType() {
    return bodyType;
  }
  
  public void close(Optional<ArtifactBuilder> ref) {
    dep.getValidator().validate(ref.map(e -> e.getAst()));
  }
}
