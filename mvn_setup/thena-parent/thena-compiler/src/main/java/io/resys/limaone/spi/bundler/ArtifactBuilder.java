package io.resys.limaone.spi.bundler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.program.Program.ProgramAssociation;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.spi.compiler.CompilableUnit.Artifact;
import io.resys.limaone.spi.compiler.CompilableUnit.Dependency;
import io.resys.limaone.spi.compiler.ImmutableArtifact;

public class ArtifactBuilder {
  private String artifactId;
  private String artifactName;
  private Model.BodyType artifactType;

  private final List<Dependency> childDeps = new ArrayList<>();
  private final List<Dependency> parentDeps = new ArrayList<>();
  private final List<ModelError> errors = new ArrayList<>();
  private final List<ProgramAssociation> associations = new ArrayList<>();

  private Simple_AST ast;
  private ProgramStatus programStatus;

  public ArtifactBuilder ast(Simple_AST ast) {
    this.ast = Objects.requireNonNull(ast, () -> "ast must be defined");
    return this;
  }
  // Fluent setters
  public ArtifactBuilder artifactId(String artifactId) {
    this.artifactId = Objects.requireNonNull(artifactId, () -> "artifactId must be defined");
    return this;
  }

  public ArtifactBuilder artifactName(String artifactName) {
    this.artifactName = Objects.requireNonNull(artifactName, () -> "artifactName must be defined");
    return this;
  }

  public ArtifactBuilder artifactType(Model.BodyType artifactType) {
    this.artifactType = Objects.requireNonNull(artifactType, () -> "artifactType must be defined");
    return this;
  }

  // Mutable list operations
  public ArtifactBuilder addChildDep(Dependency dep) {
    this.childDeps.add(dep);
    return this;
  }

  public ArtifactBuilder addError(ModelError error) {
    this.errors.add(error);
    return this;
  }

  public ArtifactBuilder clearErrors() {
    this.errors.clear();
    return this;
  }

  // Direct list access for complex operations
  public List<ModelError> getErrors() {
    return errors; // Mutable access
  }

  public List<Dependency> getChildDeps() {
    return childDeps; // Mutable access
  }

  public ArtifactBuilder programStatus(ProgramStatus status) {
    this.programStatus = status;
    return this;
  }
  
  public void init() {
    Objects.requireNonNull(ast, () -> "ast must be defined");
    Objects.requireNonNull(artifactId, () -> "artifactId must be defined");
    Objects.requireNonNull(artifactName, () -> "artifactName must be defined");
    Objects.requireNonNull(artifactType, () -> "artifactType must be defined");
  }

  public Artifact build() {
    return ImmutableArtifact.builder()
        .artifactId(artifactId)
        .artifactName(artifactName)
        .artifactType(artifactType)
        .childDeps(childDeps)
        .parentDeps(parentDeps)
        .errors(errors)
        .associations(associations)
        .programStatus(errors.isEmpty() ? ProgramStatus.UP : ProgramStatus.DEPENDENCY_ERROR)
        .build();
  }

  public String getArtifactId() {
    return artifactId;
  }
  public String getArtifactName() {
    return artifactName;
  }
  public Model.BodyType getArtifactType() {
    return artifactType;
  }
  public Simple_AST getAst() {
    return ast;
  }
}