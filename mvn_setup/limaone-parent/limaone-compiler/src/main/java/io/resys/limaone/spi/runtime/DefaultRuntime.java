package io.resys.limaone.spi.runtime;

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


import java.util.Optional;

import io.resys.limaone.program.Compiler.Bundle;
import io.resys.limaone.program.Compiler.BundleConstructor;
import io.resys.limaone.program.Runtime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class DefaultRuntime implements io.resys.limaone.program.Runtime {
  private static final long serialVersionUID = 119708670216052569L;
  private final EnvironmentProperties envir;
  private final Bundle bundle;

  public DefaultRuntime(EnvironmentProperties envir, BundleConstructor bundle) {
    super();
    this.envir = envir;
    this.bundle = bundle.accept(this);
  }
  @Override
  public EnvironmentProperties getProperties() {
    return envir;
  }
  @Override
  public Heap getHeap() {
    return null;
  }
  @Override
  public Runtime withTenant(Optional<String> tid) {
    if(tid == null || tid.isEmpty()) {
      return this;
    }
    if(this.envir.getDefaultTenantName().equals(tid.get())) {
      return this;
    }
    if(!this.envir.isDev()) {
      log.error("Multi tenant is not supported!");
      return this;
    }
    return new DefaultRuntime(envir, new BundleConstructor_Lazy(envir, tid.get()));
  }
  @Override
  public Bundle getBundle() {
    return bundle;
  }
  public static DefaultRuntime of(EnvironmentProperties envir) {
    return new DefaultRuntime(envir, new BundleConstructor_Lazy(envir));
  }  
  public static DefaultRuntime of(EnvironmentProperties envir, BundleConstructor bundler) {
    return new DefaultRuntime(envir, bundler);
  }

  
  @RequiredArgsConstructor
  private static class BundleConstructor_Lazy implements BundleConstructor {
    private final EnvironmentProperties envir;
    private final String tid;
    public BundleConstructor_Lazy(EnvironmentProperties envir) {
      super();
      this.envir = envir;
      this.tid = null;
    }
    
    @Override
    public Bundle accept(Runtime runtime) {
      return new Bundle_Lazy(envir, tid, runtime);
    }
  }
}
