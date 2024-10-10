package io.resys.hdes.ast.api.visitors;

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
import io.resys.hdes.ast.api.nodes.MappingNode.ExpressionMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.FastMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.FieldMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.MappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.ObjectMappingDef;
import io.resys.hdes.ast.api.nodes.ServiceNode.CommandInvocation;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServicePromise;

public interface ServiceBodyVisitor<T, R> {
  R visitBody(ServiceTree ctx); 
  T visitHeaders(Headers node, HdesTree ctx);
  T visitHeader(TypeDef node, HdesTree ctx);
  T visitHeader(ScalarDef node, HdesTree ctx);
  T visitHeader(ObjectDef node, HdesTree ctx);
  
  T visitCommandInvocation(CommandInvocation command, HdesTree ctx);
  T visitPromise(ServicePromise promise, HdesTree ctx);
  T visitClassName(InvocationNode invocation, HdesTree ctx);
  T visitMapping(ObjectMappingDef mapping, HdesTree ctx);

  
  interface ServiceMappingDefVisitor<T, R> {
    R visitBody(ObjectMappingDef mapping, HdesTree ctx);
    T visitMappingDef(MappingDef node, HdesTree ctx);
    T visitExpressionMappingDef(ExpressionMappingDef node, HdesTree ctx);
    T visitFastMappingDef(FastMappingDef node, HdesTree ctx);
    T visitFieldMappingDef(FieldMappingDef node, HdesTree ctx);
    T visitObjectMappingDef(ObjectMappingDef node, HdesTree ctx);
  }
}
