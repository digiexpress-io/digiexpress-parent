package io.resys.hdes.compiler.spi.fl.visitors.mapping;

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

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.CallDef;
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.StepAs;
import io.resys.hdes.ast.api.nodes.HdesTree;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServiceBody;
import io.resys.hdes.ast.api.visitors.FlowBodyVisitor.MappingEvent;
import io.resys.hdes.compiler.api.HdesCompilerException;

public class FlowMappingFactory {
  
  public static CodeBlock from(EndPointer end, HdesTree tree) {
    return new EndMappingDefVisitor().visitBody(end, tree);
  }
  
  public static CodeBlock from(StepAs stepAs, HdesTree tree) {
    return new StepAsMappingDefVisitor().visitBody(stepAs, tree);
  }

  public static CodeBlock from(CallDef def, HdesTree ctx) {
    final var dependencyId = def.getId().getValue();
    final var dependencyNode = ctx.getRoot().getBody(dependencyId); 
    
    if(dependencyNode instanceof DecisionTableBody) {
      return new DecisionTableDefMappingDefVisitor().visitBody(def, ctx);
    } else if(dependencyNode instanceof FlowBody) {
      return new FlowCallDefMappingDefVisitor().visitBody(def, ctx);
    } else if(dependencyNode instanceof ServiceBody) {
      return new ServiceCallDefMappingDefVisitor().visitBody(def, ctx);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(def));
  }
  
  public static CodeBlock from(CallDef def, MappingEvent event, HdesTree ctx) {
    final var dependencyId = def.getId().getValue();
    final var dependencyNode = ctx.getRoot().getBody(dependencyId); 
    if(dependencyNode instanceof ServiceBody) {
      return new ServiceCallDefMappingDefVisitor().visitBody(def, event, ctx);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpression(def));
  }
}
