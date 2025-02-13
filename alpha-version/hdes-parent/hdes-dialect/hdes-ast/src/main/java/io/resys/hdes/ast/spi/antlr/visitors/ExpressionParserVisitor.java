package io.resys.hdes.ast.spi.antlr.visitors;

import java.util.ArrayList;

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

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.immutables.value.Value;

import io.resys.hdes.ast.HdesParser;
import io.resys.hdes.ast.HdesParser.AdditiveExpressionContext;
import io.resys.hdes.ast.HdesParser.AndExpressionContext;
import io.resys.hdes.ast.HdesParser.ConditionalAndExpressionContext;
import io.resys.hdes.ast.HdesParser.ConditionalExpressionContext;
import io.resys.hdes.ast.HdesParser.ConditionalOrExpressionContext;
import io.resys.hdes.ast.HdesParser.EqualityExpressionContext;
import io.resys.hdes.ast.HdesParser.ExpressionContext;
import io.resys.hdes.ast.HdesParser.ExpressionUnitContext;
import io.resys.hdes.ast.HdesParser.FilterMethodContext;
import io.resys.hdes.ast.HdesParser.FindFirstMethodContext;
import io.resys.hdes.ast.HdesParser.InstanceMethodChildContext;
import io.resys.hdes.ast.HdesParser.InstanceMethodContext;
import io.resys.hdes.ast.HdesParser.LambdaBodyContext;
import io.resys.hdes.ast.HdesParser.LambdaExpressionContext;
import io.resys.hdes.ast.HdesParser.LambdaParametersContext;
import io.resys.hdes.ast.HdesParser.MapMethodContext;
import io.resys.hdes.ast.HdesParser.MappingMethodChildContext;
import io.resys.hdes.ast.HdesParser.MappingMethodContext;
import io.resys.hdes.ast.HdesParser.MethodArgsContext;
import io.resys.hdes.ast.HdesParser.MethodInvocationContext;
import io.resys.hdes.ast.HdesParser.MethodNameContext;
import io.resys.hdes.ast.HdesParser.MultiplicativeExpressionContext;
import io.resys.hdes.ast.HdesParser.PrimaryContext;
import io.resys.hdes.ast.HdesParser.RelationalExpressionContext;
import io.resys.hdes.ast.HdesParser.SortMethodContext;
import io.resys.hdes.ast.HdesParser.StaticMethodContext;
import io.resys.hdes.ast.HdesParser.UnaryExpressionContext;
import io.resys.hdes.ast.HdesParser.UnaryExpressionNotPlusMinusContext;
import io.resys.hdes.ast.HdesParserBaseVisitor;
import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.ExpressionNode;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.CallMethodExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.InstanceMethodExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaFilterExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaSortExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MultiplicativeType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.StaticMethodExpression;
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.HdesNode.Token;
import io.resys.hdes.ast.api.nodes.ImmutableAdditiveExpression;
import io.resys.hdes.ast.api.nodes.ImmutableAndExpression;
import io.resys.hdes.ast.api.nodes.ImmutableBetweenExpression;
import io.resys.hdes.ast.api.nodes.ImmutableBodyId;
import io.resys.hdes.ast.api.nodes.ImmutableConditionalExpression;
import io.resys.hdes.ast.api.nodes.ImmutableEmptyPlaceholder;
import io.resys.hdes.ast.api.nodes.ImmutableEqualityOperation;
import io.resys.hdes.ast.api.nodes.ImmutableErrorNode;
import io.resys.hdes.ast.api.nodes.ImmutableExpressionBody;
import io.resys.hdes.ast.api.nodes.ImmutableHeaders;
import io.resys.hdes.ast.api.nodes.ImmutableInExpression;
import io.resys.hdes.ast.api.nodes.ImmutableInstanceMethodExpression;
import io.resys.hdes.ast.api.nodes.ImmutableLambdaExpression;
import io.resys.hdes.ast.api.nodes.ImmutableLambdaFilterExpression;
import io.resys.hdes.ast.api.nodes.ImmutableLambdaSortExpression;
import io.resys.hdes.ast.api.nodes.ImmutableMultiplicativeExpression;
import io.resys.hdes.ast.api.nodes.ImmutableNegateUnary;
import io.resys.hdes.ast.api.nodes.ImmutableNotUnary;
import io.resys.hdes.ast.api.nodes.ImmutableOrExpression;
import io.resys.hdes.ast.api.nodes.ImmutablePositiveUnary;
import io.resys.hdes.ast.api.nodes.ImmutableSimpleInvocation;
import io.resys.hdes.ast.api.nodes.ImmutableStaticMethodExpression;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.api.nodes.InvocationNode.StaticMethodType;
import io.resys.hdes.ast.spi.antlr.util.Nodes;

