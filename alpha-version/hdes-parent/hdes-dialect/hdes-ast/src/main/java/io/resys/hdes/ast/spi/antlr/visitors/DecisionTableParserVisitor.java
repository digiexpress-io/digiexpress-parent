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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.immutables.value.Value;

import io.resys.hdes.ast.HdesParser;
import io.resys.hdes.ast.HdesParser.DecisionTableUnitContext;
import io.resys.hdes.ast.HdesParser.HitPolicyContext;
import io.resys.hdes.ast.HdesParser.MappingFromContext;
import io.resys.hdes.ast.HdesParser.MappingPolicyContext;
import io.resys.hdes.ast.HdesParser.MappingRowContext;
import io.resys.hdes.ast.HdesParser.MappingRowsContext;
import io.resys.hdes.ast.HdesParser.MappingToContext;
import io.resys.hdes.ast.HdesParser.MatchingPolicyContext;
import io.resys.hdes.ast.HdesParser.RuleExpressionContext;
import io.resys.hdes.ast.HdesParser.RuleLiteralContext;
import io.resys.hdes.ast.HdesParser.RuleUndefinedValueContext;
import io.resys.hdes.ast.HdesParser.ThenRulesContext;
import io.resys.hdes.ast.HdesParser.WhenRulesContext;
import io.resys.hdes.ast.HdesParser.WhenThenRulesContext;
import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.BodyNode.ContextTypeDef;
import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
import io.resys.hdes.ast.api.nodes.BodyNode.Literal;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarType;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode;
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
import io.resys.hdes.ast.api.nodes.HdesNode.Token;
import io.resys.hdes.ast.api.nodes.ImmutableBodyId;
import io.resys.hdes.ast.api.nodes.ImmutableDecisionTableBody;
import io.resys.hdes.ast.api.nodes.ImmutableExpressionBody;
import io.resys.hdes.ast.api.nodes.ImmutableHeaders;
import io.resys.hdes.ast.api.nodes.ImmutableHitPolicyAll;
import io.resys.hdes.ast.api.nodes.ImmutableHitPolicyFirst;
import io.resys.hdes.ast.api.nodes.ImmutableHitPolicyMapping;
import io.resys.hdes.ast.api.nodes.ImmutableLiteral;
import io.resys.hdes.ast.api.nodes.ImmutableMappingRow;
import io.resys.hdes.ast.api.nodes.ImmutableObjectDef;
import io.resys.hdes.ast.api.nodes.ImmutableRuleRow;
import io.resys.hdes.ast.api.nodes.ImmutableScalarDef;
import io.resys.hdes.ast.api.nodes.ImmutableThenRuleRow;
import io.resys.hdes.ast.api.nodes.ImmutableWhenRuleRow;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.spi.antlr.util.Nodes;
import io.resys.hdes.ast.spi.antlr.visitors.HdesParserVisitor.RedundentScalarType;

public class DecisionTableParserVisitor extends ExpressionParserVisitor {

  @Value.Immutable
  public interface DtRedundentMappingRows extends DecisionTableNode {
    List<MappingRow> getValues();
  }  

  @Value.Immutable
  public interface DtRedundentMappingFrom extends DecisionTableNode {
    BodyNode.ScalarType getValue();
  }  
  
  @Value.Immutable
  public interface DtRedundentMappingTo extends DecisionTableNode {
    BodyNode.ScalarType getValue();
  }  
  
  @Override
  public DecisionTableBody visitDecisionTableUnit(DecisionTableUnitContext ctx) {
    final Nodes children = nodes(ctx);
    final Headers headers = children.of(Headers.class).get();
    
    final HitPolicy hitPolicy = children.of(HitPolicy.class).get();
    final SimpleInvocation id = children.of(SimpleInvocation.class).get();
    final ObjectDef statics = statics(hitPolicy, headers);
    final ObjectDef instance = instance(hitPolicy, headers);
    
    return ImmutableDecisionTableBody.builder()
        .token(children.getToken())
        .id(ImmutableBodyId.builder().token(id.getToken()).value(id.getValue()).build())
        .headers(headers)
        .hitPolicy(hitPolicy)
        .constants(statics)
        .matched(instance)
        .build();
  }

