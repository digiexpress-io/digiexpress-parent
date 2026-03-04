package io.resys.limaone.spi.bundler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import io.resys.limaone.program.Compiler.BundleBuilder;
import io.resys.limaone.program.Program;
import io.resys.limaone.spi.compiler.CompilableUnit.Bundler;
import io.resys.limaone.spi.compiler.CompilableUnit.NewArtifact;
import io.resys.limaone.spi.compiler.CompilableUnit.OpenProgram;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;


public class BundlerImpl implements Bundler {
  private final Map<String, ArtifactBuilder> artifacts_byId = new ConcurrentHashMap<>();
  private final Map<String, ArtifactBuilder> artifacts_byName = new ConcurrentHashMap<>();
  private final List<DependencyBuilder> deps = Collections.synchronizedList(new ArrayList<>());
  private final BundleBuilderImpl bundleBuilder = new BundleBuilderImpl();
  
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
  public Uni<BundleBuilder> build(List<OpenProgram> openProgram) {
    return Multi.createFrom().items(deps.stream())
      .onItem().invoke(this::validate)
      .collect().asList().replaceWithVoid()
      .onItem().transformToMulti((ignore) -> Multi.createFrom().items(openProgram.stream()))
      .onItem().transform(open -> close(open))
      .collect().asList().replaceWithVoid()
      .onItem().transform((ignore) -> createBundle());
  }
  
  private BundleBuilder createBundle() {
    return bundleBuilder;
  }
  
  private Program close(OpenProgram open) {
    final var builder = Optional.ofNullable(artifacts_byId.get(open.getId()))
      .or(() -> {
        final var name = open.getAst().getBodyType() + "/" + open.getAst().getName();
        return Optional.ofNullable(artifacts_byName.get(name));
      }).orElse(null);
  
    Objects.requireNonNull(builder, () -> "Can't find program to finalize, id: " + open.getId());
    final var artifact = builder.build();
    final var program = open.close(artifact);
    bundleBuilder.addProgram(program);
    return program;
  }
  
  private void validate(DependencyBuilder dep) {
    final var id = dep.getId();
    final var name = dep.getBodyType() + "/" + dep.getId();
    final var ref = Optional.ofNullable(artifacts_byId.get(id)).or(() -> Optional.ofNullable(artifacts_byName.get(name)));
    dep.close(ref);
  }
}
