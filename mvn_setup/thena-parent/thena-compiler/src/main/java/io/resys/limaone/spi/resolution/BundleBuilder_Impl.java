package io.resys.limaone.spi.resolution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import io.resys.limaone.program.Compiler.Bundle;
import io.resys.limaone.program.Program;
import io.resys.limaone.spi.compiler.CompilableUnit.BundleBuilder;
import io.resys.limaone.spi.compiler.CompilableUnit.NewArtifact;
import io.resys.limaone.spi.compiler.CompilableUnit.OpenProgram;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;


public class BundleBuilder_Impl implements BundleBuilder {

  private final Map<String, ArtifactBuilder> artifacts_byId = new ConcurrentHashMap<>();
  private final Map<String, ArtifactBuilder> artifacts_byName = new ConcurrentHashMap<>();
  private final List<DependencyBuilder> deps = Collections.synchronizedList(new ArrayList<>()); 
  
  
  @Override
  public NewArtifact newArtifact() {
    final Consumer<ArtifactBuilder> artifact_callback = (artifactBuilder) -> {
      artifacts_byId.put(artifactBuilder.getArtifactId(), artifactBuilder);
      artifacts_byName.put(artifactBuilder.getArtifactType() + "/" + artifactBuilder.getArtifactName(), artifactBuilder);
    };
    final Consumer<DependencyBuilder> dependency_callback = (dependencyBuilder) -> {
      deps.add(dependencyBuilder);
    };
    return new NewArtifact_Impl(artifact_callback, dependency_callback);
  }
  
  @Override
  public Uni<Bundle> build(List<OpenProgram> openProgram) {
    return Multi.createFrom().items(deps.stream())
      .onItem().invoke(this::validate)
      .collect().asList().replaceWithVoid()
      .onItem().transformToMulti((ignore) -> Multi.createFrom().items(openProgram.stream()))
      .onItem().transform(open -> close(open))
      .collect().asList().replaceWithVoid()
      .onItem().transform((ignore) -> createBundle());
  }
  
  private Bundle createBundle() {
    
  }
  
  private Program close(OpenProgram open) {
    
  }
  
  private void validate(DependencyBuilder dep) {
    
  }
}
