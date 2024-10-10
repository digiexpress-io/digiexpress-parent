package io.resys.hdes.ast.spi.antlr.visitors.returntype.flowstep;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.HdesException;
import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.BodyNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.BodyNode.TypeDef;
import io.resys.hdes.ast.api.nodes.FlowNode.CallAction;
import io.resys.hdes.ast.api.nodes.FlowNode.CallDef;
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
import io.resys.hdes.ast.api.nodes.ImmutableErrorNode;
import io.resys.hdes.ast.api.nodes.ImmutableObjectDef;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.FlowStepVisitor;
import io.resys.hdes.ast.spi.antlr.visitors.returntype.flowstep.FlowStepEndFinder.FlowStepEndSpec;
import io.resys.hdes.ast.spi.util.Assertions;

public class FlowStepEndFinder implements FlowStepVisitor<FlowStepEndSpec, FlowStepEndSpec> {

  private final List<String> visited = new ArrayList<>();
  private final FlowStepMappingDef mappingDef = new FlowStepMappingDef();
  
  @Value.Immutable
  public interface FlowStepEndSpec {
    List<TypeDef> getValues();
  }
  
  public static TypeDef merge(List<TypeDef> defs, HdesTree ctx) {
    Iterator<TypeDef> iterator = defs.iterator();
    TypeDef merged = iterator.next();
    while(iterator.hasNext()) {
      TypeDef next = iterator.next();
      merged = merge(merged, next, ctx);
    }
    return merged;
  }

  private static TypeDef merge(TypeDef def0, TypeDef def1, HdesTree ctx) {
    Function<TypeDef, String> toName = (t) -> t instanceof ScalarDef ? ((ScalarDef) t).getName() : "OBJECT";
    
    if(def0.getArray() != def1.getArray()) {
      Function<TypeDef, String> toArrayName = (t) -> t.getArray() ? "ARRAY " + toName.apply(t) : toName.apply(t);
      throw new HdesException(
          ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(def0)
            .message("Steps lead to 'end-as' that have incompadible type def: '" + toArrayName.apply(def0)  + "' vs: " + toArrayName.apply(def1) + " type!")
            .build(),
          ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(def1)
            .message("Steps lead to 'end-as' that have incompadible type def: '" + toArrayName.apply(def1)  + "' vs: " + toArrayName.apply(def0) + " type!")
            .build()); 
    }
    
    if(def0 instanceof ScalarDef && def1 instanceof ScalarDef) {
      return merge((ScalarDef) def0, (ScalarDef) def1, ctx);
    } else if(def0 instanceof ObjectDef && def1 instanceof ObjectDef) {
      return merge((ObjectDef) def0, (ObjectDef) def1, ctx);
    }
    throw new HdesException(
        ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(def0)
          .message("Steps lead to 'end-as' that have incompadible type def: '" + toName.apply(def0)  + "' vs: " + toName.apply(def1) + " type!")
          .build(),
        ImmutableErrorNode.builder()
          .bodyId(ctx.get().bodyId())
          .target(def1)
          .message("Steps lead to 'end-as' that have incompadible type def: '" + toName.apply(def1)  + "' vs: " + toName.apply(def0) + " type!")
          .build()); 
  }

  private static TypeDef merge(ScalarDef def0, ScalarDef def1, HdesTree ctx) {
    Assertions.isTrue(def0.getName().equals(def1.getName()), () -> "Can't merge scalars: '"+ def0.getName() + "', '" + def1.getName() + "' because they have different names!");
    if(def0.getType() != def1.getType()) {
      throw new HdesException(
          ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(def0)
            .message("Steps lead to 'end-as' that have incompadible type:'" + def0.getName() + "' def: '" + def0.getType() + "' vs: " + def1.getType() + " type!")
            .build(),
          ImmutableErrorNode.builder()
            .bodyId(ctx.get().bodyId())
            .target(def1)
            .message("Steps lead to 'end-as' that have incompadible type:'" + def1.getName() + "' def: '" + def1.getType() + "' vs: " + def0.getType() + " type but was expecting !")
            .build()); 
    }
    return def0;
  }
  
