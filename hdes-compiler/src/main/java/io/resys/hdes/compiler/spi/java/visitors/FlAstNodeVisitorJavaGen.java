package io.resys.hdes.compiler.spi.java.visitors;

import java.util.ArrayList;
import java.util.List;

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

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.Mapping;
import io.resys.hdes.ast.api.nodes.FlowNode.TaskRef;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.compiler.api.Flow.FlowExecutionLog;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.api.ImmutableFlowExecutionLog;
import io.resys.hdes.compiler.spi.NamingContext;
import io.resys.hdes.compiler.spi.java.JavaSpecUtil;
import io.resys.hdes.compiler.spi.java.visitors.FlJavaSpec.FlCodeSpec;
import io.resys.hdes.compiler.spi.java.visitors.FlJavaSpec.FlTaskImplSpec;
import io.resys.hdes.compiler.spi.java.visitors.FlJavaSpec.FlTaskRefSpec;
import io.resys.hdes.compiler.spi.java.visitors.FlJavaSpec.FlWhenThenSpec;

public class FlAstNodeVisitorJavaGen extends FlAstNodeVisitorTemplate<FlJavaSpec, TypeSpec> {
  
  private final NamingContext naming;
  private final TypeNameRef typeNameRef = (v) -> {
    String value = v.getValue();
    if(value.contains(".")) {
      return "currentState." + JavaSpecUtil.getMethodCall(value);
    }
    return "input." + JavaSpecUtil.getMethodCall(value);
  };
  
  private FlowBody body;
  private ClassName flowState;
  
  public FlAstNodeVisitorJavaGen(NamingContext naming) {
    super();
    this.naming = naming;
  }
  
  @Override
  public TypeSpec visitFlowBody(FlowBody node) {
    this.body = node;
    this.flowState = naming.fl().state(node);
    
    TypeSpec.Builder flowBuilder = TypeSpec.classBuilder(naming.fl().impl(node))
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(naming.fl().interfaze(node));

    FlTaskImplSpec taskImpl = node.getTask().map(n -> visitFlowTask(n)).orElseGet(() ->
      ImmutableFlTaskImplSpec.builder().value(CodeBlock.builder().add("// no tasks described ").build()).build()
    );
    
    MethodSpec applyMethod = MethodSpec.methodBuilder("apply")
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(naming.fl().input(node), "input").build())
        .returns(flowState)
        
        .addCode(CodeBlock.builder().addStatement("long start = System.currentTimeMillis()").add("\r\n").build())
        .addStatement("$T currentState = $T.builder().input(input).build()", flowState, naming.immutable(flowState))
        .addCode(taskImpl.getValue())
        .addCode(CodeBlock.builder().add("\r\n").addStatement("long end = System.currentTimeMillis()").build())
        
        .addCode(CodeBlock.builder()
            .add("return $T.builder()", naming.immutable(flowState))
            .add("\r\n  ").add(".from(currentState)")
            .add("\r\n  ").add(".id($S)", node.getId())
            .add("\r\n  ").add(".parent(currentState.getLog())")
            .add("\r\n  ").add(".start(start)").add(".end(end)")
            .add("\r\n  ").add(".duration(end - start)")
            .add("\r\n  ").add(".build()")
            .build())
        .build();
 
