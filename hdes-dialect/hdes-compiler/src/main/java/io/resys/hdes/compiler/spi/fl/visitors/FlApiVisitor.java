package io.resys.hdes.compiler.spi.fl.visitors;

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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.BodyNode.ContextTypeDef;
import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.FlowNode.CallAction;
import io.resys.hdes.ast.api.nodes.FlowNode.CallDef;
import io.resys.hdes.ast.api.nodes.FlowNode.EmptyAction;
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.IterateAction;
import io.resys.hdes.ast.api.nodes.FlowNode.IterationEndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.SplitPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.Step;
import io.resys.hdes.ast.api.nodes.FlowNode.StepAction;
import io.resys.hdes.ast.api.nodes.FlowNode.StepAs;
import io.resys.hdes.ast.api.nodes.FlowNode.StepPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenPointer;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.HdesTree.FlowTree;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.FlowStepVisitor;
import io.resys.hdes.compiler.spi.spec.HdesDefSpec;
import io.resys.hdes.compiler.spi.spec.ImmutableSpec;
import io.resys.hdes.compiler.spi.units.CompilerNode.CompilerType;
import io.resys.hdes.compiler.spi.units.CompilerNode.FlowUnit;
import io.resys.hdes.executor.api.TraceBody;

public class FlApiVisitor implements FlowBodyVisitor<FlSpec, TypeSpec>, FlowStepVisitor<FlSpec, FlSpec> {

  @Value.Immutable
  public interface FlHeaderSpec extends FlSpec {
    Consumer<ImmutableSpec.ImmutableBuilder> getValue();
    Consumer<HdesDefSpec.ApiBuilder> getNested();
  }

  @Value.Immutable
  public interface FlHeadersSpec extends FlSpec {
    Consumer<HdesDefSpec.ApiBuilder> getValue();
  }
  
  
  @Override
  public TypeSpec visitBody(FlowTree ctx) {
    final var type = ctx.get().node(FlowUnit.class);
    final CompilerType compilerType = type.getType();
    final HdesDefSpec.ApiBuilder api = HdesDefSpec.api(compilerType);
    
    // create input/output
    visitHeaders(ctx.getValue().getHeaders(), ctx).getValue().accept(api);
    
    // create outputs for nested ends
    ctx.getValue().getStep().map(t -> visitStep(t, ctx)).ifPresent(spec -> spec.getNested().accept(api));
    return api.build().build();
  }

  @Override
  public FlHeadersSpec visitHeaders(Headers node, HdesTree ctx) {
    HdesTree next = ctx.next(node);
    Consumer<HdesDefSpec.ApiBuilder> consumer = (api) -> {
      ImmutableSpec.ImmutableBuilder input = api.inputValue();
      ImmutableSpec.ImmutableBuilder output = api.outputValue();
      
      for(TypeDef typeDef : node.getAcceptDefs()) {
        FlHeaderSpec header = visitHeader(typeDef, next);
        header.getValue().accept(input);
        header.getNested().accept(api);
      }
      
      for(TypeDef typeDef : node.getReturnDefs()) {
        FlHeaderSpec header = visitHeader(typeDef, next);
        header.getValue().accept(output);
        header.getNested().accept(api);
      }
    
      input.build();
      output.build();
    };
    return ImmutableFlHeadersSpec.builder().value(consumer).build();
  }

  @Override
  public FlHeaderSpec visitHeader(TypeDef node, HdesTree ctx) {
    if(node instanceof ScalarDef) {
      return visitHeader((ScalarDef) node, ctx);
    }
    return visitHeader((ObjectDef) node, ctx);
  }
  
  @Override
  public FlHeaderSpec visitHeader(ScalarDef node, HdesTree ctx) {
    return ImmutableFlHeaderSpec.builder()
        .value((immutable) -> immutable.method(node).build())
        .nested((api) -> {}).build();
  }

  @Override
  public FlHeaderSpec visitHeader(ObjectDef node, HdesTree ctx) {
    final var unit = ctx.get().node(FlowUnit.class);
    
    Consumer<HdesDefSpec.ApiBuilder> nested = (api) -> {
      final ImmutableSpec.ImmutableBuilder immutable;
      
      if(node.getContext() == ContextTypeDef.ACCEPTS) {
        immutable = api.inputValue(unit.getAccepts(node).getName());
      } else {
        immutable = api.outputValue(unit.getReturns(node).getName());
      }
      
      for (TypeDef type : node.getValues()) {
        FlHeaderSpec spec = visitHeader(type, ctx.next(type));
        spec.getValue().accept(immutable);
        spec.getNested().accept(api);
      }
      immutable.build();
    };
        
    final TypeName typeName = node.getContext() == ContextTypeDef.ACCEPTS ?
        unit.getAccepts(node).getName() : 
        unit.getReturns(node).getName();
    return ImmutableFlHeaderSpec.builder()
        .value((immutable) -> immutable.method(node, typeName).build())
        .nested(nested).build();
  }
  

  @Override
  public FlHeaderSpec visitStep(Step step, HdesTree ctx) {
    return visitBody(step, ctx);
  }

