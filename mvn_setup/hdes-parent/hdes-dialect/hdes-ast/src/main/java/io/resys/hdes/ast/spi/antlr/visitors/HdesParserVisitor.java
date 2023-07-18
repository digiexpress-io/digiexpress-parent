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
import java.util.List;
import java.util.Optional;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.immutables.value.Value;

import io.resys.hdes.ast.HdesParser;
import io.resys.hdes.ast.HdesParser.ArrayTypeContext;
import io.resys.hdes.ast.HdesParser.DebugValueContext;
import io.resys.hdes.ast.HdesParser.FormulaContext;
import io.resys.hdes.ast.HdesParser.FormulaOverAllContext;
import io.resys.hdes.ast.HdesParser.HdesBodyContext;
import io.resys.hdes.ast.HdesParser.HdesContentContext;
import io.resys.hdes.ast.HdesParser.HeadersAcceptsContext;
import io.resys.hdes.ast.HdesParser.HeadersContext;
import io.resys.hdes.ast.HdesParser.HeadersReturnsContext;
import io.resys.hdes.ast.HdesParser.LiteralContext;
import io.resys.hdes.ast.HdesParser.ObjectTypeContext;
import io.resys.hdes.ast.HdesParser.OptionalContext;
import io.resys.hdes.ast.HdesParser.PlaceholderRuleContext;
import io.resys.hdes.ast.HdesParser.PlaceholderTypeNameContext;
import io.resys.hdes.ast.HdesParser.ScalarTypeContext;
import io.resys.hdes.ast.HdesParser.SimpleTypeContext;
import io.resys.hdes.ast.HdesParser.SimpleTypeNameContext;
import io.resys.hdes.ast.HdesParser.TypeDefContext;
import io.resys.hdes.ast.HdesParser.TypeDefNamesContext;
import io.resys.hdes.ast.HdesParser.TypeDefsContext;
import io.resys.hdes.ast.HdesParser.TypeNameContext;
import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.BodyNode.ContextTypeDef;
import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
import io.resys.hdes.ast.api.nodes.BodyNode.Literal;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarType;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.ImmutableEmptyPlaceholder;
import io.resys.hdes.ast.api.nodes.ImmutableHeaders;
import io.resys.hdes.ast.api.nodes.ImmutableLiteral;
import io.resys.hdes.ast.api.nodes.ImmutableNamedPlaceholder;
import io.resys.hdes.ast.api.nodes.ImmutableNestedInvocation;
import io.resys.hdes.ast.api.nodes.ImmutableObjectDef;
import io.resys.hdes.ast.api.nodes.ImmutableScalarDef;
import io.resys.hdes.ast.api.nodes.ImmutableSimpleInvocation;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.InvocationNode.Placeholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.spi.antlr.util.Nodes;


public class HdesParserVisitor extends FlowParserVisitor {

  // Internal only
  @Value.Immutable
  public interface RedundentScalarType extends HdesNode {
    ScalarType getValue();
  }
  @Value.Immutable
  public interface RedundentDebugValue extends HdesNode {
    String getValue();
  }
  @Value.Immutable
  public interface RedundentFormula extends HdesNode {
    ExpressionBody getValue();
    Boolean getOverAll();
  }
  @Value.Immutable
  public interface RedundentOptional extends HdesNode {
  }
  @Value.Immutable
  public interface RedundentReturns extends HdesNode {
    List<TypeDef> getValues();
  }
  @Value.Immutable
  public interface RedundentAccepts extends HdesNode {
    List<TypeDef> getValues();
  }
  @Value.Immutable
  public interface RedundentTypeDefs extends HdesNode {
    List<TypeDef> getValues();
  }
  @Value.Immutable
  public interface RedundentTypeDefNames extends HdesNode {
    List<RedundentTypeDefName> getValues();
  }
  
  @Value.Immutable
  public interface RedundentTypeDefName extends HdesNode {
    Boolean getRequired();
    String getValue();
  }
  
  @Value.Immutable
  public interface ContentNode extends HdesNode {
    List<BodyNode> getValues();
  }
  
  @Override
  public ContentNode visitHdesContent(HdesContentContext ctx) {
    HdesNode.Token token = token(ctx);
    List<BodyNode> values = nodes(ctx).list(BodyNode.class);
    return ImmutableContentNode.builder().token(token).values(values).build();
  }

  @Override
  public BodyNode visitHdesBody(HdesBodyContext ctx) {
    return nodes(ctx).of(BodyNode.class).get();
  }
  
