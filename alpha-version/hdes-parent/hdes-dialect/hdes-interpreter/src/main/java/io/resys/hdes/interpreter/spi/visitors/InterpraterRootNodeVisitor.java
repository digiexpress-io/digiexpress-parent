package io.resys.hdes.interpreter.spi.visitors;

/*-
 * #%L
 * hdes-interpreter
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

import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.HdesTree.DecisionTableTree;
import io.resys.hdes.ast.api.nodes.HdesTree.FlowTree;
import io.resys.hdes.ast.api.nodes.HdesTree.RootTree;
import io.resys.hdes.ast.api.nodes.HdesTree.ServiceTree;
import io.resys.hdes.ast.api.nodes.RootNode;
import io.resys.hdes.ast.api.visitors.HdesVisitor.RootNodeVisitor;
import io.resys.hdes.ast.spi.ImmutableHdesTree;
import io.resys.hdes.ast.spi.util.Assertions;
import io.resys.hdes.executor.api.Trace;
import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.interpreter.api.HdesAcceptsSupplier;
import io.resys.hdes.interpreter.api.HdesInterpreter.DataAccessNode;
import io.resys.hdes.interpreter.api.HdesInterpreterException;
import io.resys.hdes.interpreter.spi.dataaccess.InputSupplierDataAccessNode;

public class InterpraterRootNodeVisitor implements RootNodeVisitor<Trace, TraceEnd> {

  private final BodyNode main;
  private final DataAccessNode dataAccess;
  
  public InterpraterRootNodeVisitor(BodyNode main, HdesAcceptsSupplier input) {
    super();
    this.main = main;
    this.dataAccess = new InputSupplierDataAccessNode(input);
  }

  @Override
  public TraceEnd visitBody(RootNode root) {
    RootTree ctx = (RootTree) ImmutableHdesTree.builder().value(root).build();
    return visitNode(main, ctx);
  }

  @Override
  public TraceEnd visitNode(BodyNode ast, RootTree ctx) {    
    if (ast instanceof DecisionTableBody) {
      return visitDecisionTable((DecisionTableTree) ctx.next(dataAccess).next((DecisionTableBody) ast));
    } else if (ast instanceof FlowBody) {
      //return visitFlow((FlowContext) ctx.next((FlowBody) ast));
    }
    throw new HdesInterpreterException(new StringBuilder()
        .append("Not implemented body").append(System.lineSeparator()) 
        .append("  - type: ").append(ast.getClass()).append(System.lineSeparator())
        .append("  - ast: ").append(ast).toString());
  }
  @Override
  public TraceEnd visitFlow(FlowTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public TraceEnd visitService(ServiceTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public TraceEnd visitDecisionTable(DecisionTableTree ctx) {
    return new InterpraterDecisionTableVisitor().visitBody(ctx);
  }
  
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private HdesAcceptsSupplier input;
    private BodyNode body;
    private RootNode root;
    
    public Builder input(HdesAcceptsSupplier input) {
      this.input = input;
      return this;
    }
    public Builder root(RootNode root) {
      this.root = root;
      return this;
    }
    public Builder main(BodyNode body) {
      this.body = body;
      return this;
    }
    public TraceEnd build() {
      Assertions.notNull(input, () -> "input can't be null!");
      Assertions.notNull(body, () -> "main can't be null!");
      Assertions.notNull(root, () -> "root can't be null!");
      return new InterpraterRootNodeVisitor(body, input).visitBody(root);
    }
  }
}