  @Override
  public FlHeaderSpec visitBody(Step step, HdesTree ctx) {
    final var next = ctx.next(step);
    final var action = visitAction(step.getAction(), next);
    final var pointer = visitPointer(step.getPointer(), next);
    final var as = step.getAs().map(v -> visitStepAs(v, next));
    
    return ImmutableFlHeaderSpec.builder()
        .nested(api -> {
          action.getNested().accept(api);
          pointer.getNested().accept(api);
          as.ifPresent(v -> v.getNested().accept(api));
        })
        .value(code -> {}).build();
  }

  @Override
  public FlHeaderSpec visitAction(StepAction action, HdesTree ctx) {
    if(action instanceof CallAction) {
      return visitCallAction((CallAction) action, ctx);
    } else if(action instanceof IterateAction) {
      return visitIterateAction((IterateAction) action, ctx);
    } else if(action instanceof EmptyAction) {
      return ImmutableFlHeaderSpec.builder().nested(api -> {}).value(code -> {}).build();
    }
    throw new IllegalArgumentException("not implemented");
  }


  @Override
  public FlHeaderSpec visitIterateAction(IterateAction action, HdesTree ctx) {
    final var unit = ctx.get().node(FlowUnit.class);
    final var step = ctx.get().node(Step.class);
    Optional<ObjectDef> endDef = ctx.step().findEnd(action.getStep());
    if(endDef.isEmpty()) {
      return ImmutableFlHeaderSpec.builder().nested(api -> {}).value(code -> {}).build();
    }
    
    Consumer<HdesDefSpec.ApiBuilder> nested = (api) -> {
      final var immutable = api.outputValue(unit.getEndAs(step).getName())
          .superinterface(TraceBody.class);
      
      for (TypeDef type : endDef.get().getValues()) {
        FlHeaderSpec spec = visitHeader(type, ctx.next(type));
        spec.getValue().accept(immutable);
        spec.getNested().accept(api);
      }
      immutable.build();
    };
        
    return ImmutableFlHeaderSpec.builder().nested(nested).value(code -> {}).build();
  }

  @Override
  public FlHeaderSpec visitPointer(StepPointer pointer, HdesTree ctx) {
    if(pointer instanceof EndPointer) {
      return visitEndPointer((EndPointer) pointer, ctx);
    } else if(pointer instanceof SplitPointer) {
      return visitSplitPointer((SplitPointer) pointer, ctx);
    } else if(pointer instanceof WhenPointer) {
      return visitWhenPointer((WhenPointer) pointer, ctx);
    } else if(pointer instanceof ThenPointer) {
      return visitThenPointer((ThenPointer) pointer, ctx); 
    }
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public FlHeaderSpec visitSplitPointer(SplitPointer pointer, HdesTree ctx) {
    final var next = ctx.next(pointer);
    final var nested = pointer.getValues().stream()
      .map(p -> visitPointer(p, next))
      .collect(Collectors.toList());
    return ImmutableFlHeaderSpec.builder()
        .nested(api -> nested.forEach(v -> v.getNested().accept(api)))
        .value(code -> {}).build();
  }
  
  @Override
  public FlHeaderSpec visitStepAs(StepAs stepAs, HdesTree ctx) {
    final var unit = ctx.get().node(FlowUnit.class);
    final var step = ctx.get().node(Step.class);
    final var endDef = ctx.step().getDefAs(step).get();
    
    Consumer<HdesDefSpec.ApiBuilder> nested = (api) -> {
      final var immutable = api
          .outputValue(unit.getEndAs(step).getName())
          .superinterface(TraceBody.class);
      
      for (TypeDef type : endDef.getValues()) {
        FlHeaderSpec spec = visitHeader(type, ctx.next(type));
        spec.getValue().accept(immutable);
        spec.getNested().accept(api);
      }
      immutable.build();
    };
        
    return ImmutableFlHeaderSpec.builder().nested(nested).value(code -> {}).build();
  }

  @Override
  public FlHeaderSpec visitWhenPointer(WhenPointer pointer, HdesTree ctx) {
    return visitPointer(pointer.getThen(), ctx.next(pointer));
  }

  @Override
  public FlHeaderSpec visitThenPointer(ThenPointer pointer, HdesTree ctx) {
    return visitStep(pointer.getStep(), ctx.next(pointer));
  }

  @Override
  public FlHeaderSpec visitEndPointer(EndPointer pointer, HdesTree ctx) {
    return ImmutableFlHeaderSpec.builder().nested(api -> {}).value(code -> {}).build();
  }
  
  @Override
  public FlHeaderSpec visitCallAction(CallAction action, HdesTree ctx) {
    return ImmutableFlHeaderSpec.builder().nested(api -> {}).value(code -> {}).build();
  }

  @Override
  public FlHeaderSpec visitCallDef(CallDef def, HdesTree ctx) {
    return ImmutableFlHeaderSpec.builder().nested(api -> {}).value(code -> {}).build();
  }
  
  @Override
  public FlSpec visitIterationEndPointer(IterationEndPointer pointer, HdesTree ctx) {
    return ImmutableFlHeaderSpec.builder().nested(api -> {}).value(code -> {}).build();
  }
}