public class ExpressionParserVisitor extends HdesParserBaseVisitor<HdesNode> {

  // Internal only
  @Value.Immutable
  public interface RedundentMethodName extends ExpressionNode {
    SimpleInvocation getValue();
  }

  @Value.Immutable
  public interface RedundentArgs extends ExpressionNode {
    List<HdesNode> getValues();
  }

  @Value.Immutable
  public interface RedundentLambdaExpression extends ExpressionNode {
    List<SimpleInvocation> getArgs();
    HdesNode getBody();
  }
  
  @Value.Immutable
  public interface RedundentLambdaArgs extends ExpressionNode {
    List<SimpleInvocation> getValues();
  }

  @Value.Immutable
  public interface RedundentLambdaBody extends ExpressionNode {
    HdesNode getValue();
  }
  
  @Value.Immutable
  public interface RedundentNullNode extends ExpressionNode {
  }

  
  @Value.Immutable
  public interface RedundentFindFirst extends ExpressionNode {
  }
  
  @Value.Immutable
  public interface RedundentNestedMethod extends ExpressionNode {
    HdesNode getValue();
  }

  
  @Value.Immutable
  public interface RedundentTypeInvocation extends ExpressionNode {
    HdesNode getValue();
  }

  
  @Override
  public CallMethodExpression visitMethodInvocation(MethodInvocationContext ctx) {
    final Nodes nodes = nodes(ctx);
/*
    final Optional<InvocationNode> type = nodes.of(InvocationNode.class);
    final SimpleInvocation methodName = nodes.of(RedundentMethodName.class).get().getValue();
    final List<HdesNode> args = nodes.of(RedundentArgs.class).map(t -> t.getValues()).orElse(Collections.emptyList());
    
    if(type.isPresent()) {
      // only lambda map support
      if(!methodName.getValue().equals("map")) {
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId("")
            .target(methodName)
            .message("Unknown method: '" + methodName.getValue() + "', supported methods on types: map!")
            .build()); 
      }
      if(args.size() != 1) {
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId("")
            .target(methodName)
            .message("Lambda method: '" + methodName.getValue() + "' expects: 1 argument but was: " + args.size() + "!")
            .build()); 
      }
      
      HdesNode arg = args.get(0);
      if(!(arg instanceof RedundentLambdaExpression)) {
        throw new HdesException(ImmutableErrorNode.builder()
            .bodyId("")
            .target(methodName)
            .message("Lambda method: '" + methodName.getValue() + "' expects: lambda expression 'val -> exp' but was: '" + arg.getToken().getText() + "'!")
            .build()); 
      }
      
      RedundentLambdaExpression lambda = (RedundentLambdaExpression) arg;
      return ImmutableLambdaMapExpression.builder()
          .token(nodes.getToken())
          .type(type.get())
          .iterable(lambda.getArgs().get(0))
          .body(lambda.getBody())
          .build();
    }*/

    return nodes.of(CallMethodExpression.class).get();
  }
  
  @Override
  public InstanceMethodExpression visitInstanceMethod(InstanceMethodContext ctx) {
    final var nodes = nodes(ctx);
    final SimpleInvocation methodName = nodes.of(RedundentMethodName.class).get().getValue();
    final List<HdesNode> args = nodes.of(RedundentArgs.class).map(t -> t.getValues()).orElse(Collections.emptyList());
    return ImmutableInstanceMethodExpression.builder()
        .token(nodes.getToken())
        .name(methodName)
        .values(args)
        .next(nodes.of(RedundentNestedMethod.class))
        .build();
  }
  
