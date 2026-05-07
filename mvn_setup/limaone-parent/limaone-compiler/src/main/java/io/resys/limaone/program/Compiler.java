package io.resys.limaone.program;

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
import java.util.function.Consumer;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.model.Model.ModelWorld;
import jakarta.annotation.Nullable;

public interface Compiler {  
  AST_Parser getParser();
  BundleBuilder compile(ModelWorld world);
  
  interface BundleBuilder {
    // open for user definition
    BundleBuilder id(String id);
    BundleBuilder name(String name);
    BundleBuilder created(OffsetDateTime created);
    
    BundleBuilder externalId(@Nullable String externalId);
    BundleBuilder startDate(@Nullable OffsetDateTime startDate);
    BundleBuilder endDate(@Nullable OffsetDateTime endDate);
    
    // force to internal cache, makes the bundle by default visible to dependencies
    BundleBuilder cacheKey(@Nullable String cacheKey); 
    
    // end result
    io.resys.limaone.program.Runtime build();
    
    // injected later to runtime
    BundleConstructor buildConstructor();
  }
  
  
  @FunctionalInterface
  interface BundleConstructor {
    Bundle accept(io.resys.limaone.program.Runtime runtime);
  }
  
  interface Bundle {
    String getId();
    String getName();
    String getExternalId();
    
    OffsetDateTime getCreated();
    OffsetDateTime getStartDate();
    OffsetDateTime getEndDate();

    Bundle withCacheless();
    BundleQuery<DialobProgram> queryDialobs();
    BundleQuery<WorkflowProgram> queryWorkflows();
    BundleQuery<ArticleProgram> queryArticles();
    BundleQuery<FlowTaskProgram> queryFlowTasks();
    BundleQuery<FlowProgram> queryFlows();
    BundleQuery<DecisionProgram> queryDecisions();
    BundleQuery<TagomiProgram> queryTagomis();
    
    
    default Optional<? extends Program> findAnyProgram(String id) {
      final var flow = queryFlows().id(id).findOne();
      if(flow.isPresent()) {
        return flow;
      }
      final var dt = queryDecisions().id(id).findOne();
      if(dt.isPresent()) {
        return dt;
      }
      final var ft = queryFlowTasks().id(id).findOne();
      if(ft.isPresent()) {
        return ft;
      }
      final var d = queryDialobs().id(id).findOne();
      if(d.isPresent()) {
        return d;
      }
      final var w = queryWorkflows().id(id).findOne();
      if(w.isPresent()) {
        return w;
      }
      final var t = queryTagomis().id(id).findOne();
      if(t.isPresent()) {
        return t;
      }
      return Optional.empty();
    }
  }
  
  
  interface BundleQuery<T> {
    BundleQuery<T> locale(String locale);
    BundleQuery<T> name(String name);
    BundleQuery<T> externalId(String externalId);
    BundleQuery<T> id(String id);
    
    Optional<T> findOne();
    void forEach(Consumer<T> t);
    T getOne();
  }
}
