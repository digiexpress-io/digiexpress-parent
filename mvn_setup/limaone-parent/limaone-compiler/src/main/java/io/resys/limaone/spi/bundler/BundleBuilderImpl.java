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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.resys.limaone.program.Compiler.BundleBuilder;
import io.resys.limaone.program.Compiler.BundleConstructor;
import io.resys.limaone.program.Runtime.EnvironmentProperties;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.Bundle_CacheKey;
import io.resys.limaone.spi.compiler.CompilableUnit.RuntimeLink;
import io.resys.limaone.spi.runtime.DefaultRuntime;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BundleBuilderImpl implements BundleBuilder {

  private final EnvironmentProperties properties;
  private final List<RuntimeLink> programs = new ArrayList<>();
  
  private String id;
  private String name;
  private String externalId;
  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
  private OffsetDateTime created;
  private String cacheKey;

  public BundleBuilderImpl addProgram(RuntimeLink program) {
    this.programs.add(Objects.requireNonNull(program, () -> "program must be defined!"));
    return this;
  }
  
  @Override
  public BundleBuilder id(String id) {
    this.id = Objects.requireNonNull(id, () -> "id must be defined!");
    return this;
  }
  @Override
  public BundleBuilder name(String name) {
    this.name = Objects.requireNonNull(name, () -> "name must be defined!");
    return this;
  }
  @Override
  public BundleBuilder externalId(String externalId) {
    this.externalId = externalId;
    return this;
  }
  @Override
  public BundleBuilder created(OffsetDateTime created) {
    this.created = Objects.requireNonNull(created, () -> "created must be defined!");
    return this;
  }
  @Override
  public BundleBuilder startDate(OffsetDateTime startDate) {
    this.startDate = startDate;
    return this;
  }
  @Override
  public BundleBuilder endDate(OffsetDateTime endDate) {
    this.endDate = endDate;
    return this;
  }
  @Override
  public BundleBuilder cacheKey(String cacheKey) {
    this.cacheKey = cacheKey;
    return this;
  }
  @Override
  public io.resys.limaone.program.Runtime build() {
    return DefaultRuntime.of(properties, buildConstructor());
  }

  @Override
  public BundleConstructor buildConstructor() {
    return (self) -> {
      final var bundle = new ImmutableBundle(
          Objects.requireNonNull(id, () -> "id must be defined!"),
          Optional.ofNullable(name).orElse(id),
          Optional.ofNullable(externalId).orElse(id),
          Optional.ofNullable(created).orElse(OffsetDateTime.now()),
          Optional.ofNullable(startDate).orElse(OffsetDateTime.MIN),
          Optional.ofNullable(endDate).orElse(OffsetDateTime.MAX),
          programs.stream().map(link -> link.accept(self)).toList()
      );
      
      if(cacheKey != null) {
        LocalCache.computeIfAbsent(new Bundle_CacheKey(cacheKey), (key) -> bundle);
      }
      
      return bundle;
    };
  }
}