  @Override
  public RedundentNestedMethod visitInstanceMethodChild(InstanceMethodChildContext ctx) {
    final var nodes = nodes(ctx);
    return ImmutableRedundentNestedMethod.builder()
        .token(nodes.getToken())
        .value(nodes.of(HdesNode.class).get())
        .build();
  }
  
  
  @Override
  public LambdaExpression visitMappingMethod(MappingMethodContext ctx) {
    final var nodes = nodes(ctx);
    final var lambda = nodes.of(LambdaExpression.class).get();
    
    return ImmutableLambdaExpression.builder()
        .from(lambda)
        .token(nodes.getToken())
        .type(nodes.of(InvocationNode.class).get())
        .next(nodes.of(RedundentNestedMethod.class))
        .build();
  }
  
  @Override
  public HdesNode visitMappingMethodChild(MappingMethodChildContext ctx) {
    // TODO Auto-generated method stub
    return super.visitMappingMethodChild(ctx);
  }
  
  @Override
  public LambdaExpression visitMapMethod(MapMethodContext ctx) {
    final var nodes = nodes(ctx);
    final var lambda = nodes.of(RedundentLambdaExpression.class).get();
    
    return ImmutableLambdaExpression.builder()
        .token(nodes.getToken())
        .param(lambda.getArgs().get(0))
        .body(lambda.getBody())
        .sort(nodes.list(LambdaSortExpression.class))
        .filter(nodes.list(LambdaFilterExpression.class))
        .findFirst(nodes.of(RedundentFindFirst.class).isPresent())
        .type(ImmutableEmptyPlaceholder.builder().token(nodes.getToken()).build())
        .build();
  }
  
  @Override
  public HdesNode visitFindFirstMethod(FindFirstMethodContext ctx) {
    final var nodes = nodes(ctx);
    return ImmutableRedundentFindFirst.builder().token(nodes.getToken()).build();
  }
  
  @Override
  public LambdaFilterExpression visitFilterMethod(FilterMethodContext ctx) {
    final var nodes = nodes(ctx);
    final var lambda = nodes.of(RedundentLambdaExpression.class).get();
    
    return ImmutableLambdaFilterExpression.builder()
        .token(nodes.getToken())
        .param(lambda.getArgs().get(0))
        .body(lambda.getBody())
        .build();
  }
  
  @Override
  public LambdaSortExpression visitSortMethod(SortMethodContext ctx) {
    final var nodes = nodes(ctx);
    final var lambda = nodes.of(RedundentLambdaExpression.class).get();
    
    return ImmutableLambdaSortExpression.builder()
        .token(nodes.getToken())
        .param(lambda.getArgs().get(0))
        .body(lambda.getBody())
        .build();
  }
  
  @Override
  public StaticMethodExpression visitStaticMethod(StaticMethodContext ctx) {
    final Nodes nodes = nodes(ctx);
    final TerminalNode type = (TerminalNode) ctx.getChild(0);
    
    final ImmutableSimpleInvocation methodName = ImmutableSimpleInvocation.builder()
        .token(nodes.getToken())
        .value(type.getText())
        .build();
    
    final StaticMethodType mathType;
    switch(type.getSymbol().getText().toLowerCase()) {
      case "sum": mathType = StaticMethodType.SUM; break;
      case "avg": mathType = StaticMethodType.AVG; break;
      case "min": mathType = StaticMethodType.MIN; break;
      case "max": mathType = StaticMethodType.MAX; break;
      case "in": mathType = StaticMethodType.MAX; break;
      default: throw new HdesException(ImmutableErrorNode.builder()
        .bodyId("")
        .target(methodName)
        .message("Expected one of global methods: sum, avg, min, max but was: '" + methodName.getValue() + "'!")
        .build());
    }
    
    final List<HdesNode> args = nodes.of(RedundentArgs.class).map(t -> t.getValues()).orElse(Collections.emptyList());
    return ImmutableStaticMethodExpression.builder()
        .token(nodes.getToken())
        .type(mathType)
        .values(args)
        .build();
  }
  
  @Override
  public RedundentMethodName visitMethodName(MethodNameContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableRedundentMethodName.builder().token(nodes.getToken()).value(nodes.of(SimpleInvocation.class).get()).build();
  }
  
  @Override
  public RedundentArgs visitMethodArgs(MethodArgsContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableRedundentArgs.builder().token(nodes.getToken()).values(nodes.list(HdesNode.class)).build();
  }

