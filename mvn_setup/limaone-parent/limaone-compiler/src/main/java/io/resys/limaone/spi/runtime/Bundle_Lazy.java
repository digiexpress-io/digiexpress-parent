package io.resys.limaone.spi.runtime;

import java.time.Duration;

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
import java.util.Optional;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.resys.limaone.model.ImmutableModelWorld;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.program.ArticleProgram;
import io.resys.limaone.program.Compiler.Bundle;
import io.resys.limaone.program.Compiler.BundleQuery;
import io.resys.limaone.program.DecisionProgram;
import io.resys.limaone.program.DialobProgram;
import io.resys.limaone.program.FlowProgram;
import io.resys.limaone.program.FlowTaskProgram;
import io.resys.limaone.program.Runtime;
import io.resys.limaone.program.Runtime.EnvironmentProperties;
import io.resys.limaone.program.TagomiProgram;
import io.resys.limaone.program.WorkflowProgram;
import io.resys.limaone.spi.compiler.CompilerImpl;



public class Bundle_Lazy implements Bundle {
  private final Cache<String, String> debounce;
  private final EnvironmentProperties envir;
  private final String tid;
  private final Runtime runtime;
  private Bundle bundle;
  private String bundleHash;
  

  
  
  public Bundle_Lazy(EnvironmentProperties envir, String tid, Runtime runtime) {
    this(envir, tid, runtime, 30);
  }
  public Bundle_Lazy(EnvironmentProperties envir, String tid, Runtime runtime, int seconds) {
    super();
    this.envir = envir;
    this.tid = tid;
    this.runtime = runtime;
    this.debounce = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(seconds))
            .build();
  }
  
  // delegate-s
  @Override public String getId() { return await().getId(); }
  @Override public String getName() { return await().getName(); }
  @Override public String getExternalId() { return await().getExternalId(); }
  @Override public OffsetDateTime getCreated() { return await().getCreated(); }
  @Override public OffsetDateTime getStartDate() { return await().getStartDate(); }
  @Override public OffsetDateTime getEndDate() { return await().getEndDate(); }
  @Override public Bundle withCacheless() { return new Bundle_Lazy(envir, tid, runtime, 2); }
  @Override public BundleQuery<DialobProgram> queryDialobs() { return await().queryDialobs(); }
  @Override public BundleQuery<WorkflowProgram> queryWorkflows() { return await().queryWorkflows(); }
  @Override public BundleQuery<ArticleProgram> queryArticles() { return await().queryArticles(); }
  @Override public BundleQuery<FlowTaskProgram> queryFlowTasks() { return await().queryFlowTasks(); }
  @Override public BundleQuery<FlowProgram> queryFlows() { return await().queryFlows(); }
  @Override public BundleQuery<DecisionProgram> queryDecisions() { return await().queryDecisions(); }
  @Override public BundleQuery<TagomiProgram> queryTagomis() { return await().queryTagomis(); }
  
  public Bundle await() {
    if(debounce.asMap().containsKey(getTid())) {
      return bundle;
    }
    debounce.put(getTid(), getTid());

    // blocking op... goes to db 
    final var tenant = envir.getTenantDb().getTenantByAnything(tid);    
    final var modelDb = envir.getModelDb().withTenant(Optional.ofNullable(tenant));
    final var ref = modelDb.worldRefQuery().findOneSync();
    
    if(ref.isEmpty()) {
      final var constructor = new CompilerImpl(envir)
          .compile(ImmutableModelWorld.builder()
              .name("empty-world")
              .build())
          .id("empty")
          .buildConstructor();
      this.bundle = constructor.accept(runtime);
      return bundle;
    }
    
    
    final var newHash = ref.get().getHash();
    if(newHash.equals(bundleHash)) {
      return bundle;
    }
    
    final var world = modelDb
        .worldQuery()
        .docs(BodyType.without(BodyType.DEPLOYMENT))
        .findAllSync();
    
    final var id = world.getName();
    final var constructor = new CompilerImpl(envir).compile(world).id(id).buildConstructor();
    this.bundle = constructor.accept(runtime);
    this.bundleHash = newHash;
    return this.bundle;
  }
  
  private String getTid() {
    return this.tid == null ? "" : this.tid;
  }
}