  @Override
  public HdesNode visitLiteral(LiteralContext ctx) {
    String value = ctx.getText();
    ScalarType type = null;
    TerminalNode terminalNode = (TerminalNode) ctx.getChild(0);
    
    switch (terminalNode.getSymbol().getType()) {
    case HdesParser.StringLiteral:
      type = ScalarType.STRING;
      value = value.substring(1, value.length() - 1);
      break;
    case HdesParser.BooleanLiteral:
      type = ScalarType.BOOLEAN;
      break;
    case HdesParser.DecimalLiteral:
      type = ScalarType.DECIMAL;
      break;
    case HdesParser.IntegerLiteral:
      type = ScalarType.INTEGER;
      value = value.replaceAll("_", "");
      break;
    default: throw new HdesException("Unknown literal: " + ctx.getText() + "!");
    }

    return ImmutableLiteral.builder().token(token(ctx)).type(type).value(value).build();
  }

  @Override
  public HdesNode visitTypeName(TypeNameContext ctx) {
    HdesNode.Token token = token(ctx);
    
    // nested invocation
    if(ctx.getChildCount() > 1) {
      InvocationNode value = (InvocationNode) ctx.getChild(2).accept(this);
      InvocationNode path = (InvocationNode) ctx.getChild(0).accept(this);
      
      return ImmutableNestedInvocation.builder().token(token).path(path).value(value).build();
    }
    return first(ctx);
  }
  
  @Override
  public SimpleInvocation visitSimpleTypeName(SimpleTypeNameContext ctx) {
    return ImmutableSimpleInvocation.builder()
        .token(token(ctx))
        .value(ctx.getText())
        .build();
  }
  
  @Override
  public HdesNode visitPlaceholderTypeName(PlaceholderTypeNameContext ctx) {
    HdesNode typeName = first(ctx);
    
    if(ctx.getText().startsWith("_") && ctx.getText().length() > 1 && typeName instanceof SimpleInvocation) {
      return ImmutableNamedPlaceholder.builder()
          .token(token(ctx))
          .value(((SimpleInvocation) typeName).getValue().substring(1))
          .build();
    }
    return typeName;
  }

  @Override
  public Placeholder visitPlaceholderRule(PlaceholderRuleContext ctx) {
    if(ctx.getText().startsWith("_") && ctx.getText().length() > 1) {
      return ImmutableNamedPlaceholder.builder()
          .token(token(ctx))
          .value(ctx.getText().substring(1))
          .build();
    }
    return ImmutableEmptyPlaceholder.builder().token(token(ctx)).build();
  }

  @Override
  public RedundentScalarType visitScalarType(ScalarTypeContext ctx) {
    return ImmutableRedundentScalarType.builder()
        .token(token(ctx))
        .value(ScalarType.valueOf(ctx.getText().toUpperCase()))
        .build();
  }
  
  @Override
  public Headers visitHeaders(HeadersContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableHeaders.builder()
        .token(token(ctx))
        .acceptDefs(nodes.of(RedundentAccepts.class).get().getValues())
        .returnDefs(nodes.of(RedundentReturns.class).get().getValues())
        .build();
  }

  @Override
  public RedundentAccepts visitHeadersAccepts(HeadersAcceptsContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableRedundentAccepts.builder()
        .token(token(ctx))
        .values(nodes.of(RedundentTypeDefs.class).get().getValues())
        .build();
  }
  
  @Override
  public RedundentReturns visitHeadersReturns(HeadersReturnsContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableRedundentReturns.builder()
        .token(nodes.getToken())
        .values(nodes.of(RedundentTypeDefs.class).get().getValues())
        .build();
  }
  
  @Override
  public RedundentTypeDefs visitTypeDefs(TypeDefsContext ctx) {
    Nodes nodes = nodes(ctx);
    
    final var builder = ImmutableRedundentTypeDefs.builder().token(nodes.getToken());
    for(var defs : nodes.list(RedundentTypeDefs.class)) {
      builder.addAllValues(defs.getValues());
    }
    
    return builder.build();
  }

  @Override
  public RedundentTypeDefs visitTypeDef(TypeDefContext ctx) {
    Nodes nodes = nodes(ctx);
    return nodes.of(RedundentTypeDefs.class).get();
  }
  
  @Override
  public RedundentFormula visitFormula(FormulaContext ctx) {
    Nodes nodes = nodes(ctx);
    Optional<ExpressionBody> exp = nodes.of(ExpressionBody.class);
    return ImmutableRedundentFormula.builder().overAll(false).token(token(ctx)).value(exp.get()).build();
  }
  
  @Override
  public RedundentFormula visitFormulaOverAll(FormulaOverAllContext ctx) {
    Nodes nodes = nodes(ctx);
    Optional<ExpressionBody> exp = nodes.of(ExpressionBody.class);
    return ImmutableRedundentFormula.builder().overAll(true).token(token(ctx)).value(exp.get()).build();
  }
  