  @Override
  public RedundentLambdaExpression visitLambdaExpression(LambdaExpressionContext ctx) {
    final Nodes nodes = nodes(ctx);
    final List<SimpleInvocation> values = nodes.of(RedundentLambdaArgs.class).map(e -> e.getValues()).orElse(Collections.emptyList());
    final RedundentLambdaExpression result = ImmutableRedundentLambdaExpression.builder()
      .token(nodes.getToken())
      .args(values)
      .body(nodes.of(RedundentLambdaBody.class).get().getValue())
      .build();
    
    if(values.size() != 1) {
      throw new HdesException(ImmutableErrorNode.builder()
          .bodyId("")
          .target(result)
          .message("Lambda expression is expected to have: 1 argument but was: " + values.size() + "!")
          .build());
    }
    return result;
  }
  
  @Override
  public RedundentLambdaBody visitLambdaBody(LambdaBodyContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableRedundentLambdaBody.builder().token(nodes.getToken()).value(nodes.of(HdesNode.class).get()).build();
  }
  
  @Override
  public RedundentLambdaArgs visitLambdaParameters(LambdaParametersContext ctx) {
    Nodes nodes = nodes(ctx);
    return ImmutableRedundentLambdaArgs.builder().token(nodes.getToken()).values(nodes.list(SimpleInvocation.class)).build();
  }
  
  @Override
  public HdesNode visitPrimary(PrimaryContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    for (int i = 0; i < n; i++) {
      ParseTree c = ctx.getChild(i);
      if (c instanceof TerminalNode) {
        continue;
      }
      return c.accept(this);
    }
    return null;
  }

  @Override
  public HdesNode visitExpression(ExpressionContext ctx) {
    return first(ctx);
  }
  
