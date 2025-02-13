package io.resys.hdes.compiler.spi.units;

import org.immutables.value.Value;

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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import io.resys.hdes.ast.api.nodes.BodyNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.Step;
import io.resys.hdes.ast.api.nodes.HdesNode;
import io.resys.hdes.ast.api.nodes.ServiceNode.ServiceBody;
import io.resys.hdes.compiler.api.HdesCompiler.ResourceType;

public interface CompilerNode extends HdesNode {
  
  ServiceUnit st(ServiceBody node);
  FlowUnit fl(FlowBody node);
  DecisionTableUnit dt(DecisionTableBody node);  
  
  interface ServiceUnit extends HdesNode {
    CompilerType getType();
    ServiceBody getBody();
    
    CompilerEntry getAccepts(ObjectDef object);
    CompilerEntry getReturns(ObjectDef object);
  }
  
  interface DecisionTableUnit extends HdesNode {
    CompilerType getType();
    DecisionTableBody getBody();
    CompilerEntry getListValue();
    CompilerEntry getConstants();
  }
  
  interface FlowUnit extends HdesNode {
    CompilerType getType();
    FlowBody getBody();
    
    CompilerEntry getEndAs(Step object);
    CompilerEntry getAccepts(ObjectDef object);
    CompilerEntry getReturns(ObjectDef object);
    
    /*
    TaskRefNaming ref(StepCall ref);
    ClassName stateValue();
    ClassName stateValue(Step task, LoopPointer loop);
    ClassName inputValue(ObjectDef object);
    ClassName outputValue(ObjectDef object);*/
  }
  
  @Value.Immutable
  interface CompilerType {
    String getPkg();
    
    // api and implementation
    CompilerEntry getApi();
    CompilerEntry getImpl();

    // input/output
    CompilerEntry getAccepts();
    CompilerEntry getReturns();

    // Main method return type
    CompilerEntry getReturnType();
    
    ResourceType getSourceType();
  }
  
  
  @Value.Immutable
  interface CompilerEntry {
    ClassName getName();
    TypeName getSuperinterface();
  }
  
  @Value.Immutable
  interface TaskRefNaming {
    Boolean getArray();
    CompilerType getType();
    
    ClassName getMeta();
  }
  
}