  @Override
  public RedundentTypeDefs visitSimpleType(SimpleTypeContext ctx) {
    Nodes nodes = nodes(ctx);
    Optional<RedundentFormula> formula = nodes.of(RedundentFormula.class);
    
    ContextTypeDef ctxTypeDef = getTypeDefContextType(ctx);
    RedundentTypeDefNames names = getDefTypeName(ctx);
    
    final var builder = ImmutableRedundentTypeDefs.builder().token(nodes.getToken());
    for(var name : names.getValues()) {
      builder.addValues(ImmutableScalarDef.builder()
          .token(token(ctx.getParent()))
          .required(formula.isPresent() ? false : name.getRequired())
          .name(name.getValue())
          .type(nodes.of(RedundentScalarType.class).get().getValue())
          .debugValue(nodes.of(RedundentDebugValue.class).map(e -> e.getValue()))
          .formula(formula.map(e -> e.getValue()))
          .formulaOverAll(formula.map(e -> e.getOverAll()))
          .array(false)
          .context(ctxTypeDef)
          .build());
    }
    return builder.build();
  }
  
  @Override
  public RedundentOptional visitOptional(OptionalContext ctx) {
    return ImmutableRedundentOptional.builder().token(token(ctx)).build();
  }
  
  @Override
  public RedundentTypeDefs visitArrayType(ArrayTypeContext ctx) {
    Nodes nodes = nodes(ctx);
    RedundentTypeDefs defs = nodes.of(RedundentTypeDefs.class).get();
    final var builder = ImmutableRedundentTypeDefs.builder().token(nodes.getToken());
    
    for(var input : defs.getValues()) {
      if(input instanceof ObjectDef) {
        ObjectDef def = (ObjectDef) input;
        builder.addValues(ImmutableObjectDef.builder().from(def).array(true).build());
      } else {
        ScalarDef def = (ScalarDef) input;
        builder.addValues(ImmutableScalarDef.builder().from(def).array(true).build());
      }
    }
    
    return builder.build();
  }

  @Override
  public RedundentTypeDefs visitObjectType(ObjectTypeContext ctx) {
    Nodes nodes = nodes(ctx);
    List<TypeDef> values = nodes.of(RedundentTypeDefs.class)
        .map((RedundentTypeDefs i)-> i.getValues())
        .orElse(Collections.emptyList());
    
    ContextTypeDef ctxTypeDef = getTypeDefContextType(ctx);
    RedundentTypeDefNames names = getDefTypeName(ctx);
    
    final var builder = ImmutableRedundentTypeDefs.builder().token(nodes.getToken());
    for(var name : names.getValues()) {
      builder.addValues(ImmutableObjectDef.builder()
        .token(nodes.getToken())
        .required(name.getRequired())
        .name(name.getValue())
        .values(values)
        .array(false)
        .context(ctxTypeDef)
        .build());
    }
    return builder.build();
  }

  @Override
  public RedundentDebugValue visitDebugValue(DebugValueContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableRedundentDebugValue.builder()
        .token(token(ctx))
        .value(nodes.of(Literal.class).get().getValue())
        .build();
  }
  
  
  protected ContextTypeDef getTypeDefContextType(ParserRuleContext context) {
    ParserRuleContext parent = context;
    do {
      if(parent instanceof HeadersAcceptsContext) {
        return ContextTypeDef.ACCEPTS;
      } else if(parent instanceof HeadersReturnsContext) {
        return ContextTypeDef.RETURNS;
      } else {
        parent = parent.getParent();
      }
    } while(parent != null);
    return null;
  }
  
  protected RedundentTypeDefNames getDefTypeName(ParserRuleContext ctx) {
    final ParserRuleContext childContext;
    if(ctx.getParent() instanceof TypeDefContext) {
      childContext = ctx.getParent();
    } else {
      childContext = ctx.getParent().getParent();
    }
    
    for(int index = 0; index < childContext.getChildCount(); index++) {
      ParseTree names = childContext.getChild(index);
      if(names instanceof TypeDefNamesContext) {
        return (RedundentTypeDefNames) names.accept(this);
      }
    }
    
    return ImmutableRedundentTypeDefNames.builder()
        .token(Nodes.token(ctx))
        .build();
  }
  
  @Override 
  public RedundentTypeDefNames visitTypeDefNames(HdesParser.TypeDefNamesContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableRedundentTypeDefNames.builder()
        .token(nodes.getToken())
        .values(nodes.list(RedundentTypeDefName.class))
        .build(); 
  }
  @Override 
  public RedundentTypeDefName visitTypeDefName(HdesParser.TypeDefNameContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableRedundentTypeDefName.builder()
        .token(nodes.getToken())
        .required(nodes.of(RedundentOptional.class).isEmpty())
        .value(nodes.of(SimpleInvocation.class).get().getValue())
        .build(); 
  }
}