  private final ObjectDef statics(HitPolicy hitPolicy, Headers headers) {
    Token token = headers.getToken();
    
    if(hitPolicy instanceof HitPolicyFirst || hitPolicy instanceof HitPolicyAll) {
      
      List<TypeDef> fields = headers.getReturnDefs().stream()
        .map(h -> (ScalarDef) h)
        .filter(h -> h.getFormula().isEmpty())
        .collect(Collectors.toList());
      
      return ImmutableObjectDef.builder()
          .name("constants").token(token)
          .array(true).required(true)
          .addAllValues(fields)
          .context(ContextTypeDef.CONSTANTS)
          .build();
    }
    
    HitPolicyMapping matrix = (HitPolicyMapping) hitPolicy;
    List<TypeDef> staticValues = new ArrayList<>(); 

    // matrix type
    staticValues.add(ImmutableScalarDef.builder()
        .name("").token(token)
        .array(true).required(true)
        .context(ContextTypeDef.CONSTANTS)
        .type(matrix.getDefTo())
        .build());
    
    // matrix row name and value
    for(MappingRow row : matrix.getMapsTo()) {
      staticValues.add(ImmutableScalarDef.builder()
          .name(row.getAccepts().getValue()).token(token)
          .array(true).required(true)
          .context(ContextTypeDef.CONSTANTS)
          .type(matrix.getDefTo())
          .build());
    }
  
    return ImmutableObjectDef.builder()
        .array(true)
        .required(true)
        .addAllValues(staticValues)
        .name("constants")
        .token(token)
        .context(ContextTypeDef.CONSTANTS)
        .build();
  }
  
  private ObjectDef instance(HitPolicy hitPolicy, Headers headers) {
    Token token = headers.getToken();
    if(hitPolicy instanceof HitPolicyFirst || hitPolicy instanceof HitPolicyAll) {
      
      List<TypeDef> fields = headers.getReturnDefs().stream()
          .map(h -> (ScalarDef) h)
          .collect(Collectors.toList());
      
      return ImmutableObjectDef.builder()
          .array(hitPolicy instanceof HitPolicyAll).required(true)
          .addAllValues(fields)
          .context(ContextTypeDef.MATCHES)
          .name("").token(token)
          .build();
    }
    
    HitPolicyMapping matrix = (HitPolicyMapping) hitPolicy;
    List<TypeDef> fields = new ArrayList<>();

    // matrix type
    fields.add(ImmutableObjectDef.builder()
      .name("instance").token(token)
      .array(true).required(true)
      .context(ContextTypeDef.MATCHES)
      .addValues(ImmutableScalarDef.builder()
          .name("").token(token)
          .context(ContextTypeDef.MATCHES)
          .array(true).required(true)
          .type(matrix.getDefTo())
          .build())
      .build());
    
    for(MappingRow row : matrix.getMapsTo()) {
      fields.add(ImmutableScalarDef.builder()
          .name(row.getAccepts().getValue()).token(token)
          .array(true).required(true)
          .context(ContextTypeDef.MATCHES)
          .type(matrix.getDefTo())
          .build());
    }
  
    return ImmutableObjectDef.builder()
        .array(false).required(true)
        .addAllValues(fields)
        .context(ContextTypeDef.MATCHES)
        .name("instance").token(token).build();
    
  }

  @Override
  public HitPolicy visitHitPolicy(HitPolicyContext ctx) {
    return nodes(ctx).of(HitPolicy.class).get();
  }
  
