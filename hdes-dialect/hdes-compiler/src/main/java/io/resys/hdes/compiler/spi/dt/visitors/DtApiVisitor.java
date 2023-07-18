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

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
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
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.HdesTree.DecisionTableTree;
import io.resys.hdes.ast.api.visitors.DecisionTableVisitor;
import io.resys.hdes.compiler.spi.spec.HdesDefSpec;
import io.resys.hdes.compiler.spi.spec.ImmutableSpec;
import io.resys.hdes.compiler.spi.units.CompilerNode.DecisionTableUnit;
import io.resys.hdes.executor.spi.dt.DecisionTableConstants;

public class DtApiVisitor implements DecisionTableVisitor<DtSpec, TypeSpec> {

  @Value.Immutable
  public interface DtHeaderSpec extends DtSpec {
    Consumer<ImmutableSpec.ImmutableBuilder> getValue();
  }

  @Value.Immutable
  public interface DtHeadersSpec extends DtSpec {
    Consumer<HdesDefSpec.ApiBuilder> getValue();
  }

  @Value.Immutable
  public interface DtHitPolicySpec extends DtSpec {
    Consumer<ImmutableSpec.ImmutableBuilder> getOutputs();
    Consumer<ImmutableSpec.ImmutableBuilder> getStatics();
    Consumer<HdesDefSpec.ApiBuilder> getNested();
  }
  
  
  @Override
  public TypeSpec visitBody(DecisionTableTree ctx) {
    final var type = ctx.get().node(DecisionTableUnit.class);
    final var api = HdesDefSpec.api(type.getType());
    
    visitHeaders(ctx.getValue().getHeaders(), ctx).getValue().accept(api);
    visitHitPolicy(ctx.getValue().getHitPolicy(), ctx);
    
    return api.build().build();
  }
  
  @Override
  public DtHitPolicySpec visitHitPolicy(HitPolicy hitPolicy, HdesTree ctx) {
    if(hitPolicy instanceof HitPolicyAll) {
      return visitHitPolicyAll((HitPolicyAll) hitPolicy, ctx);
    } else if(hitPolicy instanceof HitPolicyFirst) {
      return visitHitPolicyFirst((HitPolicyFirst) hitPolicy, ctx);
    } else if(hitPolicy instanceof HitPolicyMapping) {
      return visitHitPolicyMapping((HitPolicyMapping) hitPolicy, ctx);
    }
    throw new IllegalArgumentException("not implemented");
  }
  
  @Override
  public DtHeadersSpec visitHeaders(Headers node, HdesTree ctx) {
    final var type = ctx.get().node(DecisionTableUnit.class);
    final DecisionTableBody body = ctx.get().node(DecisionTableBody.class);
    final ParameterizedTypeName staticType = ParameterizedTypeName.get(ClassName.get(DecisionTableConstants.class), type.getConstants().getSuperinterface());
    final var next = ctx.next(node);
    
    Consumer<HdesDefSpec.ApiBuilder> consumer = (api) -> {
      
      ImmutableSpec.ImmutableBuilder input = api.inputValue();
      ImmutableSpec.ImmutableBuilder output = api.outputValue(type.getListValue().getName());
      
      ImmutableSpec.ImmutableBuilder constants = api.immutable(type.getConstants().getName()).superinterface(staticType);
      
      // IN and OUT from headers
      node.getAcceptDefs().stream().forEach(typeDef -> visitHeader(typeDef, next).getValue().accept(input));
      node.getReturnDefs().stream().forEach(typeDef -> visitHeader(typeDef, next).getValue().accept(output));
      
      // Additional types from hit policy
      DtHitPolicySpec hitPolicy = visitHitPolicy(body.getHitPolicy(), next);
      hitPolicy.getOutputs().accept(output);
      hitPolicy.getNested().accept(api);
      hitPolicy.getStatics().accept(constants);
      
      constants.build();
      input.build();
      output.build();
    };
    return ImmutableDtHeadersSpec.builder().value(consumer).build();
  }


  @Override
  public DtHeaderSpec visitHeader(TypeDef node, HdesTree ctx) {
    ScalarDef scalar = (ScalarDef) node;
    return visitHeader(scalar, ctx);
  }
  
  @Override
  public DtHeaderSpec visitHeader(ScalarDef scalar, HdesTree ctx) {
    return ImmutableDtHeaderSpec.builder().value((immutable) -> immutable.method(scalar).build()).build();
  }

  @Override
  public DtHitPolicySpec visitHitPolicyAll(HitPolicyAll node, HdesTree ctx) {
    final var type = ctx.get().node(DecisionTableUnit.class);
    return ImmutableDtHitPolicySpec.builder()
        .nested(api -> api.outputValue().method("values").isList().returns(type.getListValue().getName()).build().build())
        .outputs(t -> {})
        .statics(t -> {})
        .build();
  }
  
  @Override
  public DtHitPolicySpec visitHitPolicyFirst(HitPolicyFirst node, HdesTree ctx) {
    return ImmutableDtHitPolicySpec.builder()
        .nested(t -> {})
        .outputs(t -> {})
        .statics(t -> {})
        .build();
  }

  @Override
  public DtHitPolicySpec visitHitPolicyMapping(HitPolicyMapping matrix, HdesTree ctx) {
    HdesTree next = ctx.next(matrix);
    List<DtHitPolicySpec> delegate = matrix.getMapsTo().stream()
        .map(row -> visitMappingRow(row, next))
        .collect(Collectors.toList()); 

    return ImmutableDtHitPolicySpec.builder()
        .nested(t -> delegate.forEach(d -> d.getNested().accept(t)))
        .outputs(t -> delegate.forEach(d -> d.getOutputs().accept(t)))
        .statics(t -> delegate.forEach(d -> d.getStatics().accept(t)))
        .build();
  }
  
  @Override
  public DtHitPolicySpec visitMappingRow(MappingRow row, HdesTree ctx) {
    final var type = ctx.get().node(DecisionTableUnit.class);
    HitPolicyMapping matrix = (HitPolicyMapping) ctx.getValue();
    String headerName = row.getAccepts().getValue();
    ScalarDef scalar = (ScalarDef) type.getBody().getHeaders().getAcceptDefs().stream()
        .filter(t -> t.getName().equals(headerName)).findFirst().get();
    
    return ImmutableDtHitPolicySpec.builder()
        .nested(t -> {})
        .outputs(t -> t.method(scalar).returns(matrix.getDefTo()).build())
        .statics(t -> t.method(row.getAccepts()).returns(type.getConstants().getSuperinterface()).build())
        .build();
  }
  
  
  @Override
  public DtSpec visitWhenRuleRow(WhenRuleRow node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public DtSpec visitThenRuleRow(ThenRuleRow node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public DtSpec visitRuleRow(RuleRow node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public DtSpec visitHeader(ObjectDef node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public DtSpec visitFormula(ScalarDef node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }
}
