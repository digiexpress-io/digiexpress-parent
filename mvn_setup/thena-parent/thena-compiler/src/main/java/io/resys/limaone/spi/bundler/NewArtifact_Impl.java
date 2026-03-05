package io.resys.limaone.spi.bundler;

import java.util.function.Consumer;

import io.resys.limaone.ast.AST_Parser.Dependency_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.spi.compiler.CompilableUnit.NewArtifact;
import io.resys.limaone.spi.compiler.CompilableUnit.Validator;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class NewArtifact_Impl implements NewArtifact {

  private final Consumer<ArtifactBuilder> artifact_callback;
  private final Consumer<DependencyBuilder> dependency_callback;
  private final ArtifactBuilder artifactBuilder = new ArtifactBuilder();
  @Override
  public NewArtifact ast(Simple_AST ast) {
    artifactBuilder.ast(ast).artifactType(ast.getBodyType());
    return this;
  }
  @Override
  public NewArtifact id(String id) {
    artifactBuilder.artifactId(id);
    return this;
  }
  @Override
  public NewArtifact name(String name) {
    artifactBuilder.artifactName(name);
    return this;
  }
  @Override
  public NewArtifact_Impl requireDependnecy(Dependency_AST dep) { 
    this.dependency_callback.accept(
        new DependencyBuilder(artifactBuilder)
          .id(dep.getDependencyId())
          .bodyType(dep.getType())
    );
    return this;
  }
  @Override
  public NewArtifact requireDependnecy(Dependency_AST dep, Validator validator) {
    this.dependency_callback.accept(
        new DependencyBuilder(artifactBuilder)
          .id(dep.getDependencyId())
          .bodyType(dep.getType())
          .validator(validator)
    );
    return this;
  }

  @Override
  public void build() {
    artifactBuilder.init();
    artifact_callback.accept(artifactBuilder);
  }

}