  @Override
  public HdesNode visitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
    return first(ctx);
  }

  @Override
  public ExpressionBody visitExpressionUnit(ExpressionUnitContext ctx) {
    String text = ctx.getStart().getInputStream().getText(new Interval(ctx.start.getStartIndex(), ctx.stop.getStopIndex()));
    Token token = token(ctx);
    HdesNode node = first(ctx);
    return ImmutableExpressionBody.builder()
        .src(text)
        .id(ImmutableBodyId.builder().value("").token(token).build())
        .headers(ImmutableHeaders.builder().token(token).build())
        .value(node == null ? ImmutableRedundentNullNode.builder().token(token).build(): node)
        .token(token)
        .build();
  }

  @Override
  public HdesNode visitConditionalExpression(ConditionalExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    // x < 20 ? 5 : 120
    // child[0] = x < 20
    // child[1] = ?
    // child[2] = 5
    // child[3] = :
    // child[4] = 120
    HdesNode condition = ctx.getChild(0).accept(this);
    ParseTree first = ctx.getChild(1);
    
    if (first instanceof TerminalNode) {
      TerminalNode terminal = ((TerminalNode) first);
      
      if(terminal.getSymbol().getType() == HdesParser.BETWEEN) {
        HdesNode left = ctx.getChild(2).accept(this);
        HdesNode right = ctx.getChild(4).accept(this);
        return ImmutableBetweenExpression.builder()
            .token(token(ctx))
            .value(condition)
            .left(left)
            .right(right)
            .build();
      } else if(terminal.getSymbol().getText().equalsIgnoreCase(StaticMethodType.IN.name())) {
        List<HdesNode> values = new ArrayList<>();
        for(int index = 2; index < n; index++) {
          HdesNode value = ctx.getChild(index).accept(this);
          if(value != null) {
            values.add(value);
          }
        }
        
        return ImmutableInExpression.builder()
            .token(token(ctx))
            .left(condition)
            .right(values)
            .build();
      }
    }
    HdesNode left = ctx.getChild(2).accept(this);
    HdesNode right = ctx.getChild(4).accept(this);
    return ImmutableConditionalExpression.builder()
        .token(token(ctx))
        .operation((EqualityOperation) condition)
        .left(left)
        .right(right)
        .build();
  }

  @Override
  public HdesNode visitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    HdesNode left = ctx.getChild(0).accept(this);
    HdesNode right = ctx.getChild(2).accept(this);
    return ImmutableOrExpression.builder()
        .token(token(ctx))
        .left(left)
        .right(right).build();
  }

  @Override
  public HdesNode visitAndExpression(AndExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    HdesNode left = ctx.getChild(0).accept(this);
    HdesNode right = ctx.getChild(2).accept(this);
    return ImmutableAndExpression.builder()
        .token(token(ctx))
        .left(left).right(right).build();
  }

  @Override
  public HdesNode visitEqualityExpression(EqualityExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }

    HdesNode left = ctx.getChild(0).accept(this);
    String v = ctx.getChild(1).getText();
    HdesNode right = ctx.getChild(2).accept(this);
    EqualityType type;
    
    if (v.equals(EqualityType.NOTEQUAL.getValue())) {
      type = EqualityType.NOTEQUAL;
    } else {
      type = EqualityType.EQUAL;
    }
    
    return ImmutableEqualityOperation.builder()
        .token(token(ctx))
        .type(type)
        .left(left).right(right).build();
  }

  @Override
  public HdesNode visitRelationalExpression(RelationalExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }

    
    HdesNode left = ctx.getChild(0).accept(this);
    String v = ctx.getChild(1).getText();
    HdesNode right = ctx.getChild(2).accept(this);
    EqualityType type;
    if (v.equals(EqualityType.LESS.getValue())) {
      type = EqualityType.LESS;
    } else if (v.equals(EqualityType.LESS_THEN.getValue())) {
      type = EqualityType.LESS_THEN;
    } else if (v.equals(EqualityType.GREATER.getValue())) {
      type = EqualityType.GREATER;
    } else {
      type = EqualityType.GREATER_THEN;
    }
    return ImmutableEqualityOperation.builder()
        .token(token(ctx))
        .type(type)
        .left(left).right(right).build();
  }

  @Override
  public HdesNode visitAdditiveExpression(AdditiveExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }

    HdesNode left = ctx.getChild(0).accept(this);
    HdesNode right = ctx.getChild(2).accept(this);
    TerminalNode sign = (TerminalNode) ctx.getChild(1);
    return ImmutableAdditiveExpression.builder()
        .token(token(ctx))
        .type(sign.getSymbol().getType() == HdesParser.PLUS ? AdditiveType.ADD : AdditiveType.SUBSTRACT)
        .left(left).right(right).build();
  }

  @Override
  public HdesNode visitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }

    HdesNode left = ctx.getChild(0).accept(this);
    HdesNode right = ctx.getChild(2).accept(this);
    TerminalNode sign = (TerminalNode) ctx.getChild(1);
    return ImmutableMultiplicativeExpression.builder()
        .token(token(ctx))
        .type(sign.getSymbol().getType() == HdesParser.MULTIPLY ? MultiplicativeType.MULTIPLY : MultiplicativeType.DIVIDE)
        .left(left).right(right).build();
  }

  @Override
  public HdesNode visitUnaryExpression(UnaryExpressionContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    
    TerminalNode terminalNode = null;
    HdesNode childResult = null;
    for (int i = 0; i < n; i++) {
      ParseTree c = ctx.getChild(i);

      if (c instanceof TerminalNode) {
        terminalNode = (TerminalNode) c;
        continue;
      }
      childResult = c.accept(this);
    }
    
    if(terminalNode.getSymbol().getType() == HdesParser.ADD) {
      return ImmutablePositiveUnary.builder()
          .token(token(ctx))
          .value(childResult).build();
    }
    return ImmutableNegateUnary.builder()
        .token(token(ctx))
        .value(childResult).build();
  }

  @Override
  public HdesNode visitUnaryExpressionNotPlusMinus(UnaryExpressionNotPlusMinusContext ctx) {
    int n = ctx.getChildCount();
    if (n == 1) {
      return first(ctx);
    }
    HdesNode childResult = null;
    for (int i = 0; i < n; i++) {
      ParseTree c = ctx.getChild(i);
      if (c instanceof TerminalNode) {
        continue;
      }
      childResult = c.accept(this);
    }
    return ImmutableNotUnary.builder()
        .token(token(ctx))
        .value(childResult).build();
  }
  
  protected final HdesNode first(ParserRuleContext ctx) {
    ParseTree c = ctx.getChild(0);
    return c.accept(this);
  }

  protected final Nodes nodes(ParserRuleContext node) {
    return Nodes.from(node, this);
  }

  protected final HdesNode.Token token(ParserRuleContext node) {
    return Nodes.token(node);
  }
}
