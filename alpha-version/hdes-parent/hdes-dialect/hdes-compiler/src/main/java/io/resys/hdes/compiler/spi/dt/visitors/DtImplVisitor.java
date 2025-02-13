package io.resys.hdes.compiler.spi.dt.visitors;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import org.immutables.value.Value;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.BodyNode.ContextTypeDef;
import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
import io.resys.hdes.ast.api.nodes.BodyNode.Literal;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicy;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMapping;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MappingRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.ThenRuleRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.WhenRuleRow;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.HdesTree.DecisionTableTree;
import io.resys.hdes.ast.api.visitors.DecisionTableVisitor;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory;
import io.resys.hdes.compiler.spi.expressions.invocation.DecisionTableInvocationVisitor;
import io.resys.hdes.compiler.spi.spec.HdesDefSpec;
import io.resys.hdes.compiler.spi.spec.ImmutableSpec;
import io.resys.hdes.compiler.spi.units.CompilerNode.DecisionTableUnit;
import io.resys.hdes.executor.spi.beans.ImmutableTrace;
import io.resys.hdes.executor.spi.beans.ImmutableMatched;
import io.resys.hdes.executor.spi.exceptions.HdesNoMatchesException;

public class DtImplVisitor implements DecisionTableVisitor<DtSpec, TypeSpec> {

  
  @Value.Immutable
  interface DtCodeBlockSpec extends DtSpec {
    CodeBlock getValue();
  }
  
  @Value.Immutable
  interface DtHitPolicyCodeSpec extends DtSpec {
    CodeBlock getValue();
    Optional<CodeBlock> getConstants();
  }

  @Value.Immutable
  interface DtHeaderCodeBlockSpec extends DtSpec {
    Optional<CodeBlock> getBefore();
    Optional<CodeBlock> getAfter();
  }
  
  @Override
  public TypeSpec visitBody(DecisionTableTree ctx) {
    final var type = ctx.get().node(DecisionTableUnit.class);
  
    final var exec = HdesDefSpec.impl(type.getType());
    final var hitpolicy = visitHitPolicy(ctx.getValue().getHitPolicy(), ctx);
    hitpolicy.getConstants().ifPresent(c -> exec.field(
        FieldSpec
        .builder(type.getConstants().getName(), DecisionTableInvocationVisitor.ACCESS_CONSTANTS, Modifier.PRIVATE, Modifier.FINAL)
        .initializer(c)
        .build()));
    
    final var execution = CodeBlock.builder()
      .addStatement(
          "var parent = $T.builder().id($S).body($L).build()", 
          ImmutableTrace.class, "input", DecisionTableInvocationVisitor.ACCESS_INPUT_VALUE);
  
    final var headers = visitHeaders(ctx.getValue().getHeaders(), ctx);
    headers.getBefore().ifPresent(e -> execution.add(e));
    
    
    execution.add(hitpolicy.getValue());
    
    headers.getAfter().ifPresent(e -> execution.add(e));
    execution
      .addStatement("return $T.builder().id($S).time(System.currentTimeMillis()).parent(parent).body(returns).build()", 
          ImmutableSpec.from(type.getType().getReturnType().getName()),
          ctx.getValue().getId().getValue());

    return exec.execution(execution.build()).build().build();
  }
  
  public DtHitPolicyCodeSpec visitHitPolicy(HitPolicy hitPolicy, HdesTree ctx) {
    if(hitPolicy instanceof HitPolicyFirst) {
     return visitHitPolicyFirst((HitPolicyFirst) hitPolicy, ctx);
    } else if(hitPolicy instanceof HitPolicyAll) {
      return visitHitPolicyAll((HitPolicyAll) hitPolicy, ctx); 
    }
    return visitHitPolicyMapping((HitPolicyMapping) hitPolicy, ctx);
  }

