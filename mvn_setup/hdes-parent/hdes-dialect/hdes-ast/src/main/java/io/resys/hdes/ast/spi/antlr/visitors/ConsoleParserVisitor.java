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

import org.antlr.v4.runtime.ParserRuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.resys.hdes.ast.HdesParser.AdditiveExpressionContext;
import io.resys.hdes.ast.HdesParser.AndExpressionContext;
import io.resys.hdes.ast.HdesParser.ArrayTypeContext;
import io.resys.hdes.ast.HdesParser.ConditionalAndExpressionContext;
import io.resys.hdes.ast.HdesParser.ConditionalExpressionContext;
import io.resys.hdes.ast.HdesParser.ConditionalOrExpressionContext;
import io.resys.hdes.ast.HdesParser.DebugValueContext;
import io.resys.hdes.ast.HdesParser.DecisionTableUnitContext;
import io.resys.hdes.ast.HdesParser.EqualityExpressionContext;
import io.resys.hdes.ast.HdesParser.ExpressionContext;
import io.resys.hdes.ast.HdesParser.ExpressionUnitContext;
import io.resys.hdes.ast.HdesParser.FlowUnitContext;
import io.resys.hdes.ast.HdesParser.FormulaContext;
import io.resys.hdes.ast.HdesParser.HdesBodyContext;
import io.resys.hdes.ast.HdesParser.HeadersContext;
import io.resys.hdes.ast.HdesParser.HitPolicyContext;
import io.resys.hdes.ast.HdesParser.LambdaBodyContext;
import io.resys.hdes.ast.HdesParser.LambdaExpressionContext;
import io.resys.hdes.ast.HdesParser.LambdaParametersContext;
import io.resys.hdes.ast.HdesParser.LiteralContext;
import io.resys.hdes.ast.HdesParser.MappingArgContext;
import io.resys.hdes.ast.HdesParser.MappingContext;
import io.resys.hdes.ast.HdesParser.MappingValueContext;
import io.resys.hdes.ast.HdesParser.MethodArgContext;
import io.resys.hdes.ast.HdesParser.MethodArgsContext;
import io.resys.hdes.ast.HdesParser.MethodInvocationContext;
import io.resys.hdes.ast.HdesParser.MethodNameContext;
import io.resys.hdes.ast.HdesParser.MultiplicativeExpressionContext;
import io.resys.hdes.ast.HdesParser.ObjectTypeContext;
import io.resys.hdes.ast.HdesParser.PrimaryContext;
import io.resys.hdes.ast.HdesParser.RelationalExpressionContext;
import io.resys.hdes.ast.HdesParser.RuleUndefinedValueContext;
import io.resys.hdes.ast.HdesParser.ScalarTypeContext;
import io.resys.hdes.ast.HdesParser.SimpleTypeContext;
import io.resys.hdes.ast.HdesParser.ThenPointerContext;
import io.resys.hdes.ast.HdesParser.TypeDefContext;
import io.resys.hdes.ast.HdesParser.TypeDefsContext;
import io.resys.hdes.ast.HdesParser.TypeNameContext;
import io.resys.hdes.ast.HdesParser.UnaryExpressionContext;
import io.resys.hdes.ast.HdesParser.UnaryExpressionNotPlusMinusContext;
import io.resys.hdes.ast.HdesParser.WhenThenPointerContext;
import io.resys.hdes.ast.HdesParserBaseVisitor;
import io.resys.hdes.ast.api.nodes.HdesNode;

