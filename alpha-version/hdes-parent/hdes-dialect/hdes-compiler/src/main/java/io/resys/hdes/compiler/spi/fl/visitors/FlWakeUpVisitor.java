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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

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
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.HdesTree.FlowTree;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.FlowStepVisitor;
import io.resys.hdes.compiler.spi.fl.visitors.FlImplVisitor.FlExecSpec;
import io.resys.hdes.compiler.spi.fl.visitors.mapping.FlowMappingFactory;
import io.resys.hdes.compiler.spi.units.CompilerNode.FlowUnit;
import io.resys.hdes.executor.api.ImmutableNested;
import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.executor.spi.beans.ImmutableTrace;
import io.resys.hdes.executor.spi.exceptions.FlowContinueException;
import io.resys.hdes.executor.spi.fl.ContinueNode;
import io.resys.hdes.executor.spi.fl.ContinueNode.ContinueCall;
import io.resys.hdes.executor.spi.fl.GetContinue;

public class FlWakeUpVisitor implements FlowBodyVisitor<FlSpec, FlSpec>, FlowStepVisitor<FlSpec, FlSpec> {
  private final List<HdesNode.Token> visited = new ArrayList<>();

  @Override
  public FlExecSpec visitBody(FlowTree ctx) {
    if(ctx.getValue().getStep().isEmpty()) {
      return ImmutableFlExecSpec.builder().execution(api -> {}).value(body -> {}).build();   
    }
    
    FlExecSpec step = visitStep(ctx.getValue().getStep().get(), ctx);
    return ImmutableFlExecSpec.builder()
        .execution(step.getExecution())
        .value(body -> {          

          final var cases = CodeBlock.builder();
          step.getValue().accept(cases);
          
          body
            .addStatement("final var continueNode = $T.from(trace).body(wakeup)", GetContinue.class)
            .beginControlFlow("switch(continueNode.getStep())")
            .add(cases.build())
            .addStatement("default: throw new $T(continueNode)", FlowContinueException.class)
            .endControlFlow();
          
        })
        .build();
  }

  @Override
  public FlExecSpec visitStep(Step step, HdesTree ctx) {
    return visitBody(step, ctx);
  }

  @Override
  public FlExecSpec visitBody(Step step, HdesTree ctx) {
    if(visited.contains(step.getToken())) {
      return ImmutableFlExecSpec.builder()
          .execution(impl -> {})
          .value(code -> {})
          .build();
    }
    visited.add(step.getToken());
    
    final var next = ctx.next(step);
    final var children = visitPointer(step.getPointer(), next);
    final var action = visitAction(step.getAction(), next);

    return ImmutableFlExecSpec.builder()
        .execution(impl -> {
          action.getExecution().accept(impl);
          children.getExecution().accept(impl);
        })
        .value(code -> {
          action.getValue().accept(code);
          children.getValue().accept(code);
        })
        .build();
  }

