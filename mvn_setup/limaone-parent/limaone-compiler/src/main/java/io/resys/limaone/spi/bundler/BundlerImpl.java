package io.resys.limaone.spi.bundler;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import io.resys.limaone.program.Compiler.BundleBuilder;
import io.resys.limaone.program.Runtime.EnvironmentProperties;
import io.resys.limaone.spi.compiler.CompilableUnit.ArtifactLink;
import io.resys.limaone.spi.compiler.CompilableUnit.Bundler;
import io.resys.limaone.spi.compiler.CompilableUnit.NewArtifact;
import io.resys.limaone.spi.compiler.CompilableUnit.RuntimeLink;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;


public class BundlerImpl implements Bundler {
  private final Map<String, ArtifactBuilder> artifacts_byName = new ConcurrentHashMap<>();
  private final List<DependencyBuilder> deps = Collections.synchronizedList(new ArrayList<>());
  private final BundleBuilderImpl bundleBuilder;
  
  public BundlerImpl(EnvironmentProperties properties) {
    this.bundleBuilder = new BundleBuilderImpl(properties);
  }

  @Override
  public NewArtifact newArtifact() {
    final Consumer<ArtifactBuilder> artifact_callback = (artifactBuilder) -> {
      artifacts_byName.put(artifactBuilder.getArtifactType() + "/" + artifactBuilder.getArtifactName(), artifactBuilder);
    };
    final Consumer<DependencyBuilder> dependency_callback = (dependencyBuilder) -> {
      deps.add(dependencyBuilder);
    };
    return new NewArtifact_Impl(artifact_callback, dependency_callback);
  }
  
  @Override
  public Uni<BundleBuilder> build(List<ArtifactLink> openProgram) {
    return Multi.createFrom().items(() -> deps.stream())
      .onItem().transform(this::validate)
      .collect().asList().replaceWithVoid()
      
      .onItem().transformToMulti((ignore) -> Multi.createFrom().items(openProgram.stream()))
      .onItem().transform(open -> close(open))
      .collect().asList().replaceWithVoid()
      
      .onItem().transform((ignore) -> createBundle());
  }
  
  private BundleBuilder createBundle() {
    return bundleBuilder;
  }
  
  private RuntimeLink close(ArtifactLink open) {
    final var name = open.getAst().getBodyType() + "/" + open.getAst().getName();
    final var builder = Optional.ofNullable(artifacts_byName.get(name)).orElse(null);
  
    Objects.requireNonNull(builder, () -> "Can't find program to finalize, name: " + name);
    final var artifact = builder.build();
    final var link = open.accept(artifact);
    bundleBuilder.addProgram(link);
    return link;
  }
  
  private DependencyBuilder validate(DependencyBuilder dep) {
    // start closing dependencies
    final var name = dep.getBodyType() + "/" + dep.getId();
    final var ref = Optional.ofNullable(artifacts_byName.get(name));
    dep.close(ref);
    return dep;
  }
  
}
