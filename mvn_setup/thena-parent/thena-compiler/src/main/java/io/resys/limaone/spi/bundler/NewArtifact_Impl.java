package io.resys.limaone.spi.bundler;

import java.util.function.Consumer;

import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.spi.compiler.CompilableUnit.NewArtifact;
import io.resys.limaone.spi.compiler.CompilableUnit.RequireDependency;
import io.resys.limaone.spi.compiler.CompilableUnit.Validator;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class NewArtifact_Impl implements NewArtifact {

  private final Consumer<ArtifactBuilder> artifact_callback;
  private final Consumer<DependencyBuilder> dependency_callback;
  private final ArtifactBuilder artifactBuilder = new ArtifactBuilder();
  @Override
  public NewArtifact ast(Simple_AST ast) {
    artifactBuilder.ast(ast);
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
  public NewArtifact type(BodyType bodyType) {
    artifactBuilder.artifactType(bodyType);
    return this;
  }
  @Override
  public RequireDependency requireDependnecy() {
    final DependencyBuilder dependencyBuilder = new DependencyBuilder(artifactBuilder); 
    this.dependency_callback.accept(dependencyBuilder);
    
    return new RequireDependency() {
      @Override
      public RequireDependency validator(Validator validator) {
        dependencyBuilder.validator(validator);
        return this;
      }
      @Override
      public RequireDependency id(String idOrName) {
        dependencyBuilder.id(idOrName);
        return this;
      }
      @Override
      public RequireDependency bodyType(BodyType bodyType) {
        dependencyBuilder.bodyType(bodyType);
        return this;
      }
      @Override
      public void build() {
        dependencyBuilder.init();
      }
      
    };
  }
  @Override
  public void build() {
    artifactBuilder.init();
    artifact_callback.accept(artifactBuilder);
  }

}
