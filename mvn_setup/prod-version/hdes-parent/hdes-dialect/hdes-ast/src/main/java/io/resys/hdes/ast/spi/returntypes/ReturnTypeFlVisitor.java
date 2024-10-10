package io.resys.hdes.ast.spi.returntypes;

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

import java.util.Optional;

import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.BodyNode.ContextTypeDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.ExpressionNode;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.IterateAction;
import io.resys.hdes.ast.api.nodes.FlowNode.Step;
import io.resys.hdes.ast.api.nodes.FlowNode.StepAction;
import io.resys.hdes.ast.api.nodes.FlowNode.StepCallDef;
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.ImmutableErrorNode;
import io.resys.hdes.ast.api.nodes.InvocationNode;
import io.resys.hdes.ast.api.nodes.InvocationNode.EmptyPlaceholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.NamedPlaceholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.NestedInvocation;
import io.resys.hdes.ast.api.nodes.InvocationNode.Placeholder;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;
import io.resys.hdes.ast.api.visitors.ExpressionVisitor.InvocationVisitor;




public class ReturnTypeFlVisitor implements InvocationVisitor<TypeDef, TypeDef> {

  @Override
  public TypeDef visitBody(InvocationNode node, HdesTree ctx) {
    if(node instanceof NestedInvocation) {
      return visitNested((NestedInvocation) node, ctx);
    } else if(node instanceof Placeholder) {
      return visitPlaceholder((Placeholder) node, ctx);
    } else if(node instanceof SimpleInvocation) {
      return visitSimple((SimpleInvocation) node, ctx);
    }
    throw new HdesException(unknownInvocation(node));
  }

  @Override
  public TypeDef visitNested(NestedInvocation node, HdesTree ctx) {
    HdesTree next = ctx.next(node);
    TypeDef parent = visitBody(node.getPath(), next);
    return visitBody(node.getValue(), next.next(parent));
  }
  
  @Override
  public TypeDef visitSimple(SimpleInvocation node, HdesTree ctx) {
    
    // find lambda node and one node after it
    HdesTree iterator = ctx;
    ObjectDef lambdaObject = null;
    do {
      if(iterator.getParent().get().getValue() instanceof LambdaExpression) {
        if(iterator.getValue() instanceof ObjectDef) {
          lambdaObject = (ObjectDef) iterator.getValue();    
          if(lambdaObject.getName().equals(node.getValue())) {
            break;
          }
        }
      }
      iterator = iterator.getParent().get();
    } while(!(iterator.getValue() instanceof FlowBody));
    
    if(lambdaObject != null && lambdaObject.getName().equals(node.getValue())) {
      return lambdaObject;
    }
    
    
    if(ctx.getValue() instanceof StepCallDef) {
      StepCallDef def = (StepCallDef) ctx.getValue();
      Optional<TypeDef> nested = def.getValues().stream().filter(s -> s.getName().equals(node.getValue())).findFirst();
      if(nested.isPresent()) {
        return nested.get();
      }
    }
    
    // find node on parent
    if(ctx.getValue() instanceof ObjectDef) {
      ObjectDef def = (ObjectDef) ctx.getValue();
      Optional<TypeDef> child = def.getValues().stream().filter(s -> s.getName().equals(node.getValue())).findFirst();
      if(child.isPresent()) {
        return child.get();
      }
      
      Optional<StepCallDef> skip = def.getValues().stream()
          .filter(p -> p instanceof StepCallDef)
          .filter(s -> s.getName().equals("_"))
          .map(p -> (StepCallDef) p)
          .findFirst();
      
      if(skip.isPresent()) {
        Optional<TypeDef> nested = skip.get().getValues().stream().filter(s -> s.getName().equals(node.getValue())).findFirst();
        if(nested.isPresent()) {
          return nested.get();
        }
      }
    }
    
    
    FlowBody body = ctx.get().node(FlowBody.class);
    
    // find flow step
    Optional<Step> step = ctx.step().findStep(node.getValue(), body.getStep());
    if(step.isPresent()) {
      
      if(step.get().getAs().isPresent()) {
        return ctx.step().getDefAs(step.get()).get();  
      }
      
      return ctx.step().getDef(step.get());
    }
    
    // flow accepted section
    Optional<TypeDef> accepted = body.getHeaders().getAcceptDefs().stream()
        .filter(t -> t.getName().equals(node.getValue()))
        .findFirst();
    if(accepted.isPresent()) {
      return accepted.get();
    }
    
    // flow returns section
    Optional<TypeDef> returns = body.getHeaders().getReturnDefs().stream()
        .filter(t -> t.getName().equals(node.getValue()))
        .findFirst();
    if(returns.isPresent()) {
      return returns.get();
    }
    
    throw new HdesException(
        ImmutableErrorNode.builder()
        .bodyId(ctx.get().body().getId().getValue())
        .target(node)
        .message("Can't find type def element: '" + node.getValue() + "'!")
        .build());
  }

