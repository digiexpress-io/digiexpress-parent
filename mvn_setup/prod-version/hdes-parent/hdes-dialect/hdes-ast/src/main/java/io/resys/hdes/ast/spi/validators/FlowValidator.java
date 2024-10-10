package io.resys.hdes.ast.spi.validators;

import io.resys.hdes.ast.api.nodes.BodyNode.Headers;
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

import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.HdesTree.FlowTree;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.FlowStepVisitor;
import io.resys.hdes.ast.spi.validators.RootNodeValidator.RootNodeErrors;

public class FlowValidator implements FlowBodyVisitor<RootNodeErrors, RootNodeErrors>, FlowStepVisitor<RootNodeErrors, RootNodeErrors> {

  
  @Override
  public RootNodeErrors visitBody(FlowTree ctx) {
    ImmutableRootNodeErrors.Builder result = ImmutableRootNodeErrors.builder();
    
    
    return result.build();
  }

  @Override
  public RootNodeErrors visitBody(Step step, HdesTree ctx) {
    ImmutableRootNodeErrors.Builder result = ImmutableRootNodeErrors.builder();
    
    
    return result.build();
  }

  @Override
  public RootNodeErrors visitAction(StepAction action, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitCallAction(CallAction action, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitIterateAction(IterateAction action, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitPointer(StepPointer pointer, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitSplitPointer(SplitPointer pointer, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitWhenPointer(WhenPointer pointer, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitThenPointer(ThenPointer pointer, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitEndPointer(EndPointer pointer, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitHeaders(Headers node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitHeader(TypeDef node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitHeader(ScalarDef node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitHeader(ObjectDef node, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitStep(Step step, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitCallDef(CallDef def, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitIterationEndPointer(IterationEndPointer pointer, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RootNodeErrors visitStepAs(StepAs stepAs, HdesTree ctx) {
    // TODO Auto-generated method stub
    return null;
  }

}
