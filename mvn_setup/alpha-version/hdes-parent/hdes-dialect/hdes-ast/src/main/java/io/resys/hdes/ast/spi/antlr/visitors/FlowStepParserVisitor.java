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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.HdesException;
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
import io.resys.hdes.ast.api.nodes.ImmutableIterateAction;
import io.resys.hdes.ast.api.nodes.ImmutableSplitPointer;
import io.resys.hdes.ast.api.nodes.ImmutableStep;
import io.resys.hdes.ast.api.nodes.ImmutableThenPointer;
import io.resys.hdes.ast.api.nodes.ImmutableWhenPointer;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.FlowStepVisitor;
import io.resys.hdes.ast.spi.antlr.visitors.FlowParserVisitor.FwRedundentContinuePointer;
import io.resys.hdes.ast.spi.antlr.visitors.FlowParserVisitor.FwRedundentEmptyPointer;
import io.resys.hdes.ast.spi.antlr.visitors.FlowParserVisitor.FwRedundentThenPointer;
import io.resys.hdes.ast.spi.antlr.visitors.FlowParserVisitor.RedundentIterateAction;
import io.resys.hdes.ast.spi.antlr.visitors.FlowStepParserVisitor.StepTree;

public class FlowStepParserVisitor implements FlowStepVisitor<HdesNode, StepTree> {
  private final List<Step> src;
  private final List<Step> unclaimed = new ArrayList<>();
  private final Map<String, Integer> index = new HashMap<>();
  
  
  public FlowStepParserVisitor(List<Step> src) {
    this.src = src;
    int id = 0;
    for(var step : src) {
      if(index.containsKey(step.getId().getValue())) {
        // error case
        index.put(step.getId().getValue() + id, id++);        
      } else {
        index.put(step.getId().getValue(), id++);
      }

    } 
  }
  
  @Override
  public StepTree visitBody(Step step, HdesTree ctx) {
    HdesTree next = ctx.next(step);
    Step clone = ImmutableStep.builder().from(step)
      .action(visitAction(step.getAction(), next))
      .pointer(visitPointer(step.getPointer(), next))
      .build();
    
    return ImmutableStepTree.builder().step(clone).unclaimed(unclaimed).build();
  }

  @Override
  public StepAction visitAction(StepAction action, HdesTree ctx) {
    if(!(action instanceof RedundentIterateAction)) {
      return action;
    }
    
    RedundentIterateAction iterate = (RedundentIterateAction) action;
    final Optional<Step> step;
    boolean nested = true;
    if(iterate.getSteps().isEmpty()) {
      step = Optional.empty();
    } else {
      StepTree tree = builder().src(iterate.getSteps()).ctx(ctx.next(action)).build();
      nested = !(tree.getStep().get().getPointer() instanceof IterationEndPointer);
      step = tree.getStep();
      this.unclaimed.addAll(tree.getUnclaimed());
    }
    
    return ImmutableIterateAction.builder()
        .token(iterate.getToken())
        .over(iterate.getOver())
        .step(step)
        .nested(nested)
        .build();
  }