public class ConsoleParserVisitor extends HdesParserBaseVisitor<HdesNode> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleParserVisitor.class);
  
  @Override
  public HdesNode visitFormula(FormulaContext ctx) {
    log(ctx);
    return super.visitFormula(ctx);
  }

  @Override
  public HdesNode visitMethodArg(MethodArgContext ctx) {
    log(ctx);
    return super.visitMethodArg(ctx);
  }

  @Override
  public HdesNode visitLambdaExpression(LambdaExpressionContext ctx) {
    log(ctx);
    return super.visitLambdaExpression(ctx);
  }

  @Override
  public HdesNode visitLambdaParameters(LambdaParametersContext ctx) {
    log(ctx);
    return super.visitLambdaParameters(ctx);
  }

  @Override
  public HdesNode visitLambdaBody(LambdaBodyContext ctx) {
    log(ctx);
    return super.visitLambdaBody(ctx);
  }

  @Override
  public HdesNode visitWhenThenPointer(WhenThenPointerContext ctx) {
    log(ctx);
    return super.visitWhenThenPointer(ctx);
  }

  @Override
  public HdesNode visitThenPointer(ThenPointerContext ctx) {
    log(ctx);
    return super.visitThenPointer(ctx);
  }

  @Override
  public HdesNode visitRuleUndefinedValue(RuleUndefinedValueContext ctx) {
    log(ctx);
    return super.visitRuleUndefinedValue(ctx);
  }

  @Override
  public HdesNode visitScalarType(ScalarTypeContext ctx) {
    log(ctx);
    return super.visitScalarType(ctx);
  }

  @Override
  public HdesNode visitTypeDefs(TypeDefsContext ctx) {
    log(ctx);
    return super.visitTypeDefs(ctx);
  }

  @Override
  public HdesNode visitHdesBody(HdesBodyContext ctx) {
    log(ctx);
    return super.visitHdesBody(ctx);
  }

  @Override
  public HdesNode visitDecisionTableUnit(DecisionTableUnitContext ctx) {
    log(ctx);
    return super.visitDecisionTableUnit(ctx);
  }

  @Override
  public HdesNode visitMethodName(MethodNameContext ctx) {
    log(ctx);
    return super.visitMethodName(ctx);
  }

  @Override
  public HdesNode visitMethodInvocation(MethodInvocationContext ctx) {
    log(ctx);
    return super.visitMethodInvocation(ctx);
  }

  @Override
  public HdesNode visitMethodArgs(MethodArgsContext ctx) {
    log(ctx);
    return super.visitMethodArgs(ctx);
  }

  @Override
  public HdesNode visitPrimary(PrimaryContext ctx) {
    log(ctx);
    return super.visitPrimary(ctx);
  }

  @Override
  public HdesNode visitExpressionUnit(ExpressionUnitContext ctx) {
    log(ctx);
    return super.visitExpressionUnit(ctx);
  }

  @Override
  public HdesNode visitExpression(ExpressionContext ctx) {
    log(ctx);
    return super.visitExpression(ctx);
  }

  @Override
  public HdesNode visitConditionalExpression(ConditionalExpressionContext ctx) {
    log(ctx);
    return super.visitConditionalExpression(ctx);
  }

  @Override
  public HdesNode visitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
    log(ctx);
    return super.visitConditionalOrExpression(ctx);
  }

  @Override
  public HdesNode visitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
    log(ctx);
    return super.visitConditionalAndExpression(ctx);
  }

  @Override
  public HdesNode visitAndExpression(AndExpressionContext ctx) {
    log(ctx);
    return super.visitAndExpression(ctx);
  }

  @Override
  public HdesNode visitEqualityExpression(EqualityExpressionContext ctx) {
    log(ctx);
    return super.visitEqualityExpression(ctx);
  }

  @Override
  public HdesNode visitRelationalExpression(RelationalExpressionContext ctx) {
    log(ctx);
    return super.visitRelationalExpression(ctx);
  }

  @Override
  public HdesNode visitAdditiveExpression(AdditiveExpressionContext ctx) {
    log(ctx);
    return super.visitAdditiveExpression(ctx);
  }

  @Override
  public HdesNode visitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
    log(ctx);
    return super.visitMultiplicativeExpression(ctx);
  }

  @Override
  public HdesNode visitUnaryExpression(UnaryExpressionContext ctx) {
    log(ctx);
    return super.visitUnaryExpression(ctx);
  }

  @Override
  public HdesNode visitUnaryExpressionNotPlusMinus(UnaryExpressionNotPlusMinusContext ctx) {
    log(ctx);
    return super.visitUnaryExpressionNotPlusMinus(ctx);
  }

  @Override
  public HdesNode visitFlowUnit(FlowUnitContext ctx) {
    log(ctx);
    return super.visitFlowUnit(ctx);
  }
  
  @Override
  public HdesNode visitMapping(MappingContext ctx) {
    log(ctx);
    return super.visitMapping(ctx);
  }

  @Override
  public HdesNode visitMappingArg(MappingArgContext ctx) {
    log(ctx);
    return super.visitMappingArg(ctx);
  }

  @Override
  public HdesNode visitMappingValue(MappingValueContext ctx) {
    log(ctx);
    return super.visitMappingValue(ctx);
  }

  @Override
  public HdesNode visitTypeDef(TypeDefContext ctx) {
    log(ctx);
    return super.visitTypeDef(ctx);
  }

  @Override
  public HdesNode visitSimpleType(SimpleTypeContext ctx) {
    log(ctx);
    return super.visitSimpleType(ctx);
  }

  @Override
  public HdesNode visitObjectType(ObjectTypeContext ctx) {
    log(ctx);
    return super.visitObjectType(ctx);
  }

  @Override
  public HdesNode visitArrayType(ArrayTypeContext ctx) {
    log(ctx);
    return super.visitArrayType(ctx);
  }

  @Override
  public HdesNode visitDebugValue(DebugValueContext ctx) {
    log(ctx);
    return super.visitDebugValue(ctx);
  }

  @Override
  public HdesNode visitLiteral(LiteralContext ctx) {
    log(ctx);
    return super.visitLiteral(ctx);
  }

  @Override
  public HdesNode visitHeaders(HeadersContext ctx) {
    log(ctx);
    return super.visitHeaders(ctx);
  }

  @Override
  public HdesNode visitTypeName(TypeNameContext ctx) {
    log(ctx);
    return super.visitTypeName(ctx);
  }

  @Override
  public HdesNode visitHitPolicy(HitPolicyContext ctx) {
    log(ctx);
    return super.visitHitPolicy(ctx);
  }
  
  public static final void log(ParserRuleContext context) {
    if(LOGGER.isDebugEnabled()) {
      StringBuilder step = new StringBuilder();
      ParserRuleContext parent = context;
      while((parent = parent.getParent()) != null) {
        step.append("  ");
      }
      LOGGER.debug(step + context.getText());
    }
  }
}
