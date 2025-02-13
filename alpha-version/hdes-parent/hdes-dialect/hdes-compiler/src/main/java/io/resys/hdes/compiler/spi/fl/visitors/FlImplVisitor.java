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
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import org.immutables.value.Value;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.FlowNode.CallAction;
import io.resys.hdes.ast.api.nodes.FlowNode.CallDef;
import io.resys.hdes.ast.api.nodes.FlowNode.EmptyAction;
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
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
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory;
import io.resys.hdes.compiler.spi.fl.visitors.mapping.FlowMappingFactory;
import io.resys.hdes.compiler.spi.spec.HdesDefSpec;
import io.resys.hdes.compiler.spi.spec.ImmutableSpec;
import io.resys.hdes.compiler.spi.units.CompilerNode.FlowUnit;
import io.resys.hdes.executor.api.ImmutableAwait;
import io.resys.hdes.executor.api.ImmutableMaped;
import io.resys.hdes.executor.api.ImmutableMapedIterator;
import io.resys.hdes.executor.api.ImmutableNested;
import io.resys.hdes.executor.api.ImmutableStepId;
import io.resys.hdes.executor.api.Trace;
import io.resys.hdes.executor.api.Trace.TraceEnd;
import io.resys.hdes.executor.spi.beans.ImmutableTrace;

public class FlImplVisitor implements FlowBodyVisitor<FlSpec, TypeSpec>, FlowStepVisitor<FlSpec, FlSpec> {
  private final List<HdesNode.Token> visited = new ArrayList<>();
  //iterator name
  private static final String ACCESS_IT = "_it"; 
  private static final FlPointerVisitor POINTER_VISITOR = new FlPointerVisitor();
  
  @Value.Immutable
  public interface FlExecSpec extends FlSpec {
    Consumer<HdesDefSpec.ImplBuilder> getExecution();
    Consumer<CodeBlock.Builder> getValue();
  }

