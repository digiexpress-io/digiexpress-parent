package io.resys.hdes.interpreter.spi.visitors;

import java.io.Serializable;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.nodes.BodyNode.ContextTypeDef;
import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
import io.resys.hdes.ast.api.nodes.BodyNode.Literal;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicy;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMapping;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MappingRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.ThenRuleRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.WhenRuleRow;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.HdesTree.DecisionTableTree;
import io.resys.hdes.ast.api.visitors.DecisionTableVisitor;
import io.resys.hdes.executor.api.Trace;
import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.executor.spi.beans.ImmutableTrace;
import io.resys.hdes.executor.spi.beans.ImmutableMatched;
import io.resys.hdes.interpreter.api.HdesInterpreter.AcceptsMap;
import io.resys.hdes.interpreter.api.HdesInterpreter.DataAccessNode;
import io.resys.hdes.interpreter.api.HdesInterpreter.InterpratedNode;
import io.resys.hdes.interpreter.api.HdesInterpreter.ReturnsMap;
import io.resys.hdes.interpreter.api.HdesInterpreterException;
import io.resys.hdes.interpreter.api.ImmutableAcceptsMap;
import io.resys.hdes.interpreter.api.ImmutableReturnsMap;
import io.resys.hdes.interpreter.spi.dataaccess.AcceptsDataAccessNode;
import io.resys.hdes.interpreter.spi.visitors.InterpraterExpressionVisitor.LiteralInterpratedNode;

public class InterpraterDecisionTableVisitor implements DecisionTableVisitor<InterpratedNode, TraceEnd> {

  private final InterpraterExpressionVisitor expressionVisitor = new InterpraterExpressionVisitor();
  
  @Value.Immutable
  public interface WhenInterpratedNode extends InterpratedNode {
    boolean getMatched();
  }
  
  @Value.Immutable
  public interface ThenInterpratedNode extends InterpratedNode {
    Map<String, Serializable> getValues();
  }
  
  @Value.Immutable
  public interface HitPolicyInterpratedNode extends InterpratedNode, HdesNode {
    Trace getTrace();
    Map<String, Serializable> getValue();
  }

  @Value.Immutable
  public interface HeadersInterpratedNode extends InterpratedNode {
    @Nullable
    AcceptsMap getAccepts();
    @Nullable
    ReturnsMap getReturns();
  }
  
  @Value.Immutable
  public interface HeaderInterpratedNode extends InterpratedNode {
    @Nullable
    TypeDef getTypeDef();
    @Nullable
    Serializable getValue();
  }
  
  @Override
  public TraceEnd visitBody(DecisionTableTree ctx) {
    final var startTime = System.currentTimeMillis();
    final var body = ctx.getValue();
    final var inputs = visitHeaders(body.getHeaders(), ctx);
    final var start = ImmutableTrace.builder()
      .id("inputs")
      .time(startTime)
      .body(inputs.getAccepts())
      .build();
    
    var next = ctx.next(new AcceptsDataAccessNode(inputs.getAccepts()));
    HitPolicyInterpratedNode hitpolicy = visitHitPolicy(body.getHitPolicy(), next); 
    HeadersInterpratedNode outputs = visitHeaders(body.getHeaders(), next.next(hitpolicy));

    var trace = ImmutableTrace.builder()
      .id(hitpolicy.getTrace().getId())
      .body(hitpolicy.getTrace().getBody())
      .time(hitpolicy.getTrace().getTime())
      .parent(start)
      .build();
    
    return ImmutableTrace.builder()
        .id(body.getId().getValue())
        .body(outputs.getReturns())
        .parent(trace)
        .end();
  }

  @Override
  public HeadersInterpratedNode visitHeaders(Headers node, HdesTree ctx) {
    Optional<HitPolicyInterpratedNode> hitPolicy = ctx.find().node(HitPolicyInterpratedNode.class);
    if(hitPolicy.isPresent()) {
      // all values from hit policy
      var returns = ImmutableReturnsMap.builder().putAllValues(hitPolicy.get().getValue());
      
      node.getReturnDefs().stream()
        .map(h -> visitHeader(h, ctx))
        .filter(h -> h.getTypeDef() != null)
        .forEach(h -> returns.putValues(h.getTypeDef().getName(), h.getValue()));
      
      return ImmutableHeadersInterpratedNode.builder().returns(returns.build()).build();
    }
    
    var accepts = ImmutableAcceptsMap.builder();
    node.getAcceptDefs().stream()
      .map(h -> visitHeader(h, ctx))
      .filter(h -> h.getTypeDef() != null)
      .forEach(h -> accepts.putValues(h.getTypeDef().getName(), h.getValue()));
    return ImmutableHeadersInterpratedNode.builder().accepts(accepts.build()).build();
  }