  @Override
  public StepPointer visitPointer(StepPointer pointer, HdesTree ctx) {
    if(pointer instanceof SplitPointer) {
      return visitSplitPointer((SplitPointer) pointer, ctx);
    } else if(pointer instanceof EndPointer) {
      return visitEndPointer((EndPointer) pointer, ctx);
    } else if(pointer instanceof WhenPointer) {
      return visitWhenPointer((WhenPointer) pointer, ctx);
    } else if(pointer instanceof IterationEndPointer) {
      return visitIterationEndPointer((IterationEndPointer) pointer, ctx);
    } else if(pointer instanceof FwRedundentEmptyPointer) {
      throw new HdesException(ImmutableErrorNode.builder()
          .target(pointer)
          .bodyId(ctx.get().bodyId())
          .message("Step: '" + ctx.get().node(Step.class).getId().getValue() + "' is missing 'then' definition!")
          .build());
    
    } else if(pointer instanceof FwRedundentContinuePointer) {
      Step redundent = ctx.get().node(Step.class);
      int nextIndex = index.get(redundent.getId().getValue()) + 1;
      Optional<String> then = this.index.entrySet().stream()
          .filter(e -> e.getValue().equals(nextIndex)).map(e -> e.getKey())
          .findFirst();
      
      Optional<Step> step = src.stream().filter(s -> s.getId().getValue().equals(then.get())).findFirst();
      
      if(step.isPresent()) {
        StepTree subtree = visitBody(step.get(), ctx.next(pointer));
        unclaimed.addAll(subtree.getUnclaimed());
        return ImmutableThenPointer.builder()
          .token(pointer.getToken())
          .id(redundent.getId())
          .step(subtree.getStep().get())
          .build();
      } else {
        throw new HdesException(ImmutableErrorNode.builder()
            .target(pointer)
            .bodyId(ctx.get().bodyId())
            .message("Can't find step defined in then: " + then + "!")
            .build());
      }
    
    } else if(pointer instanceof FwRedundentThenPointer) {
      FwRedundentThenPointer redundent = (FwRedundentThenPointer) pointer;
      String then = redundent.getId().getValue();
      Optional<Step> step = src.stream().filter(s -> s.getId().getValue().equals(then)).findFirst();
      
      if(step.isPresent()) {
        StepTree subtree = visitBody(step.get(), ctx.next(pointer));
        unclaimed.addAll(subtree.getUnclaimed());
        return ImmutableThenPointer.builder()
          .token(pointer.getToken())
          .id(redundent.getId())
          .step(subtree.getStep().get())
          .build();
      } else {
        throw new HdesException(ImmutableErrorNode.builder()
            .target(pointer)
            .bodyId(ctx.get().bodyId())
            .message("Can't find step defined in then: " + then + "!")
            .build());
      }
    }
    throw new HdesException("Unknown pointer: " + pointer + "!");
  }
  

  @Override
  public SplitPointer visitSplitPointer(SplitPointer pointer, HdesTree ctx) {
    HdesTree next = ctx.next(pointer);
    return ImmutableSplitPointer.builder().from(pointer)
        .values(pointer.getValues().stream()
            .map(s -> visitPointer(s, next))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public WhenPointer visitWhenPointer(WhenPointer pointer, HdesTree ctx) {
    return ImmutableWhenPointer.builder()
        .from(pointer)
        .then(visitPointer(pointer.getThen(), ctx.next(pointer)))
        .build();
  }

  public IterationEndPointer visitIterationEndPointer(IterationEndPointer pointer, HdesTree ctx) {
    return pointer;    
  }
  
  @Override
  public EndPointer visitEndPointer(EndPointer pointer, HdesTree ctx) {
    return pointer;
  }
  
  @Override
  public CallAction visitCallAction(CallAction action, HdesTree ctx) {
    return action;
  }

  @Override
  public IterateAction visitIterateAction(IterateAction action, HdesTree ctx) {
    return action;
  }
  
  @Override
  public HdesNode visitThenPointer(ThenPointer pointer, HdesTree ctx) {
    return pointer;
  }
  
  @Override
  public CallDef visitCallDef(CallDef def, HdesTree ctx) {
    return def;
  }

  @Override
  public HdesNode visitStepAs(StepAs stepAs, HdesTree ctx) {
    return stepAs;
  }
  
  @Value.Immutable
  public interface StepTree {
    Optional<Step> getStep();
    List<Step> getUnclaimed();
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private List<Step> src;
    private HdesTree ctx;
    public Builder src(List<Step> src) {
      this.src = src;
      return this;
    }
    public Builder ctx(HdesTree ctx) {
      this.ctx = ctx;
      return this;
    }
    public StepTree build() {
      if(src == null || src.isEmpty()) {
        return ImmutableStepTree.builder().build();
      }
      Step step = src.iterator().next();
      return new FlowStepParserVisitor(src).visitBody(step, ctx);
    }
  }
}
