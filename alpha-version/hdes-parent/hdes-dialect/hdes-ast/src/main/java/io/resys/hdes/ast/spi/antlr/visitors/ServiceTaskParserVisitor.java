package io.resys.hdes.ast.spi.antlr.visitors;

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

import io.resys.hdes.ast.HdesParser.ExternalServiceContext;
import io.resys.hdes.ast.HdesParser.PromiseContext;
import io.resys.hdes.ast.HdesParser.PromiseTimeoutContext;
import io.resys.hdes.ast.HdesParser.ServiceTaskUnitContext;
import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ImmutableBodyId;
import io.resys.hdes.ast.api.nodes.ImmutableCommandInvocation;
import io.resys.hdes.ast.api.nodes.ImmutableServiceBody;
import io.resys.hdes.ast.api.nodes.ImmutableServicePromise;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.api.nodes.MappingNode.ObjectMappingDef;
import io.resys.hdes.ast.api.nodes.ServiceNode.CommandInvocation;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServiceBody;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServicePromise;
import io.resys.hdes.ast.spi.antlr.util.Nodes;

public class ServiceTaskParserVisitor extends DecisionTableParserVisitor {

  @Override
  public ServiceBody visitServiceTaskUnit(ServiceTaskUnitContext ctx) {
    final Nodes children = nodes(ctx);
    final Headers headers = children.of(Headers.class).get();
    final SimpleInvocation id = children.of(SimpleInvocation.class).get();
    final CommandInvocation command = children.of(CommandInvocation.class).get();
    
    return ImmutableServiceBody.builder()
        .token(children.getToken())
        .id(ImmutableBodyId.builder().token(id.getToken()).value(id.getValue()).build())
        .headers(headers)
        .command(command)
        .build();
  }
  
  @Override
  public CommandInvocation visitExternalService(ExternalServiceContext ctx) {
    final Nodes nodes = nodes(ctx);
    return ImmutableCommandInvocation.builder()
        .token(nodes.getToken())
        .promise(nodes.of(ServicePromise.class))
        .className(nodes.of(InvocationNode.class).get())
        .mapping(nodes.of(ObjectMappingDef.class).get())
        .build();
  }
  
  @Override
  public ServicePromise visitPromise(PromiseContext ctx) {
    final Nodes nodes = nodes(ctx);
    return ImmutableServicePromise.builder()
        .token(nodes.getToken())
        .timeout(nodes.of(ExpressionBody.class))
        .build();
  }

  @Override
  public ExpressionBody visitPromiseTimeout(PromiseTimeoutContext ctx) {
    final Nodes children = nodes(ctx);
    return children.of(ExpressionBody.class).get();
  }
}