  private static TypeDef merge(ObjectDef def0, ObjectDef def1, HdesTree ctx) {
    Assertions.isTrue(def0.getName().equals(def1.getName()), () -> "Can't merge objects: '"+ def0.getName() + "', '" + def1.getName() + "' because they have differentName");
    
    Map<String, TypeDef> defs = new HashMap<>();
    def0.getValues().forEach(t -> defs.put(t.getName(), t));
    
    for(TypeDef def : def1.getValues()) {
      final var name = def.getName();
      final TypeDef merged;
      
      if(defs.containsKey(name)) {
        merged = merge(defs.get(name), def, ctx);
      } else {
        merged = def;
      }
      
      defs.put(name, merged);
    }
    return ImmutableObjectDef.builder()
        .name(def0.getName())
        .array(def0.getArray())
        .required(false)
        .context(def0.getContext())
        .token(def0.getToken())
        .addAllValues(defs.values())
        .build();
  }
  
  @Override
  public FlowStepEndSpec visitEndPointer(EndPointer pointer, HdesTree ctx) {
    return ImmutableFlowStepEndSpec.builder().addValues(mappingDef.visitBody(pointer, ctx)).build();
  }

  @Override
  public FlowStepEndSpec visitBody(Step step, HdesTree ctx) {
    if(visited.contains(step.getId().getValue())) {
      return ImmutableFlowStepEndSpec.builder().build();
    }
    visited.add(step.getId().getValue());
    final var next = ctx.next(step);
    return visitPointer(step.getPointer(), next);
  }

  @Override
  public FlowStepEndSpec visitPointer(StepPointer pointer, HdesTree ctx) {
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
    throw new HdesException(unknownAst(pointer));
  }

  @Override
  public FlowStepEndSpec visitSplitPointer(SplitPointer pointer, HdesTree ctx) {
    final var result = ImmutableFlowStepEndSpec.builder();
    final var next = ctx.next(pointer);
    for(StepPointer child : pointer.getValues()) {
      result.addAllValues(visitPointer(child, next).getValues());
    }
    return result.build();
  }

  @Override
  public FlowStepEndSpec visitWhenPointer(WhenPointer pointer, HdesTree ctx) {
    final var next = ctx.next(pointer);
    return visitPointer(pointer.getThen(), next);
  }

  @Override
  public FlowStepEndSpec visitThenPointer(ThenPointer pointer, HdesTree ctx) {
    final var next = ctx.next(pointer);
    return visitBody(pointer.getStep(), next);
  }

  private String unknownAst(HdesNode ast) {
    return new StringBuilder()
        .append("Unknown AST: ").append(ast.getClass())
        .append("  - ").append(ast).append(System.lineSeparator())
        .toString();
  }
  
  @Override
  public FlowStepEndSpec visitIterationEndPointer(IterationEndPointer pointer, HdesTree ctx) {
    return ImmutableFlowStepEndSpec.builder().build();
  }
  @Override
  public FlowStepEndSpec visitAction(StepAction action, HdesTree ctx) {
    throw new HdesException(unknownAst(action));
  }
  @Override
  public FlowStepEndSpec visitCallAction(CallAction action, HdesTree ctx) {
    throw new HdesException(unknownAst(action));
  }
  @Override
  public FlowStepEndSpec visitCallDef(CallDef def, HdesTree ctx) {
    throw new HdesException(unknownAst(def));
  }
  @Override
  public FlowStepEndSpec visitIterateAction(IterateAction action, HdesTree ctx) {
    throw new HdesException(unknownAst(action));
  }
  @Override
  public FlowStepEndSpec visitStepAs(StepAs stepAs, HdesTree ctx) {
    throw new HdesException(unknownAst(stepAs));
  }
}
