package io.resys.hdes.ast.spi.validators;

/*-
 * #%L
 * hdes-ast
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

import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.HdesTree.ServiceTree;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.MappingNode.ObjectMappingDef;
import io.resys.hdes.ast.api.nodes.ServiceNode.CommandInvocation;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServicePromise;
import io.resys.hdes.ast.api.visitors.ServiceBodyVisitor;
import io.resys.hdes.ast.spi.validators.RootNodeValidator.RootNodeErrors;

public class ServiceValidator implements ServiceBodyVisitor<RootNodeErrors, RootNodeErrors> {

  
  @Override
  public RootNodeErrors visitBody(ServiceTree ctx) {
    ImmutableRootNodeErrors.Builder result = ImmutableRootNodeErrors.builder();
    
    
    return result.build();
  }

  @Override
  public RootNodeErrors visitHeaders(Headers node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitHeader(TypeDef node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitHeader(ScalarDef node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitHeader(ObjectDef node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitClassName(InvocationNode invocation, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitMapping(ObjectMappingDef mapping, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitCommandInvocation(CommandInvocation command, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitPromise(ServicePromise promise, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }


}
