package io.resys.hdes.compiler.spi.expressions.invocation;

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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.BodyNode;
import io.resys.hdes.ast.api.nodes.BodyNode.ContextTypeDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.FlowNode.CallAction;
import io.resys.hdes.ast.api.nodes.FlowNode.CallDef;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.IterateAction;
import io.resys.hdes.ast.api.nodes.FlowNode.Step;
import io.resys.hdes.ast.api.nodes.FlowNode.StepAction;
import io.resys.hdes.ast.api.nodes.FlowNode.StepCallDef;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.HdesTree.TypeDefReturns;
import io.resys.hdes.ast.api.nodes.InvocationNode.EmptyPlaceholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.NamedPlaceholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.NestedInvocation;
import io.resys.hdes.ast.api.nodes.InvocationNode.Placeholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServiceBody;
import io.resys.hdes.ast.api.visitors.ExpressionVisitor.InvocationVisitor;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory.ExpCode;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory.ExpObjectCode;
import io.resys.hdes.compiler.spi.expressions.ExpressionFactory.ExpScalarCode;
import io.resys.hdes.compiler.spi.expressions.ImmutableExpObjectCode;
import io.resys.hdes.compiler.spi.expressions.ImmutableExpScalarCode;
import io.resys.hdes.compiler.spi.spec.HdesDefSpec;
import io.resys.hdes.compiler.spi.spec.JavaSpecUtil;
import io.resys.hdes.compiler.spi.units.CompilerNode;
import io.resys.hdes.compiler.spi.units.CompilerNode.CompilerType;
import io.resys.hdes.compiler.spi.units.CompilerNode.DecisionTableUnit;
import io.resys.hdes.compiler.spi.units.CompilerNode.FlowUnit;
import io.resys.hdes.compiler.spi.units.CompilerNode.ServiceUnit;
import io.resys.hdes.executor.spi.fl.GetTrace;

public class FlowInvocationVisitor extends DecisionTableInvocationVisitor implements InvocationVisitor<ExpCode, ExpCode> {
  public final static String ACCESS_INPUT_VALUE = HdesDefSpec.ACCESS_INPUT_VALUE;

  @Override
  public ExpCode visitNested(NestedInvocation node, HdesTree ctx) {
    final var next = ctx.next(node);
    final var pathDef = next.any().build(node.getPath());
    
    CodeBlock path = visitBody(node.getPath(), next).getValue();
    ExpCode value = visitBody(node.getValue(), next.next(pathDef));
    
    final var code = CodeBlock.builder().add(path).add(value.getValue()).build();
    if(value instanceof ExpScalarCode) {
      ExpScalarCode src = (ExpScalarCode) value;
      return ImmutableExpScalarCode.builder()
          .type(src.getType()).array(src.getArray())
          .value(code)
          .build();
    }
    
    ExpObjectCode src = (ExpObjectCode) value;
    return ImmutableExpObjectCode.builder()
        .array(src.getArray()).type(src.getType())
        .value(code)
        .build();
  }
  