  @Override
  public DtHitPolicyCodeSpec visitHitPolicyAll(HitPolicyAll node, HdesTree ctx) {
    final var type = ctx.get().node(DecisionTableUnit.class);
    var next = ctx.next(node);
    final var code = CodeBlock.builder()
        .addStatement("final var hitpolicy = $T.builder()", ImmutableSpec.from(type.getType().getReturns().getName()))
        .addStatement("final var hitpolicyTrace = $T.builder()", ImmutableMatched.class);
    int index = 0;
    for(RuleRow rules : node.getRows()) {
      final CodeBlock when = visitWhenRuleRow(rules.getWhen(), next).getValue();
      
      DtCodeBlockSpec conclusion = visitThenRuleRow(rules.getThen(), next);
      
      code
      .beginControlFlow("if($L)", when.isEmpty() ? "true" : when)
      
      // trace
      .addStatement("hitpolicyTrace.add($L, $S)", index, rules.getToken().getText())
      
      // entry
      .addStatement("hitpolicy.addValues($L)", conclusion.getValue())

      .endControlFlow();
      
      index++;
    }
    code
      .addStatement("var returns = hitpolicy.build()")
      .addStatement("parent = $T.builder().parent(parent).id($S).body(hitpolicyTrace.returns(returns).build()).build()", ImmutableTrace.class, "when");
    return ImmutableDtHitPolicyCodeSpec.builder().constants(getRuleConstants(node.getRows(), next)).value(code.build()).build();
  }
  
  @Override
  public DtCodeBlockSpec visitWhenRuleRow(WhenRuleRow node, HdesTree ctx) {
    final var next = ctx.next(node);
    final var code = CodeBlock.builder();

    int index = 0;
    List<CodeBlock> blocks = new ArrayList<>();
    for(ExpressionBody expression : node.getValues()) {
      final ScalarDef header = (ScalarDef) ctx.get().body().getHeaders().getAcceptDefs().get(index);
      CodeBlock value = ExpressionFactory.builder().body(expression).tree(next.next(header)).build().getValue();
      if(!value.toString().trim().equals("true")) {
        blocks.add(value);
      }
      index++;
    }
    index = 0;
    for(CodeBlock value : blocks) {
      if(index++ > 0) {
        code.add(" && ");  
      }
      code.add("($L)", value);
    }
    return ImmutableDtCodeBlockSpec.builder().value(code.build()).build();
  }

  @Override
  public DtHitPolicyCodeSpec visitHitPolicyFirst(HitPolicyFirst node, HdesTree ctx) {
    final var type = ctx.get().node(DecisionTableUnit.class);
    final var next = ctx.next(node);
    final var code = CodeBlock.builder()
        .addStatement("$T returns", type.getType().getReturns().getName())
        .addStatement("final var hitpolicyTrace = $T.builder()", ImmutableMatched.class);
    
    int index = 0;
    for(RuleRow rules : node.getRows()) {
      final CodeBlock when = visitWhenRuleRow(rules.getWhen(), next).getValue();
      final var control = index > 0 ? "else if" : "if";
      
      DtCodeBlockSpec conclusion = visitThenRuleRow(rules.getThen(), next);
      
      code
      .beginControlFlow(control + "($L)", when.isEmpty() ? "true" : when)
      
      // trace
      .addStatement("hitpolicyTrace.add($L, $S)", index, rules.getToken().getText())
      
      .addStatement("returns = $L", conclusion.getValue())
       
      .endControlFlow();
     
      index++;
    }
    
    if(index > 0) {
      code.beginControlFlow("else").addStatement("throw new $T()", HdesNoMatchesException.class).endControlFlow();
    }
    code.addStatement("parent = $T.builder().parent(parent).id($S).body(hitpolicyTrace.returns(returns).build()).build()", ImmutableTrace.class, "when");
    return ImmutableDtHitPolicyCodeSpec.builder().constants(getRuleConstants(node.getRows(), next)).value(code.build()).build();
  }
  

  @Override
  public DtHitPolicyCodeSpec visitHitPolicyMapping(HitPolicyMapping node, HdesTree ctx) {
    final var type = ctx.get().node(DecisionTableUnit.class);
    final var next = ctx.next(node);
    
    final var code = CodeBlock.builder()
        .addStatement("final var hitpolicy = $T.builder()", ImmutableSpec.from(type.getType().getReturns().getName()))
        .addStatement("final var hitpolicyTrace = $T.builder()", ImmutableMatched.class);
    for (MappingRow row : node.getMapsTo()) {
      code.add("\r\n").add(visitMappingRow(row, next).getValue());
    }
    code
      .addStatement("var returns = hitpolicy.build()")
      .addStatement("parent = $T.builder().parent(parent).id($S).body(hitpolicyTrace.returns(returns).build()).build()", ImmutableTrace.class, "when");
    return ImmutableDtHitPolicyCodeSpec.builder().constants(getMappingConstants(node.getMapsTo(), next)).value(code.build()).build();
  }
  