  @Override
  public TypeSpec visitBody(FlowTree tree) {
    final var unit = tree.get().node(FlowUnit.class);
    final var impl = HdesDefSpec.impl(unit.getType());
    
    addSteps(impl, tree, unit);
    addContinue(impl, tree);
    
    return impl.build().build();
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
    } else if(pointer instanceof IterationEndPointer) {
      return visitIterationEndPointer((IterationEndPointer) pointer, ctx); 
    }
    throw new IllegalArgumentException("not implemented");
  }
  
  @Override
  public FlExecSpec visitAction(StepAction action, HdesTree ctx) {
    if(action instanceof CallAction) {
      
      if(ctx.get().node(Step.class).getAwait()) {
        return visitWaitForAction((CallAction) action, ctx);
      }
      
      return visitCallAction((CallAction) action, ctx);      
    } else if(action instanceof IterateAction) {
      return visitIterateAction((IterateAction) action, ctx);
    } else if(action instanceof EmptyAction) {
      return ImmutableFlExecSpec.builder().execution(api -> {}).value(code -> {}).build();
    }
    throw new IllegalArgumentException("not implemented");
  }
  
  @Override
  public FlExecSpec visitStep(Step step, HdesTree ctx) {
    final var body = visitBody(step, ctx);
    
    return ImmutableFlExecSpec.builder()
        .execution(body.getExecution())
        .value(b -> b.addStatement("return $L(parent)", visitMethodName(step, ctx)))
        .build();
  }

  @Override
  public FlExecSpec visitBody(Step step, HdesTree ctx) {
    if(visited.contains(step.getToken())) {
      return ImmutableFlExecSpec.builder().execution(impl -> {}).value(code -> {}).build();
    }
    visited.add(step.getToken());
    
    
    final var next = ctx.next(step);
    final var children = visitPointer(step.getPointer(), next);
    final var action = visitAction(step.getAction(), next);
    final var insideIteration = ctx.find().ctx(IterateAction.class);
    final var unit = ctx.get().node(FlowUnit.class);
    
    
    final ClassName returnType;
    if(insideIteration.isPresent()) {
      returnType = ClassName.get(TraceEnd.class);
    } else {
      returnType = unit.getType().getReturnType().getName();
    }
    
    // step call/iteration/await etc...
    final var body = CodeBlock.builder();
    action.getValue().accept(body);
    if(step.getAs().isPresent()) {
      visitStepAs(step.getAs().get(), next).getValue().accept(body);
    }
    
    // return next step to call
    children.getValue().accept(body);
    
    final var method = MethodSpec.methodBuilder(visitMethodName(step, ctx))
        .addModifiers(Modifier.PROTECTED)
        .addParameter(ParameterSpec.builder(ClassName.get(Trace.class), "parent").build())
        .addCode(body.build())
        .returns(returnType).build();
    
    return ImmutableFlExecSpec.builder()
        .execution(impl -> {
          // current visit
          impl.method(method);
          
          // nested visits
          action.getExecution().accept(impl);
          children.getExecution().accept(impl);
        })
        .value(code -> {})
        .build();
  }
  
  @Override
  public FlExecSpec visitSplitPointer(SplitPointer pointer, HdesTree ctx) {
    final var next = ctx.next(pointer);
    final var nested = pointer.getValues().stream()
        .map(p -> visitPointer(p, next))
        .collect(Collectors.toList());

    return ImmutableFlExecSpec.builder()
        .execution(api -> nested.forEach(e -> e.getExecution().accept(api)))
        .value(POINTER_VISITOR.visitSplitPointer(pointer, ctx).getValue()).build();
  }

  @Override
  public FlExecSpec visitWhenPointer(WhenPointer pointer, HdesTree ctx) {
    final var next = ctx.next(pointer);
    final var conclusion = visitPointer(pointer.getThen(), next);
    return ImmutableFlExecSpec.builder()
        .execution(api -> conclusion.getExecution().accept(api))
        .value(POINTER_VISITOR.visitWhenPointer(pointer, ctx).getValue())
        .build();
  }

  @Override
  public FlExecSpec visitThenPointer(ThenPointer pointer, HdesTree ctx) {
    final var next = visitBody(pointer.getStep(), ctx.next(pointer));
    final var body = CodeBlock.builder();
    
    if(!ctx.get().node(Step.class).getAwait()) {
      POINTER_VISITOR.visitThenPointer(pointer, ctx).getValue().accept(body); 
    }
    
    return ImmutableFlExecSpec.builder()
        .execution(next.getExecution())
        .value(code -> code.add(body.build()))
        .build();
  }

  @Override
  public FlExecSpec visitEndPointer(EndPointer pointer, HdesTree ctx) {
    return ImmutableFlExecSpec.builder()
        .execution(e -> {})
        .value(POINTER_VISITOR.visitEndPointer(pointer, ctx).getValue())
        .build();
  }
  @Override
  public FlExecSpec visitCallAction(CallAction action, HdesTree ctx) {    
    final var next = ctx.next(action);
    final var step = ctx.get().node(Step.class);
    
    final var callBody = CodeBlock.builder();
    final var traceBody = CodeBlock.builder()
        .add("$T.builder()", ImmutableNested.class);
    
    if(action.getCalls().size() == 1) {
      visitCallDef(action.getCalls().get(0), next).getValue().accept(callBody);
      traceBody.add(".addValues($L)", "call");
    } else if(action.getCalls().size() > 1) {
      action.getCalls().forEach(c -> {
        visitCallDef(c, next).getValue().accept(callBody);
        traceBody.add(".addValues($L)", "call" + c.getIndex().get());
      });
    }
    return ImmutableFlExecSpec.builder().execution(api -> {})
        .value(code -> { 
          code.add(callBody.build());
          
          final var stepOrEnd = step.getPointer() instanceof IterationEndPointer ? "end" : "step";
          
          code.addStatement("parent = $T.builder().id($S).parent(parent).body($L.build()).$L()", 
              ImmutableTrace.class, step.getId().getValue(), traceBody.build(), stepOrEnd);
          
          
        }).build();
  }

  @Override
  public FlExecSpec visitIterateAction(IterateAction action, HdesTree ctx) {
    final var next = ctx.next(action);
    final var step = ctx.get().node(Step.class);
    
    CodeBlock mappedTo = action.getStep()
      .map(childStep -> { 
        final var traceBody = CodeBlock.builder()
            .add("$T.builder().value($L).build()", 
                ImmutableMapedIterator.class, ACCESS_IT).build();
        
        final var parent = CodeBlock.builder()
            .add("$T.builder().id($S).parent(mappedToParent).body($L).build()",
                ImmutableTrace.class, step.getId().getValue(), traceBody).build();
        
        return CodeBlock.builder().add("$L($L)", visitMethodName(childStep, next), parent).build();
      })      
      .orElseGet(() -> CodeBlock.builder().add("/* no mapping provided */ null").build());
    
    CodeBlock iteration = CodeBlock.builder()
        .add("$T.builder().values(mappedTo).build()", ImmutableMaped.class)
        .build();
    
    return ImmutableFlExecSpec.builder()
        .execution(impl -> action.getStep()
            .ifPresent(childStep -> visitStep(childStep, next).getExecution().accept(impl)))
        .value(code -> {
          final var condition = ExpressionFactory.builder().body(action.getOver()).tree(next).build().getValue();
          code
            .addStatement("final var mappedToParent = parent")
            .addStatement("final var mappedTo = $L.stream().map($L -> $L).collect($T.toList())", condition, ACCESS_IT, mappedTo, Collectors.class)
            .addStatement("parent = $T.builder().id($S).body($L).step()", ImmutableTrace.class, step.getId().getValue(), iteration);
        }).build();
  }


  @Override
  public FlExecSpec visitIterationEndPointer(IterationEndPointer pointer, HdesTree ctx) {
    return ImmutableFlExecSpec.builder()
        .execution(e -> {})
        .value(POINTER_VISITOR.visitIterationEndPointer(pointer, ctx).getValue())
        .build();
  }

  @Override
  public FlExecSpec visitStepAs(StepAs stepAs, HdesTree ctx) {
    final var step = ctx.get().node(Step.class);
    final var as = FlowMappingFactory.from(stepAs, ctx);;
    final var body = CodeBlock.builder().addStatement(
        "parent = $T.builder().id($S).body($L).parent(parent).step()", 
        ImmutableTrace.class, step.getId().getValue(), as).build();
    
    return ImmutableFlExecSpec.builder()
        .execution(e -> {})
        .value(b -> b.add(body))
        .build();
  }

  
  @Override
  public FlExecSpec visitCallDef(CallDef def, HdesTree ctx) {
    return ImmutableFlExecSpec.builder()
        .execution(api -> {})
        .value(code -> code.add(FlowMappingFactory.from(def, ctx)))
        .build();
  }
  
  private void addSteps(HdesDefSpec.ImplBuilder impl, FlowTree tree, FlowUnit unit) {
    
    final var code = CodeBlock.builder().addStatement(
        "final var parent = $T.builder().id($S).body($L).build()", 
        ImmutableTrace.class, "input", HdesDefSpec.ACCESS_INPUT_VALUE);

    final var step = tree.getValue().getStep()
        // step to code
        .map(s -> visitStep(s, tree))
        
        // empty body
        .orElseGet(() -> ImmutableFlExecSpec.builder().execution(e -> {})
            .value(b -> b
                .addStatement("final var returns = $T.builder().build()", ImmutableSpec.from(unit.getType().getReturns().getName()))
                .addStatement("return $T.builder().id($S).time(System.currentTimeMillis()).parent(parent).body(returns).build()",
                  ImmutableSpec.from(unit.getType().getReturnType().getName()), tree.getValue().getId().getValue())
                )
            .build());
    step.getExecution().accept(impl);
    step.getValue().accept(code);
    impl.execution(code.build());
  }

  private static FlExecSpec visitWaitForAction(CallAction action, HdesTree ctx) {
    return ImmutableFlExecSpec.builder()
        .execution(api -> {})
        .value(code -> {
          final var unit = ctx.get().node(FlowUnit.class);
          final var next = ctx.next(action);
          final var step = ctx.get().node(Step.class);
          final var flow = ctx.get().node(FlowBody.class);
          final var event = action.getCalls().stream().findFirst().get();
          
          final ClassName returnType;
          final var insideIteration = ctx.find().ctx(IterateAction.class);
          if(insideIteration.isPresent()) {
            returnType = ClassName.get(TraceEnd.class);
          } else {
            returnType = unit.getType().getReturnType().getName();
          }
          
          code
            .add(FlowMappingFactory.from(event, MappingEvent.ON_ENTER, next))
            .addStatement("final var stepId = $T.builder().flow($S).flowStep($S).build()", ImmutableStepId.class, flow.getId().getValue(), step.getId().getValue())
            .addStatement("final var suspend = $T.builder().trace(call).stepId(stepId).dataId(call.getBody().getDataId()).build()", ImmutableAwait.class)
            .addStatement("final var end = $T.builder().parent(parent).suspend(suspend).id($S).suspend()", ImmutableTrace.class, flow.getId().getValue())
            .addStatement("return $T.builder().from(end).build()", ImmutableSpec.from(returnType));          
        })
        .build();
  }
  
  public static String visitMethodName(Step step, HdesTree ctx) {
    String name = step.getId().getValue();
    
    if(name.isEmpty()) {
      Optional<HdesTree> iterate = ctx.find().ctx(IterateAction.class);
      if(iterate.isPresent()) {
        Step origin = iterate.get().get().node(Step.class);
        name = origin.getId().getValue() + "_It";
      }
    }
    
    return new StringBuilder()
        .append("visit")
        .append(name.substring(0, 1).toUpperCase())
        .append(name.length() == 1 ? "" : name.substring(1))
        .toString();
  }
  
  private static void addContinue(HdesDefSpec.ImplBuilder impl, FlowTree tree) {
    FlExecSpec continueSpec = new FlWakeUpVisitor().visitBody(tree);
    
    final var wakeup = CodeBlock.builder();
    continueSpec.getValue().accept(wakeup);
    
    continueSpec.getExecution().accept(impl);
    impl.wakeup(wakeup.build());
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

}
