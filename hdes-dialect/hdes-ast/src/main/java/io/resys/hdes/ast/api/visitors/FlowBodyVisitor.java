package io.resys.hdes.ast.api.visitors;

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
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.HdesTree.FlowTree;
import io.resys.hdes.ast.api.nodes.MappingNode.ExpressionMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.FastMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.FieldMappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.MappingDef;
import io.resys.hdes.ast.api.nodes.MappingNode.ObjectMappingDef;

public interface FlowBodyVisitor<T, R> {
  R visitBody(FlowTree ctx); 
  T visitHeaders(Headers node, HdesTree ctx);
  T visitHeader(TypeDef node, HdesTree ctx);
  T visitHeader(ScalarDef node, HdesTree ctx);
  T visitHeader(ObjectDef node, HdesTree ctx);
  T visitStep(Step step, HdesTree ctx);


  // fl
  interface FlowStepVisitor<T, R> extends FlowPointerVisitor<T, R> {
    R visitBody(Step step, HdesTree ctx);
    
    T visitAction(StepAction action, HdesTree ctx);
    T visitCallAction(CallAction action, HdesTree ctx);
    T visitCallDef(CallDef def, HdesTree ctx);
    T visitIterateAction(IterateAction action, HdesTree ctx);
    T visitStepAs(StepAs stepAs, HdesTree ctx);
  }
  
  // fl
  interface FlowPointerVisitor<T, R> {
    T visitPointer(StepPointer pointer, HdesTree ctx);
    T visitSplitPointer(SplitPointer pointer, HdesTree ctx);
    T visitWhenPointer(WhenPointer pointer, HdesTree ctx);
    T visitThenPointer(ThenPointer pointer, HdesTree ctx);
    T visitEndPointer(EndPointer pointer, HdesTree ctx);
    T visitIterationEndPointer(IterationEndPointer pointer, HdesTree ctx);
  }
  
  interface FlowMappingDefVisitor<T, R> {
    R visitBody(EndPointer node, HdesTree ctx);
    R visitBody(StepAs def, HdesTree ctx);
    R visitBody(CallDef def, HdesTree ctx);
    R visitBody(CallDef def, MappingEvent event, HdesTree ctx);
    
    T visitMappingDef(MappingDef node, HdesTree ctx);
    T visitExpressionMappingDef(ExpressionMappingDef node, HdesTree ctx);
    T visitFastMappingDef(FastMappingDef node, HdesTree ctx);
    T visitFieldMappingDef(FieldMappingDef node, HdesTree ctx);
    T visitObjectMappingDef(ObjectMappingDef node, HdesTree ctx);
  }
  
  enum MappingEvent {
    ON_ENTER, ON_COMPLETE, ON_ERROR, ON_TIMEOUT
  }
}