  @Override
  public TypeDef visitPlaceholder(Placeholder node, HdesTree ctx) {
    if(node instanceof EmptyPlaceholder) {
      return visitEmptyPlaceholder((EmptyPlaceholder) node, ctx);
    } else if(node instanceof NamedPlaceholder) {
      return visitNamedPlaceholder((NamedPlaceholder) node, ctx);
    }
    throw new HdesException(unknownInvocation(node));
  }

  @Override
  public TypeDef visitEmptyPlaceholder(EmptyPlaceholder node, HdesTree ctx) {
    
    // empty placeholder from step context
    Optional<Step> step = ctx.find().node(Step.class);
    if(step.isEmpty()) {      
      throw new HdesException(
          ImmutableErrorNode.builder()
          .bodyId(ctx.get().body().getId().getValue())
          .target(node)
          .message("Incorrect use of placeholder, it's not available in the given context!")
          .build());
    }
     
    Optional<TypeDef> defFromStep = visitStep(node, step.get(), ctx);
    if(defFromStep.isPresent()) {
      return defFromStep.get();
    }
    
    
    StepAction action = step.get().getAction();
    if(action instanceof IterateAction) {
      IterateAction iterate = (IterateAction) action;
      if(!iterate.getNested()) {
        ObjectDef defFromIteration = ctx.step().getDef(iterate.getStep().get());
        Optional<TypeDef> placeholder = defFromIteration.getValues().stream().filter(e -> e.getName().equals("_")).findFirst();
        if(placeholder.isPresent()) {
          return placeholder.get();
        }
      }
    }
    
    
    
    
    
    throw new HdesException(
      ImmutableErrorNode.builder()
      .bodyId(ctx.get().body().getId().getValue())
      .target(node)
      .message("Incorrect use of placeholder, it's not available in the given "
          + "step: '" + step.get().getId().getValue()+ "' "
          + "expression: '" + ctx.get().node(ExpressionNode.class).getToken().getText() + "' context!")
      .build());
  }
  
  private Optional<TypeDef> visitStep(EmptyPlaceholder node, Step step, HdesTree ctx) {
    ObjectDef stepDef = ctx.step().getDef(step);    
     
    // is the access from iteration
    Optional<StepAction> action = ctx.find().limit(Step.class).node(StepAction.class);
    
    
    // call is from iteration
    if(action.isPresent() && action.get() instanceof IterateAction) {
      // if iteration -> unwrap entity
      throw new HdesException(
          ImmutableErrorNode.builder()
          .bodyId(ctx.get().body().getId().getValue())
          .target(node)
          .message("Incorrect use of placeholder, it's not available in the given context!")
          .build());
    } 
    
    // placeholder refers to call body
    Optional<TypeDef> callDef = stepDef.getValues().stream()
        .filter(s -> s.getContext() == ContextTypeDef.STEP_CALL)
        .filter(s -> s.getName().equals("_"))
        .findFirst();
    if(callDef.isPresent()) {
      return callDef;
    }
    
    // place holder refers to end mapping in the step
    return stepDef.getValues().stream()
        .filter(s -> s.getContext() == ContextTypeDef.STEP_END)
        .findFirst();
  }

  @Override
  public TypeDef visitNamedPlaceholder(NamedPlaceholder node, HdesTree ctx) {
    Optional<Step> step = ctx.find().node(Step.class);
    if(step.isEmpty()) {
      throw new HdesException(
          ImmutableErrorNode.builder()
          .bodyId(ctx.get().body().getId().getValue())
          .target(node)
          .message("Can't find step for placeholder: '" + node.getValue() + "'!")
          .build()); 
    }
    
    ObjectDef stepDef = ctx.step().getDef(step.get());
    
    // default place
    Optional<TypeDef> def = stepDef.getValues().stream().filter(t -> t.getName().equals("_")).findFirst();
    if(def.isPresent()) {
      StepCallDef type = (StepCallDef) def.get();
      Optional<TypeDef> value = type.getValues().stream().filter(t -> t.getName().equals(node.getValue())).findFirst();
      if(value.isPresent()) {
        return value.get();
      }
      
    } else {
      Optional<TypeDef> callDefForOrder = stepDef.getValues().stream().filter(t -> t.getName().equals("_" + node.getValue())).findFirst();
      if(callDefForOrder.isPresent()) {
        return callDefForOrder.get();
      }
    }
    
    
    throw new HdesException(
        ImmutableErrorNode.builder()
        .bodyId(ctx.get().body().getId().getValue())
        .target(node)
        .message("Unknown placeholder: '" + node.getValue() + "'!")
        .build()); 
  }
  
  private String unknownInvocation(HdesNode ast) {
    return new StringBuilder()
        .append("Unknown invocation type!").append(System.lineSeparator())
        .append(" AST: ").append(ast.getClass()).append(System.lineSeparator())
        .append("  - ").append(ast).append("!")
        .toString();
  }
}