  @Override
  public HeaderInterpratedNode visitHeader(TypeDef node, HdesTree ctx) {
    if(node instanceof ScalarDef) {
      return visitHeader((ScalarDef) node, ctx);
    }
    return visitHeader((ObjectDef) node, ctx);
  }

  @Override
  public HeaderInterpratedNode visitHeader(ScalarDef node,HdesTree ctx) {
    if(node.getFormula().isPresent()) {
      return visitFormula(node, ctx);
    }
    
    if(node.getContext() == ContextTypeDef.ACCEPTS) {
      DataAccessNode dataAccess = ctx.get().node(DataAccessNode.class);
      Serializable value = dataAccess.get(node.getName());
      if(value == null && node.getRequired()) {
        throw new HdesInterpreterException(
            new StringBuilder("Accepted type: '")
            .append(node.getName()).append("'").append(" is REQUIRED but was null!")
            .toString());
      }
      return ImmutableHeaderInterpratedNode.builder().typeDef(node).value(value).build(); 

    }
    
    return ImmutableHeaderInterpratedNode.builder().typeDef(null).value(null).build();
  }

  @Override
  public HeaderInterpratedNode visitHeader(ObjectDef node, HdesTree ctx) {
    return ImmutableHeaderInterpratedNode.builder().build();
  }

  @Override
  public HeaderInterpratedNode visitFormula(ScalarDef node, HdesTree ctx) {
    LiteralInterpratedNode result = expressionVisitor.visitBody(node.getFormula().get(), ctx.next(node));
    return ImmutableHeaderInterpratedNode.builder().typeDef(node).value(result.getValue()).build(); 
  }

  @Override
  public HitPolicyInterpratedNode visitHitPolicy(HitPolicy node, HdesTree ctx) {
    if(node instanceof HitPolicyFirst) {
      return visitHitPolicyFirst((HitPolicyFirst) node, ctx);
    } else if(node instanceof HitPolicyAll) {
      return visitHitPolicyAll((HitPolicyAll) node, ctx);
    }
    return visitHitPolicyMapping((HitPolicyMapping) node, ctx);
  }

  @Override
  public HitPolicyInterpratedNode visitHitPolicyAll(HitPolicyAll node, HdesTree ctx) {
    final HdesTree next = ctx.next(node);
    final var matches = new ArrayList<Serializable>();
    final var trace = ImmutableMatched.builder();
    
    int rowId = -1;
    for(RuleRow row : node.getRows()) {
      rowId++;
      WhenInterpratedNode when = visitWhenRuleRow(row.getWhen(), next);
      if(!when.getMatched()) {
        continue;
      }
      trace.add(rowId, row.getWhen().getToken().getText());
      ThenInterpratedNode then = visitThenRuleRow(row.getThen(), next);
      matches.add((Serializable) then.getValues());
    }
    
    var result = new HashMap<String, Serializable>();
    result.put("matches", matches);
    return ImmutableHitPolicyInterpratedNode.builder()
        .token(node.getToken())
        .value(result)
        .trace(ImmutableTrace.builder().id("hitPolicy").body(trace.build()).build())
        .build();
  }

  @Override
  public ThenInterpratedNode visitThenRuleRow(ThenRuleRow node, HdesTree ctx) {
    var builder = ImmutableThenInterpratedNode.builder();
    var defs = ctx.get().node(DecisionTableBody.class).getHeaders().getReturnDefs();
    var returns = defs.stream().map(t -> (ScalarDef) t)
        .filter(t -> t.getFormula().isEmpty())
        .collect(Collectors.toList());
    
    var next = ctx.next(node);
    int index = 0;
    for(Literal literal : node.getValues()) {
      var header = returns.get(index++);
      LiteralInterpratedNode value = expressionVisitor.visitLiteral(literal, next.next(header));
      builder.putValues(header.getName(), value.getValue());
    }
    
    return builder.build();
  }
  
  @Override
  public WhenInterpratedNode visitWhenRuleRow(WhenRuleRow node, HdesTree ctx) {
    var builder = ImmutableWhenInterpratedNode.builder();
    var defs = ctx.get().node(DecisionTableBody.class).getHeaders().getAcceptDefs();
    var accepts = defs.stream().map(t -> (ScalarDef) t).collect(Collectors.toList());
    
    var next = ctx.next(node);
    int index = 0;
    for(ExpressionBody exp : node.getValues()) {
      var header = accepts.get(index++);
      LiteralInterpratedNode value = expressionVisitor.visitBody(exp, next.next(header));
      if(!value.as(Boolean.class)) {
        return builder.matched(false).build();
      }
    }
    return builder.matched(true).build();
  }
  
  @Override
  public HitPolicyInterpratedNode visitHitPolicyMapping(HitPolicyMapping node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HitPolicyInterpratedNode visitHitPolicyFirst(HitPolicyFirst node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InterpratedNode visitRuleRow(RuleRow node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InterpratedNode visitMappingRow(MappingRow node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }
}