  @Override
  public FlExecSpec visitPointer(StepPointer pointer, HdesTree ctx) {
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
  public FlExecSpec visitSplitPointer(SplitPointer pointer, HdesTree ctx) {
    final var next = ctx.next(pointer);
    final var nested = pointer.getValues().stream()
        .map(p -> visitPointer(p, next))
        .collect(Collectors.toList());

    return ImmutableFlExecSpec.builder()
        .execution(api -> nested.forEach(e -> e.getExecution().accept(api)))
        .value(c -> nested.forEach(e -> e.getValue().accept(c)))
        .build();
  }

  @Override
  public FlExecSpec visitWhenPointer(WhenPointer pointer, HdesTree ctx) {
    final var next = ctx.next(pointer);
    return visitPointer(pointer.getThen(), next);
  }

  @Override
  public FlExecSpec visitThenPointer(ThenPointer pointer, HdesTree ctx) {
    FlExecSpec next = visitBody(pointer.getStep(), ctx.next(pointer));
    return next;
  }

  @Override
  public FlExecSpec visitEndPointer(EndPointer pointer, HdesTree ctx) {
    return ImmutableFlExecSpec.builder()
        .execution(e -> {})
        .value(code -> {})
        .build();
  }
  
  @Override
  public FlExecSpec visitAction(StepAction action, HdesTree ctx) {
    if(action instanceof CallAction) {
      return visitCallAction((CallAction) action, ctx);      
    } else if(action instanceof IterateAction) {
      return visitIterateAction((IterateAction) action, ctx);
    } else if(action instanceof EmptyAction) {
      return ImmutableFlExecSpec.builder().execution(api -> {}).value(code -> {}).build();
    }
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public FlExecSpec visitCallAction(CallAction action, HdesTree ctx) {
    final var step = ctx.get().node(Step.class);
    if(!step.getAwait()) {
      return ImmutableFlExecSpec.builder()
          .execution(api -> {})
          .value(code -> {}).build();
    }
    
    final var unit = ctx.get().node(FlowUnit.class);
    final var next = ctx.next(action);
    final var event = action.getCalls().stream().findFirst().get();
    final var insideIteration = ctx.find().ctx(IterateAction.class);
    
    final ClassName returnType;
    if(insideIteration.isPresent()) {
      returnType = ClassName.get(TraceEnd.class);
    } else {
      returnType = unit.getType().getReturnType().getName();
    }
    
    final var traceBody = CodeBlock.builder()
        .add("$T.builder()", ImmutableNested.class);
    
    if(action.getCalls().size() == 1) {
      traceBody.add(".addValues($L)", "call");
    } else if(action.getCalls().size() > 1) {
      action.getCalls().forEach(c -> {
        traceBody.add(".addValues($L)", "call" + c.getIndex().get());
      });
    }
    
    final var body = CodeBlock.builder()
      .addStatement("var parent = continueNode.getParent()")
      .addStatement("final var continueCall = ($T) continueNode.getBody().get()", ContinueCall.class)
      .addStatement("final var dataId = continueCall.getDataId()")
      .addStatement("final var data = continueCall.getData()")
      .add(FlowMappingFactory.from(event, MappingEvent.ON_COMPLETE, next))
      .addStatement("parent = $T.builder().id($S).parent(parent).body($L.build()).step()", ImmutableTrace.class, step.getId().getValue(), traceBody.build());
    
    new FlPointerVisitor()
      .visitPointer(step.getPointer(), next)
      .getValue().accept(body);

    final var method = MethodSpec.methodBuilder(wakeupMethodName(step))
        .addModifiers(Modifier.PROTECTED)
        .addParameter(ParameterSpec.builder(ClassName.get(ContinueNode.class), "continueNode").build())
        .addCode(body.build())
        .returns(returnType).build();
    
    return ImmutableFlExecSpec.builder()
        .execution(api -> api.method(method))
        .value(code -> {
          code
            .beginControlFlow("case $S: ", step.getId().getValue())
            .addStatement("return $L(continueNode)", wakeupMethodName(step))
            .endControlFlow();
        }).build();
  }

  @Override
  public FlExecSpec visitIterateAction(IterateAction action, HdesTree ctx) {
    return ImmutableFlExecSpec.builder()
        .execution(impl -> {})
        .value(code -> {}).build();
  }
  
  @Override
  public FlExecSpec visitCallDef(CallDef def, HdesTree ctx) {
    return ImmutableFlExecSpec.builder()
        .execution(api -> {})
        .value(code -> code.add(FlowMappingFactory.from(def, ctx)))
        .build();
  }
  
  @Override
  public FlSpec visitHeaders(Headers node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public FlSpec visitHeader(TypeDef node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public FlSpec visitHeader(ScalarDef node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public FlSpec visitHeader(ObjectDef node, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public FlSpec visitIterationEndPointer(IterationEndPointer pointer, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public FlSpec visitStepAs(StepAs stepAs, HdesTree ctx) {
    throw new IllegalArgumentException("not implemented");
  }
  
  private String wakeupMethodName(Step step) {
    String name = step.getId().getValue();
    return new StringBuilder()
        .append("onComplete")
        .append(name.substring(0, 1).toUpperCase())
        .append(name.length() == 1 ? "" : name.substring(1))
        .toString();
  }
}