  @Override
  public HitPolicy visitMatchingPolicy(MatchingPolicyContext ctx) {
    Nodes nodes = nodes(ctx);
    List<RuleRow> rows = nodes.list(RuleRow.class);
    TerminalNode v = (TerminalNode) ctx.getChild(0);
    
    if(v.getSymbol().getType() == HdesParser.FIND_FIRST) {
      return ImmutableHitPolicyFirst.builder().token(nodes.getToken()).rows(rows).build();
    }
    return ImmutableHitPolicyAll.builder().token(nodes.getToken()).rows(rows).build();
  }
  
  @Override
  public HdesNode visitWhenThenRules(WhenThenRulesContext ctx) {
    Nodes nodes = nodes(ctx);
    String text = ctx.getStart().getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
    
    return ImmutableRuleRow.builder().token(nodes.getToken()).text(text)
        .when(nodes.of(WhenRuleRow.class).get())
        .then(nodes.of(ThenRuleRow.class).get())
        .build();
  }
    
  @Override
  public WhenRuleRow visitWhenRules(WhenRulesContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableWhenRuleRow.builder().token(nodes.getToken()).values(nodes.list(ExpressionBody.class)).build();
  }

  @Override
  public ExpressionBody visitRuleExpression(RuleExpressionContext ctx) {
    return (ExpressionBody) first(ctx);
  }
  
  @Override
  public ExpressionBody visitRuleUndefinedValue(RuleUndefinedValueContext ctx) {
    Token token = token(ctx);
    return ImmutableExpressionBody.builder().token(token)
        .headers(ImmutableHeaders.builder().token(token).build())
        .id(ImmutableBodyId.builder().token(token).value("MatchAny").build())
        .src("true")
        .value(ImmutableLiteral.builder().token(token).type(ScalarType.BOOLEAN).value("true").build())
        .build();
  }
  
  @Override
  public ThenRuleRow visitThenRules(ThenRulesContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableThenRuleRow.builder().token(nodes.getToken()).values(nodes.list(Literal.class)).build();
  }
  
  @Override
  public Literal visitRuleLiteral(RuleLiteralContext ctx) {
    HdesNode node = first(ctx);
    return (Literal) node;
  }

  @Override
  public HitPolicyMapping visitMappingPolicy(MappingPolicyContext ctx) {
    Nodes nodes = nodes(ctx);
    BodyNode.ScalarType from = nodes.of(DtRedundentMappingFrom.class).get().getValue();
    BodyNode.ScalarType to = nodes.of(DtRedundentMappingTo.class).get().getValue();
    
    WhenRuleRow when = nodes.of(WhenRuleRow.class).get();
    DtRedundentMappingRows mapping = nodes.of(DtRedundentMappingRows.class).get();
    
    return ImmutableHitPolicyMapping.builder()
        .token(token(ctx))
        .defFrom(from).defTo(to)
        .when(when)
        .mapsTo(mapping.getValues())
        .build();
  }
  
  @Override
  public DtRedundentMappingFrom visitMappingFrom(MappingFromContext ctx) {
    RedundentScalarType scalar = (RedundentScalarType) first(ctx);
    return ImmutableDtRedundentMappingFrom.builder().token(scalar.getToken()).value(scalar.getValue()).build();
    
  }
  
  @Override
  public DtRedundentMappingTo visitMappingTo(MappingToContext ctx) {
    RedundentScalarType scalar = (RedundentScalarType) first(ctx);
    return ImmutableDtRedundentMappingTo.builder().token(scalar.getToken()).value(scalar.getValue()).build();
  }
  

  @Override
  public DtRedundentMappingRows visitMappingRows(MappingRowsContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableDtRedundentMappingRows.builder()
        .token(nodes.getToken())
        .values(nodes.list(MappingRow.class))
        .build();
  }
  
  @Override
  public MappingRow visitMappingRow(MappingRowContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableMappingRow.builder()
        .token(nodes.getToken())
        .accepts(nodes.of(SimpleInvocation.class).get())
        .then(nodes.of(ThenRuleRow.class).get())
        .build();
  }
}
