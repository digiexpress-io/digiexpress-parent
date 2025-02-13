package io.resys.hdes.compiler.spi.expressions;

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

import org.immutables.value.Value;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.BodyNode.Literal;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarType;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.ImmutableBodyId;
import io.resys.hdes.ast.api.nodes.ImmutableExpressionBody;
import io.resys.hdes.ast.api.nodes.ImmutableHeaders;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServiceBody;
import io.resys.hdes.ast.api.visitors.ExpressionVisitor.InvocationVisitor;
import io.resys.hdes.ast.spi.util.Assertions;
import io.resys.hdes.compiler.spi.expressions.invocation.DecisionTableInvocationVisitor;
import io.resys.hdes.compiler.spi.expressions.invocation.FlowInvocationVisitor;
import io.resys.hdes.compiler.spi.expressions.invocation.ServiceInvocationVisitor;
import io.resys.hdes.compiler.spi.expressions.visitors.GenericExpressionVisitor;

public interface ExpressionFactory {
  
  interface ExpCode {
    boolean getArray();
    CodeBlock getValue();
  }
  
  @Value.Immutable
  interface ExpScalarCode extends ExpCode {
    ScalarType getType();
  }

  @Value.Immutable
  interface ExpObjectCode extends ExpCode {
    ObjectDef getType();
  }
  
  @FunctionalInterface
  interface ExpressionCallback {
    ExpCode visitAny(HdesNode node, HdesTree ctx);
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private ExpressionBody body;
    private HdesTree tree;
    
    public Builder body(ExpressionBody body) {
      this.body = body;
      return this;
    }
    public Builder body(Literal body) {
      this.body = ImmutableExpressionBody.builder()
          .token(body.getToken()).value(body)
          .src(body.getToken().getText())
          .headers(ImmutableHeaders.builder().token(body.getToken()).build())
          .id(ImmutableBodyId.builder().token(body.getToken()).value("").build())
          .build();
      return this;
    }
    public Builder body(InvocationNode body) {
      this.body = ImmutableExpressionBody.builder()
          .token(body.getToken()).value(body)
          .src(body.getToken().getText())
          .headers(ImmutableHeaders.builder().token(body.getToken()).build())
          .id(ImmutableBodyId.builder().token(body.getToken()).value("").build())
          .build();
      return this;
    }
    
    public Builder tree(HdesTree tree) {
      this.tree = tree;
      return this;
    }
    public ExpScalarCode build() {
      Assertions.notNull(body, () -> "body side can't be null!");
      Assertions.notNull(tree, () -> "tree side can't be null!");
      BodyNode closestBody = tree.get().body();
      
      InvocationVisitor<ExpCode, ExpCode> invocation;
      if(closestBody instanceof DecisionTableBody) {
        invocation = new DecisionTableInvocationVisitor();
      } else if(closestBody instanceof ServiceBody) {
        invocation = new ServiceInvocationVisitor();
      } else if(closestBody instanceof FlowBody) {
        invocation = new FlowInvocationVisitor();
      } else {
        throw new HdesException("Unknown body node: " + body.getClass() + "!");
      }
      
      return new GenericExpressionVisitor(invocation).visitBody(body, tree);
    }
  }
}