  @Override
  public DtCodeBlockSpec visitMappingRow(MappingRow row, HdesTree ctx) {
    final var type = ctx.get().node(DecisionTableUnit.class);
    HitPolicyMapping matrix = (HitPolicyMapping) type.getBody().getHitPolicy();
    WhenRuleRow when = matrix.getWhen();
    
    final ScalarDef header = (ScalarDef) type.getBody().getHeaders().getAcceptDefs().stream()
        .filter(t -> t.getName().equals(row.getAccepts().getValue()))
        .findFirst().get();

    final var code = CodeBlock.builder();
    final var next = ctx.next(header);
    int index = 0;
    for (ExpressionBody condition : when.getValues()) {
      final var control = index > 0 ? "else if" : "if";
      final Literal conclusion = row.getThen().getValues().get(index);
      code
        .beginControlFlow("$L($L) ", control, ExpressionFactory.builder().body(condition).tree(next).build().getValue())
        .addStatement("hitpolicyTrace.add($L, $S)", index, condition.getToken().getText());  
      code
        .addStatement("hitpolicy.$L($L)", header.getName(), ExpressionFactory.builder().body(conclusion).tree(next).build().getValue())
        .endControlFlow();
      index++;
    }
    return ImmutableDtCodeBlockSpec.builder().value(code.build()).build();
  }
  
  @Override
  public DtHeaderCodeBlockSpec visitHeaders(Headers node, HdesTree ctx) {
    final var next = ctx.next(node);
    
    
    // before
    final var beforeCollector = CodeBlock.builder();
    for(TypeDef type : node.getAcceptDefs()) {
      beforeCollector.add(visitHeader(type, next).getValue());
    }
    final var beforeFormulas = beforeCollector.build();
    final var before = CodeBlock.builder();
    if(!beforeFormulas.toString().isBlank()) {
      before
        .add("\r\n").add("// formulas on accepts params\r\n")
        .add(beforeFormulas)
        .addStatement(
          "parent = $T.builder().parent(parent).id($S).body($L).build()", 
          ImmutableTrace.class, "accepts-formulas", DecisionTableInvocationVisitor.ACCESS_INPUT_VALUE);
    }
    
    
    // after
    final var afterCollector = CodeBlock.builder();
    for(TypeDef type : node.getReturnDefs()) {
      afterCollector.add(visitHeader(type, next).getValue());
    }
    final var afterFormulas = afterCollector.build();

    final var after = CodeBlock.builder();
    if(!afterFormulas.toString().isBlank()) {
      after
        .add("\r\n").add("// formulas on returns params\r\n")
        .add(afterFormulas)
        .addStatement(
          "parent = $T.builder().parent(parent).id($S).body($L).build()", 
          ImmutableTrace.class, "returns-formulas", DecisionTableInvocationVisitor.ACCESS_OUTPUT_VALUE);
    }
    
    return ImmutableDtHeaderCodeBlockSpec.builder().before(before.add("\r\n").build()).after(after.add("\r\n").build()).build();
  }

  @Override
  public DtCodeBlockSpec visitHeader(TypeDef node, HdesTree ctx) {
    ScalarDef scalar = (ScalarDef) node;
    return visitHeader(scalar, ctx);
  }

  @Override
  public DtCodeBlockSpec visitHeader(ScalarDef node, HdesTree ctx) {
    if(node.getFormula().isEmpty()) {
      return ImmutableDtCodeBlockSpec.builder().value(CodeBlock.builder().build()).build();
    }
    return visitFormula(node, ctx);
  }

