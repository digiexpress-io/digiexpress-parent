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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.nodes.BodyNode.Literal;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleRow;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.InvocationNode.EmptyPlaceholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.NamedPlaceholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.NestedInvocation;
import io.resys.hdes.ast.api.nodes.InvocationNode.Placeholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.api.visitors.ExpressionVisitor.InvocationVisitor;
import io.resys.hdes.interpreter.api.HdesInterpreter.DataAccessNode;
import io.resys.hdes.interpreter.api.HdesInterpreter.InterpratedNode;
import io.resys.hdes.interpreter.api.HdesInterpreterException;
import io.resys.hdes.interpreter.spi.dataaccess.AcceptsDataAccessNode;
import io.resys.hdes.interpreter.spi.dataaccess.LambdaParamAccessNode;
import io.resys.hdes.interpreter.spi.dataaccess.NestedParamAccessNode;
import io.resys.hdes.interpreter.spi.dataaccess.ReturnsDataAccessNode;
import io.resys.hdes.interpreter.spi.visitors.InterpraterInvocationVisitor.InvocationInterpratedNode;

public class InterpraterInvocationVisitor implements InvocationVisitor<InvocationInterpratedNode, InvocationInterpratedNode> {

  private final InterpraterExpressionVisitor expressionVisitor;
  
  public InterpraterInvocationVisitor(InterpraterExpressionVisitor expressionVisitor) {
    super();
    this.expressionVisitor = expressionVisitor;
  }

  @Value.Immutable
  public interface InvocationInterpratedNode extends InterpratedNode {
    Serializable getValue();
  }

  @Override
  public InvocationInterpratedNode visitBody(InvocationNode node, HdesTree ctx) {
    if(node instanceof NestedInvocation) {
      return visitNested((NestedInvocation) node, ctx);
    } else if(node instanceof Placeholder) {
      return visitPlaceholder((Placeholder) node, ctx);
    } else if(node instanceof SimpleInvocation) {
      return visitSimple((SimpleInvocation) node, ctx);
    }
    
    throw new HdesInterpreterException(new StringBuilder()
        .append("Not implemented body").append(System.lineSeparator()) 
        .append("  - type: ").append(node.getClass()).append(System.lineSeparator())
        .append("  - node: ").append(node).toString());
  }
  
  @Override
  public InvocationInterpratedNode visitPlaceholder(Placeholder node, HdesTree ctx) {
    if(node instanceof EmptyPlaceholder) {
      return visitEmptyPlaceholder((EmptyPlaceholder) node, ctx);
    }
    return visitNamedPlaceholder((NamedPlaceholder) node, ctx);
  }

  @Override
  public InvocationInterpratedNode visitNamedPlaceholder(NamedPlaceholder node, HdesTree ctx) {
    if(node.getValue().equals("constants")) {
      final var next = ctx.next(node); 
      DecisionTableBody body = ctx.get().node(DecisionTableBody.class);
      List<ScalarDef> returns = body.getHeaders().getReturnDefs().stream()
        .map(r -> (ScalarDef) r)
        .filter(r -> r.getFormula().isEmpty())
        .collect(Collectors.toList());
      
      if(body.getHitPolicy() instanceof HitPolicyAll) {
        HitPolicyAll all = (HitPolicyAll) body.getHitPolicy();
        return ImmutableInvocationInterpratedNode.builder()
            .value((Serializable) toThenEntity(returns, all.getRows(), next))
            .build();
      } else if(body.getHitPolicy() instanceof HitPolicyFirst) {
        HitPolicyFirst first = (HitPolicyFirst) body.getHitPolicy();
        return ImmutableInvocationInterpratedNode.builder()
            .value((Serializable) toThenEntity(returns, first.getRows(), next))
            .build();
      }
    }
    
    throw new HdesInterpreterException(new StringBuilder()
        .append("Not implemented body").append(System.lineSeparator()) 
        .append("  - type: ").append(node.getClass()).append(System.lineSeparator())
        .append("  - node: ").append(node).toString());
  }
  
  private List<Map<String, Serializable>> toThenEntity(List<ScalarDef> returns, List<RuleRow> rules, HdesTree next) {
    List<Map<String, Serializable>> values = new ArrayList<>();
    for(RuleRow row : rules) {
      Map<String, Serializable> entity = new HashMap<>();
      
      int pos = 0;
      for(Literal literal : row.getThen().getValues()) {
        Serializable value = expressionVisitor.visitLiteral(literal, next).getValue();
        entity.put(returns.get(pos++).getName(), value);
      }
      values.add(entity);
    }
    return values; 
  }
  
  @Override
  public InvocationInterpratedNode visitSimple(SimpleInvocation node, HdesTree ctx) {
    String name = node.getValue();
    Function<Class<? extends DataAccessNode>, Optional<Serializable>> access = (type) -> ctx.find().node(type).map(t -> t.get(name));
    
    Optional<Serializable> nested = access.apply(NestedParamAccessNode.class);
    if(nested.isPresent()) {
      return ImmutableInvocationInterpratedNode.builder().value(nested.get()).build();
    }

    Optional<Serializable> lambda = access.apply(LambdaParamAccessNode.class);
    if(lambda.isPresent()) {
      return ImmutableInvocationInterpratedNode.builder().value(lambda.get()).build();
    }
    
    Optional<Serializable> returns = access.apply(ReturnsDataAccessNode.class);
    if(returns.isPresent()) {
      return ImmutableInvocationInterpratedNode.builder().value(returns.get()).build();
    }    
    
    Optional<Serializable> accepts = access.apply(AcceptsDataAccessNode.class);
    if(accepts.isPresent()) {
      return ImmutableInvocationInterpratedNode.builder().value(accepts.get()).build();      
    }
    
    DataAccessNode dataAccess = ctx.get().node(DataAccessNode.class);
    Serializable value = dataAccess.get(node.getValue());
    return ImmutableInvocationInterpratedNode.builder().value(value).build();
  }

  @SuppressWarnings("unchecked")
  @Override
  public InvocationInterpratedNode visitNested(NestedInvocation node, HdesTree ctx) {
    InvocationInterpratedNode path = visitBody(node.getPath(), ctx.next(node));
    NestedParamAccessNode nested = NestedParamAccessNode.create((Map<String, Serializable>) path.getValue());
    return visitBody(node.getValue(), ctx.next(nested));
  }

  @Override
  public InvocationInterpratedNode visitEmptyPlaceholder(EmptyPlaceholder node, HdesTree ctx) {
    ScalarDef header = ctx.get().node(ScalarDef.class);
    DataAccessNode dataAccess = ctx.get().node(AcceptsDataAccessNode.class);
    
    Serializable value = dataAccess.get(header.getName());
    if(value == null) {
      throw new HdesInterpreterException(new StringBuilder()
          .append("Expected non null value for:").append(System.lineSeparator()) 
          .append("  - type: ").append(node.getClass()).append(System.lineSeparator())
          .append("  - node: ").append(node).toString());
    }
    return ImmutableInvocationInterpratedNode.builder().value(value).build();
  }
}
