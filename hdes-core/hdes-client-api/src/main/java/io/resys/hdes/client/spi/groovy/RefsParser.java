package io.resys.hdes.client.spi.groovy;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;

import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstService.AstServiceRef;
import io.resys.hdes.client.api.ast.ImmutableAstServiceRef;

public class RefsParser {
  private final ClassNode classNode;
  private final List<AstServiceRef> refs = new ArrayList<>();

  public RefsParser(ClassNode classNode) {
    super();
    this.classNode = classNode;
  }

  public List<AstServiceRef> visit() {
    for (org.codehaus.groovy.ast.MethodNode method : classNode.getMethods()) {
      final var code = method.getCode();
      visitStatement(code);
    }
    return refs;
  }

  private void visitStatement(Statement statement) {
    if (statement instanceof BlockStatement) {
      for(final var nested : ((BlockStatement) statement).getStatements()) {
        visitStatement(nested);  
      }
    } else if(statement instanceof ExpressionStatement) {
      visitExpression(((ExpressionStatement) statement).getExpression());
    } else if(statement instanceof ReturnStatement) {
      final ReturnStatement orig = (ReturnStatement) statement;
      visitExpression(orig.getExpression());
    } else if(statement instanceof IfStatement) {
      final IfStatement orig = (IfStatement) statement;
      visitStatement(orig.getIfBlock());
      visitStatement(orig.getElseBlock());
    } else if(statement instanceof TryCatchStatement) {
      TryCatchStatement orig = (TryCatchStatement) statement;
      visitStatement(orig.getTryStatement());
    } else if(statement instanceof ForStatement) {
      ForStatement orig = (ForStatement) statement;
      visitStatement(orig.getLoopBlock());
    } else if(statement instanceof EmptyStatement) {
    } else if(statement instanceof ContinueStatement) {
    }
  }
  
  private void visitExpression(Expression expression) {
    try {
      final var code = expression.getText();
      // lazy way
      final var executor = code.indexOf(".executor()") > -1;
      if(!executor) {
        return;
      }
      
      final var flow = code.indexOf(".flow(");
      final var decision = code.indexOf(".decision(");
      final var service = code.indexOf(".service(");

      if(flow > -1) {
        final var ref = parseValue(code, flow);
        refs.add(ImmutableAstServiceRef.builder()
            .refValue(ref)
            .bodyType(AstBodyType.FLOW)
            .build());
      }
      if(decision > -1) {
        final var ref = parseValue(code, decision);
        refs.add(ImmutableAstServiceRef.builder()
            .refValue(ref)
            .bodyType(AstBodyType.DT)
            .build());
      }
      if(service > -1) {
        final var ref = parseValue(code, service);
        refs.add(ImmutableAstServiceRef.builder()
            .refValue(ref)
            .bodyType(AstBodyType.FLOW_TASK)
            .build());
      }
    } catch(Exception e) {}
  }
  

  
  private String parseValue(String code, int index) {
    final var start = code.substring(index);
    return start.substring(start.indexOf("(") + 1, start.indexOf(")"));
  }
}