  @Override
  public DtCodeBlockSpec visitFormula(ScalarDef node, HdesTree ctx) {
    final var type = ctx.get().node(DecisionTableUnit.class);
    final var next = ctx.next(node);
    
    final var code = CodeBlock.builder();
    if(node.getContext() == ContextTypeDef.ACCEPTS) {
      CodeBlock formula = ExpressionFactory.builder().body(node.getFormula().get()).tree(next).build().getValue();
      
      code.addStatement("$L = $T.builder().from($L).$L($L).build()",
          DecisionTableInvocationVisitor.ACCESS_INPUT_VALUE,
          ImmutableSpec.from(type.getType().getAccepts().getName()),
          DecisionTableInvocationVisitor.ACCESS_INPUT_VALUE,
          node.getName(), formula
          );
    } else {
      CodeBlock formula = ExpressionFactory.builder().body(node.getFormula().get()).tree(next).build().getValue();
      code.addStatement("$L = $T.builder().from($L).$L($L).build()",
          DecisionTableInvocationVisitor.ACCESS_OUTPUT_VALUE,
          ImmutableSpec.from(type.getType().getReturns().getName()),
          DecisionTableInvocationVisitor.ACCESS_OUTPUT_VALUE,
          node.getName(), formula
          );
    } 
    
    return ImmutableDtCodeBlockSpec.builder().value(code.build()).build();
  }
  
  @Override
  public DtCodeBlockSpec visitThenRuleRow(ThenRuleRow node, HdesTree ctx) {
    final var type = ctx.get().node(DecisionTableUnit.class);
    final var next = ctx.next(node);

    // non formula headers    
    final List<ScalarDef> headers = type.getBody().getHeaders().getReturnDefs().stream()
        .map(e -> (ScalarDef) e)
        .filter(e -> e.getFormula().isEmpty())
        .collect(Collectors.toList());

    final var code = CodeBlock.builder().add("$T.builder()", ImmutableSpec.from(type.getListValue().getName()));
    int index = 0; 
    for(Literal conclusion : node.getValues()) {
      TypeDef header = headers.get(index);
      code.add(".$L($L)", header.getName(), ExpressionFactory.builder().body(conclusion).tree(next).build().getValue());
      index++;
    }
    
    return ImmutableDtCodeBlockSpec.builder().value(code.add(".build()").build()).build();
  }
  
  @Override
  public DtCodeBlockSpec visitHeader(ObjectDef node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }
  @Override
  public DtCodeBlockSpec visitRuleRow(RuleRow node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }
  
  
  private CodeBlock getRuleConstants(List<RuleRow> rows, HdesTree ctx) {
    final var type = ctx.get().node(DecisionTableUnit.class);
    final List<ScalarDef> headers = type.getBody().getHeaders().getReturnDefs().stream()
        .map(e -> (ScalarDef) e)
        .filter(e -> e.getFormula().isEmpty())
        .collect(Collectors.toList());

    CodeBlock.Builder execution = CodeBlock.builder().add("$T.builder()", ImmutableSpec.from(type.getConstants().getName()));
    for (RuleRow row : rows) {
      CodeBlock.Builder value = CodeBlock.builder();
      int index = 0;
      for (Literal then : row.getThen().getValues()) {
        final var header = headers.get(index++);
        final var next = ctx.next(row).next(header);
        CodeBlock literal = ExpressionFactory.builder().body(then).tree(next).build().getValue();
        CodeBlock ruleCode = CodeBlock.builder().add(".$L($L)", header.getName(), literal).build();
        
        if (ruleCode.isEmpty()) {
          continue;
        }
        
        value.add(ruleCode);
      }
      
      execution.add(".addValues($T.builder()$L.build())", ImmutableSpec.from(type.getListValue().getName()), value.build());
    }

    return execution.add(".build()").build();
  }
  
  private CodeBlock getMappingConstants(List<MappingRow> rows, HdesTree ctx) {
    final var type = ctx.get().node(DecisionTableUnit.class);
    CodeBlock.Builder execution = CodeBlock.builder().add("$T.builder()", ImmutableSpec.from(type.getConstants().getName()));
    for (MappingRow matrixRow : rows) {
      ScalarDef header = (ScalarDef) type.getBody().getHeaders().getAcceptDefs().stream()
          .filter(t -> t.getName().equals(matrixRow.getAccepts().getValue())).findFirst().get();
      
      CodeBlock.Builder values = CodeBlock.builder();
      final var next = ctx.next(matrixRow).next(header);
      for (Literal literalValue : matrixRow.getThen().getValues()) {
        if(!values.isEmpty()) {
          values.add(", ");
        }
        values.add("$L", ExpressionFactory.builder().body(literalValue).tree(next).build().getValue());
      }
      execution.add(".$L($T.asList($L))", header.getName(), Arrays.class, values.build());
      execution.add(".addValues($T.asList($L))", Arrays.class, values.build());
    }
    return execution.add(".build()").build();
  }
  
}
