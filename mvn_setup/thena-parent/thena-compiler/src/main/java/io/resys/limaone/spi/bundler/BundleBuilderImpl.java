package io.resys.limaone.spi.bundler;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.resys.limaone.program.Compiler.Bundle;
import io.resys.limaone.program.Compiler.BundleBuilder;
import io.resys.limaone.program.Program;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.Bundle_CacheKey;

public class BundleBuilderImpl implements BundleBuilder {

  private String id;
  private String name;
  private String externalId;
  private OffsetDateTime startDate;
  private OffsetDateTime endDate;
  private OffsetDateTime created;
  private String cacheKey;
  private final List<Program> programs = new ArrayList<>();
  
  
  
  public BundleBuilderImpl addProgram(Program program) {
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
  public Bundle build() {
    final var bundle = new ImmutableBundle(
        Objects.requireNonNull(id, () -> "id must be defined!"),
        Optional.ofNullable(name).orElse(id),
        Optional.ofNullable(externalId).orElse(id),
        Optional.ofNullable(created).orElse(OffsetDateTime.now()),
        Optional.ofNullable(startDate).orElse(OffsetDateTime.MIN),
        Optional.ofNullable(endDate).orElse(OffsetDateTime.MAX),
        programs
    );
    
    if(cacheKey != null) {
      LocalCache.computeIfAbsent(new Bundle_CacheKey(cacheKey), (key) -> bundle);
    }
    return bundle;
  }


}
