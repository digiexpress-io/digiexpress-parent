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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.HdesTree.DecisionTableTree;
import io.resys.hdes.ast.api.nodes.HdesTree.FlowTree;
import io.resys.hdes.ast.api.nodes.HdesTree.RootTree;
import io.resys.hdes.ast.api.nodes.HdesTree.ServiceTree;
import io.resys.hdes.ast.api.nodes.RootNode;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServiceBody;
import io.resys.hdes.ast.api.visitors.HdesVisitor.RootNodeVisitor;
import io.resys.hdes.ast.spi.ImmutableHdesTree;
import io.resys.hdes.ast.spi.ImmutableRootNode;

public class RootNodeDependencyVisitor implements RootNodeVisitor<RootNode, BodyNode> {
  
  @Override
  public RootNode visitBody(RootNode root) {
    RootTree ctx = (RootTree) ImmutableHdesTree.builder().value(root).build();
    Map<String, BodyNode> body = new HashMap<>();
    root.getBody().entrySet().stream().forEach(s ->
      body.put(s.getKey(), visitNode(s.getValue(), ctx))
    );
    
    return new ImmutableRootNode(Collections.unmodifiableMap(body), root.getOrigin(), root.getErrors());
  }
  
  @Override
  public BodyNode visitService(ServiceTree ctx) {
    return ctx.getValue();
  }
  
  @Override
  public BodyNode visitFlow(FlowTree ctx) {
    return ctx.getValue();
  }

  @Override
  public BodyNode visitDecisionTable(DecisionTableTree ctx) {
    return ctx.getValue();
  }
  
  @Override
  public BodyNode visitNode(BodyNode ast, RootTree ctx) {
    if (ast instanceof DecisionTableBody) {
      return visitDecisionTable((DecisionTableTree) ctx.next((DecisionTableBody) ast));
    } else if (ast instanceof FlowBody) {
      return visitFlow((FlowTree) ctx.next((FlowBody) ast));
    } else if (ast instanceof ServiceBody) {
      return ast;
    }
    throw new HdesException(unknownAst(ast));
  }
  
  private String unknownAst(HdesNode ast) {
    return new StringBuilder()
        .append("Unknown AST: ").append(ast.getClass())
        .append("  - ").append(ast.getClass()).append(System.lineSeparator())
        .append("  supported types are: ").append(System.lineSeparator())
        .append("    - ").append(DecisionTableBody.class).append(System.lineSeparator())
        .append("    - ").append(ServiceBody.class).append(System.lineSeparator())
        .append("    - ").append(FlowBody.class).append(System.lineSeparator())
        .toString();
  }
}
