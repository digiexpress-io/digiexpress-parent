package io.resys.hdes.client.spi.config;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import io.resys.hdes.client.api.HdesCache;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowRoot;
import io.resys.hdes.client.api.ast.ImmutableAstFlow;

import java.util.List;
import java.util.Optional;

public interface HdesClientConfig {

  @FunctionalInterface
  interface ServiceInit {
    <T> T get(Class<T> type);
  }

  @FunctionalInterface
  public interface DependencyInjectionContext {
    <T> T get(Class<T> type);
  }
  
  DependencyInjectionContext getDependencyInjectionContext();
  ServiceInit getServiceInit();
  HdesCache getCache();
  List<AstFlowNodeVisitor> getFlowVisitors();
  HdesClientConfig config(AstFlowNodeVisitor ... changes);
  HdesClientConfig withBranch(String branchName);
  Optional<String> getBranchName();
  
  interface AstFlowNodeVisitor {
    void visit(AstFlowRoot node, ImmutableAstFlow.Builder nodesBuilder);
  }
}