  @Override
  public ExpCode visitSimple(SimpleInvocation node, HdesTree ctx) {
    Optional<LambdaExpression> lambda = ctx.find().node(LambdaExpression.class);
    TypeDef typeDef = ctx.any().build(node);
    CodeBlock.Builder value = CodeBlock.builder();
    
    if(lambda.isPresent() && lambda.get().getParam().getValue().equals(node.getValue())) {
      value.add("$L", node.getValue());
      
    } else if(typeDef.getContext() == ContextTypeDef.STEP_RETURNS) {
      value.add("$T.from(parent).step($S)", ClassName.get(GetTrace.class), node.getValue());
      
      ObjectDef returnDef = (ObjectDef) typeDef;
      
      if(returnDef.getValues().size() == 1) {
        StepCallDef call = (StepCallDef) returnDef.getValues().get(0);
        String dependencyId = call.getCallDef().getId().getValue();
        BodyNode dependencyNode = ctx.getRoot().getBody(dependencyId);
        CompilerNode compilerNode = ctx.get().node(CompilerNode.class);
        
        if(dependencyNode instanceof DecisionTableBody) {
          DecisionTableUnit unit = compilerNode.dt((DecisionTableBody) dependencyNode);
          value.add(".body($T.class)", unit.getType().getReturns().getName());
        } else if(dependencyNode instanceof FlowBody) {
          FlowUnit unit = compilerNode.fl((FlowBody) dependencyNode);
          value.add(".body($T.class)", unit.getType().getReturns().getName());
        } else if(dependencyNode instanceof ServiceBody) {
          ServiceUnit unit = compilerNode.st((ServiceBody) dependencyNode);
          value.add(".body($T.class)", unit.getType().getReturns().getName());
        } else {
          throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node)); 
        }
      }
    } else if(typeDef.getContext() == ContextTypeDef.STEP_AS) {
      FlowUnit unit = ctx.get().node(FlowUnit.class);
      FlowBody flow = ctx.get().node(FlowBody.class);
      
      Step step = ctx.step().findStep(node.getValue(), flow.getStep()).get();
      value
        .add("$T.from(parent).step($S)", ClassName.get(GetTrace.class), node.getValue())
        .add(".body($T.class)", unit.getEndAs(step).getName());
      

    } else if(typeDef.getContext() == ContextTypeDef.ACCEPTS) {
      value
        .add("$T.from(parent).step($S)", ClassName.get(GetTrace.class), ACCESS_INPUT_VALUE)
        .add(".body($T.class)", ctx.get().node(FlowUnit.class).getType().getAccepts().getName())
        .add(".$L$L", JavaSpecUtil.methodCall(typeDef.getName()), typeDef.getRequired() ? "" : ".get()"); 
    } else if(typeDef.getContext() == ContextTypeDef.STEP_END) {

    } else if(typeDef.getContext() == ContextTypeDef.RETURNS) {
      value.add("." + JavaSpecUtil.methodCall(typeDef.getName()) + (typeDef.getRequired() ? "" : ".get()"));
      
    } else if(typeDef.getContext() == ContextTypeDef.EXPRESSION) {
      value.add("." + JavaSpecUtil.methodCall(typeDef.getName()) + (typeDef.getRequired() ? "" : ".get()")); 
    }
    return wrap(typeDef, value.build());
  }

  @Override
  public ExpCode visitPlaceholder(Placeholder node, HdesTree ctx) {
    if(node instanceof EmptyPlaceholder) {
      return visitEmptyPlaceholder((EmptyPlaceholder) node, ctx);
    } else if(node instanceof NamedPlaceholder) {
      return visitNamedPlaceholder((NamedPlaceholder) node, ctx);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
  }

  @Override
  public ExpCode visitEmptyPlaceholder(EmptyPlaceholder node, HdesTree ctx) {
    Optional<HdesTree> iteratorTree = ctx.find().ctx(IterateAction.class);
    if(iteratorTree.isPresent()) {
      Step iteratorStep = iteratorTree.get().get().node(Step.class);
      IterateAction action = (IterateAction) iteratorStep.getAction();
      TypeDef iteratorType = ctx.next(iteratorStep).next(action).returns().build(action.getOver()).getReturns();
      
      final var value = CodeBlock.builder().add("$T.from(parent).step($S).body($T.class)", 
          ClassName.get(GetTrace.class), 
          iteratorStep.getId().getValue(),
          toCompilerType(iteratorType, ctx))
          .build();
      
      return wrap(iteratorType, value);
    }

    // step where iteration has happen'd
    Optional<Step> step = ctx.find().node(Step.class);
    if(step.isPresent()) {
      
      TypeDefReturns returns = ctx.returns().build(node);
      StepAction action = step.get().getAction();      
      
       if(action instanceof CallAction) {
        CodeBlock value = CodeBlock.builder().add("call.getBody()").build();
        return wrap(returns.getReturns(), value);
      } else if(action instanceof IterateAction) {
        IterateAction iterate = (IterateAction) action;
        
        
        if(iterate.getNested()) {
          FlowUnit unit = ctx.get().node(FlowUnit.class);
          CodeBlock value = CodeBlock.builder()
              .add("mappedTo.stream().map(trace -> $T.from(trace).body($T.class))", GetTrace.class, unit.getEndAs(step.get()).getName())
              .build();
          return wrap(returns.getReturns(), value);
        } else {
          // figure out which call/await this is refering...
          CallAction iterateOverAction = (CallAction) iterate.getStep().get().getAction();
          if(iterateOverAction.getCalls().size() == 1) {
            CallDef call = iterateOverAction.getCalls().get(0);
            String dependencyId = call.getId().getValue();
            BodyNode dependencyNode = ctx.getRoot().getBody(dependencyId);
            CompilerNode compilerNode = ctx.get().node(CompilerNode.class);
            CompilerType type = null;
            
            if(dependencyNode instanceof DecisionTableBody) {
              DecisionTableUnit unit = compilerNode.dt((DecisionTableBody) dependencyNode);
              type = unit.getType();
            } else if(dependencyNode instanceof FlowBody) {
              FlowUnit unit = compilerNode.fl((FlowBody) dependencyNode);
              type = unit.getType();
            } else if(dependencyNode instanceof ServiceBody) {
              ServiceUnit unit = compilerNode.st((ServiceBody) dependencyNode);
              type = unit.getType();
            } else {
              throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node)); 
            }
            
            CodeBlock value = CodeBlock.builder()
                .add("mappedTo.stream().map(trace -> $T.from(trace).body($T.class))", GetTrace.class, type.getReturns().getName())
                .build();
            return wrap(returns.getReturns(), value);
          }
          
        }
      }
    }
    
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
  }

  @Override
  public ExpCode visitNamedPlaceholder(NamedPlaceholder node, HdesTree ctx) {
    // step reference?
    Optional<Step> step = ctx.find().node(Step.class);
    if(step.isEmpty()) {
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
    }
    
    TypeDefReturns returns = ctx.returns().build(node);
    StepAction action = step.get().getAction();      
    
    if(!(action instanceof CallAction)) {
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
    }
    
    CallAction calls = (CallAction) action;
    if(calls.getCalls().size() == 1) {
      CodeBlock value = CodeBlock.builder().add("call.getBody().$L()", JavaSpecUtil.getMethodName(node.getValue())).build();
      return wrap(returns.getReturns(), value);      
    }
    
    try {
      int index = Integer.parseInt(node.getValue());
      Optional<CallDef> call = calls.getCalls().stream().filter(c -> c.getIndex().get().equals(index)).findFirst();
      if(call.isPresent()) {
        
        CodeBlock value = CodeBlock.builder().add("call$L.getBody()", index).build();
        return wrap(returns.getReturns(), value); 
      }
    } catch(Exception e) {
      // nothing to handle, unknown node
    }

    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
  }
  
  private ClassName toCompilerType(TypeDef node, HdesTree ctx) {
    if(node instanceof ScalarDef) {
      ScalarDef scalar = (ScalarDef) node;
      return ClassName.get(JavaSpecUtil.type(scalar.getType()));
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(node));
  }
}