    return flowBuilder
        .addMethod(applyMethod)
        .addMethods(taskImpl.getChildren())
        .build();
  }
  
  @Override
  public FlTaskImplSpec visitFlowTask(FlowTaskNode node) {
    List<MethodSpec> children = new ArrayList<>();
    CodeBlock.Builder codeblock = CodeBlock.builder();
    
    // visit method
    if(node.getRef().isPresent()) {
      FlTaskRefSpec ref = visitTaskRef(node);
      children.add(ref.getMethod());
      codeblock.addStatement("currentState = $L(currentState)", ref.getMethod().name);
    }
    
    // next
    if(node.getNext().isPresent()) {
      FlTaskImplSpec next = visitFlowTaskPointer(node.getNext().get());
      codeblock.add(next.getValue());
      
      for(MethodSpec method : next.getChildren()) {
        if(!children.stream().filter(m -> m.name.equals(method.name)).findFirst().isPresent()) {
          children.add(method);          
        }
      }
    }
    
    return ImmutableFlTaskImplSpec.builder()
        .value(codeblock.build())
        .addAllChildren(children)
        .build();
  }
  
  @Override
  public FlTaskRefSpec visitTaskRef(FlowTaskNode parent) {
    MethodSpec.Builder visitBuilder = MethodSpec
        .methodBuilder("visit" + parent.getId())
        .addModifiers(Modifier.PRIVATE)
        .addParameter(ParameterSpec.builder(flowState, "currentState").build())
        .returns(flowState);
    
    CodeBlock logBlock = CodeBlock.builder().add("\r\n")
      .add("$T log = $T.builder()", FlowExecutionLog.class, ImmutableFlowExecutionLog.class)
      .add("\r\n  ").add(".id($S)", parent.getId())
      .add("\r\n  ").add(".parent(currentState.getLog())")
      .add("\r\n  ").add(".start(start)").add(".end(end)")
      .add("\r\n  ").add(".duration(end - start)")
      .add("\r\n  ").addStatement(".build()").build();
    
    CodeBlock returnBlock = CodeBlock.builder()
        .add("\r\n")
        .add("return $T.builder()", naming.immutable(naming.fl().state(this.body)))
        .add("\r\n  ").add(".from(currentState).input(input).output(output)")
        .add("\r\n  ").addStatement(".log(log).build()").build(); 
    
    return ImmutableFlTaskRefSpec.builder().method(
        visitBuilder
        
          .addStatement("long start = System.currentTimeMillis()")
          .addCode(visitMapping(parent).getValue())
          .addStatement("long end = System.currentTimeMillis()")
          
          .addCode(logBlock)
          .addCode(returnBlock)

          .build())
        .build();
  }
  
  @Override
  public FlCodeSpec visitMapping(FlowTaskNode node) {
    TaskRef ref = node.getRef().get();
    ClassName input = naming.fl().refInput(node.getRef().get());
    
    CodeBlock.Builder codeBlock = CodeBlock.builder()
        .add("$T input = $T.builder()", input, naming.immutable(input));
    for(Mapping mapping : ref.getMapping()) {
      String right = JavaSpecUtil.getMethodCall(mapping.getRight());
      codeBlock.add("\r\n  ").add(".$L(currentState.$L)", mapping.getLeft(), right);
    }
    
    codeBlock
    .addStatement(".build()")
    .add("$T output = ", naming.fl().refOutput(node.getRef().get()));
    
    switch (ref.getType()) {
    case DECISION_TABLE: codeBlock.addStatement("$L.apply(input)", naming.fl().refMethod(ref)); break;
    case FLOW_TASK: codeBlock.addStatement("$L.apply(input)", naming.fl().refMethod(ref)); break;
    case MANUAL_TASK: codeBlock.addStatement("$L().apply(input)", naming.fl().refMethod(ref)); break;
    case SERVICE_TASK: codeBlock.addStatement("$L().apply(input)", naming.fl().refMethod(ref)); break;
    default: throw new HdesCompilerException(HdesCompilerException.builder().unknownFlTaskRef(ref));
    }
    return ImmutableFlCodeSpec.builder().value(codeBlock.build()).build();
  }
  
  @Override
  public FlTaskImplSpec visitFlowTaskPointer(FlowTaskPointer node) {
    // if / else
    if(node instanceof WhenThenPointer) {
      WhenThenPointer pointer = (WhenThenPointer) node;
      return visitWhenThenPointer(pointer);
    }
    
    // next
    if(node instanceof ThenPointer) {
      ThenPointer then = (ThenPointer) node;
      return visitThenPointer(then);
    } 
    
    // end
    if(node instanceof EndPointer) {
      EndPointer then = (EndPointer) node;
      return visitEndPointer(then);
    } 
    throw new HdesCompilerException(HdesCompilerException.builder().unknownFlTaskPointer(node));
  
  }
  
  @Override
  public FlCodeSpec visitWhen(ExpressionBody node) {
    return ImmutableFlCodeSpec.builder()
        .value(new EnAstNodeJavaCodeBlock(typeNameRef).visitExpressionBody(node))
        .build();
  }
  
  @Override
  public FlTaskImplSpec visitThenPointer(ThenPointer node) {
    if(node.getTask().isPresent()) {
      return visitFlowTask(node.getTask().get());
    }
    return null;
  }
  
  @Override
  public FlWhenThenSpec visitWhenThen(WhenThen node) {
    FlTaskImplSpec spec = visitFlowTaskPointer(node.getThen());
    
    return ImmutableFlWhenThenSpec.builder()
        .when(node.getWhen().map(w -> visitWhen(w).getValue()))
        .then(spec)
        .build();
  }
  
  @Override
  public FlTaskImplSpec visitEndPointer(EndPointer node) {
    // TODO Auto-generated method stub
    return super.visitEndPointer(node);
  }

  @Override
  public FlTaskImplSpec visitWhenThenPointer(WhenThenPointer pointer) {
    List<MethodSpec> methods = new ArrayList<>();
    CodeBlock.Builder codeBlock = CodeBlock.builder();
    boolean first = true;
    boolean last = false;

    for(WhenThen whenThen : pointer.getValues()) {
      if(last) {
        throw new HdesCompilerException(HdesCompilerException.builder().wildcardUnknownFlTaskWhenThen(pointer));
      }

      FlWhenThenSpec spec = visitWhenThen(whenThen);
      if(first && spec.getWhen().isPresent()) {
        codeBlock.beginControlFlow("if($L)", spec.getWhen().get());
        first = false;
      } else if(spec.getWhen().isPresent()) {
        codeBlock.beginControlFlow("else if($L)", spec.getWhen().get());
      } else {
        codeBlock.beginControlFlow("else");
        last = true;
      }
      codeBlock.add(spec.getThen().getValue()).endControlFlow();
      methods.addAll(spec.getThen().getChildren());
    }
    return ImmutableFlTaskImplSpec.builder().value(codeBlock.build()).addAllChildren(methods).build();
  }
}
