package io.resys.hdes.compiler.api;

/*-
 * #%L
 * hdes-compiler
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import java.util.List;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.nodes.BodyNode;

public interface HdesCompiler {

  ResourceParser parser();
  
  interface ResourceParser {
    ResourceParser add(String name, String src);
    List<Resource> build();
  }

  enum ResourceType { ST, FL, DT }
  
  @Value.Immutable
  interface Resource {
    String getName();
    ResourceType getType();
    String getSource();
    
    BodyNode getAst();
    ResourceName getAccepts();
    ResourceName getReturns();
    ResourceName getEnds();
    
    List<ResourceName> getTypes();
    List<ResourceDeclaration> getDeclarations();
  }
  
  @Value.Immutable
  interface ResourceDeclaration {
    ResourceName getType();
    String getValue();
    
    // Is main executable
    boolean isExecutable(); 
  }
  
  @Value.Immutable
  interface ResourceName {
    String getPkg();
    String getName();
  }
}
