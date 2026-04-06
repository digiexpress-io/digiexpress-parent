package io.resys.limaone.spi.groovy;

import java.io.Serializable;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;

import io.resys.limaone.model.FlowTask.ServiceData;
import io.resys.limaone.program.FlowTaskProgram.ServiceExecutorType0;
import io.resys.limaone.program.FlowTaskProgram.ServiceExecutorType1;
import io.resys.limaone.program.FlowTaskProgram.ServiceExecutorType2;



public class Add_Executor extends CompilationCustomizer {
  private final ClassNode serializableNode = ClassHelper.make(Serializable.class);
  private final ClassNode type0Node = ClassHelper.make(ServiceExecutorType0.class);
  private final ClassNode type1Node = ClassHelper.make(ServiceExecutorType1.class);
  private final ClassNode type2Node = ClassHelper.make(ServiceExecutorType2.class);

  public Add_Executor() {
    super(CompilePhase.CONVERSION);
  }


  @Override
  public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {

    final var method = JavaClassMeta.findExecuteMethod(classNode);
    if(method == null) {
      return;
    }
    final ClassNode returnType = method.getReturnType();
    final var length = method.getParameters().length;


    ClassNode inputType1 = null;
    ClassNode inputType2 = null;
    if(length == 1) {
      inputType1 = method.getParameters()[0].getType();
    } else if(length == 2) {
      inputType1 = method.getParameters()[0].getType();
      inputType2 = method.getParameters()[1].getType();
    }

    final ClassNode type;
    final GenericsType[] types;

    if(length == 0) {
      type = type0Node;
      types = new GenericsType[] {
          new GenericsType(returnType)
      };
    } else if(length == 1) {
      type = type1Node;
      types = new GenericsType[] {
          new GenericsType(inputType1),
          new GenericsType(returnType)
      };
    } else {
      type = type2Node;
      types = new GenericsType[] {
          new GenericsType(inputType1),
          new GenericsType(inputType2),
          new GenericsType(returnType)
      };
    }

    classNode.getInnerClasses().forEachRemaining(this::enrichPojos);
    
    //script16332575927621894006461.groovy: 24: A transform used a generics containing ClassNode io.resys.hdes.client.api.execution.Service$ServiceExecutorType0 <Integer> 
    //for the super class io.resys.wrench.assets.bundle.groovy.businesslogic.RuleGroup2 directly. 
    //You are not supposed to do this. Please create a new ClassNode referring to the old ClassNode and use the new ClassNode instead of the old one. 
    //Otherwise the compiler will create wrong descriptors and a potential NullPointerException in TypeResolver in the OpenJDK. 
    //If this is not your own doing, please report this bug to the writer of the transform.    
    classNode.addInterface(GenericsUtils.makeClassSafeWithGenerics(type, types));
  }

    
  private void enrichPojos(InnerClassNode source) {
    if(!(source.getNameWithoutPackage().endsWith("$Input") || source.getNameWithoutPackage().endsWith("$Output"))) {
      return;
    }

    addSerializable(source);
    addServiceData(source);
  }
  
  private void addServiceData(InnerClassNode source) {
    for(final var junk : source.getAnnotations()) {
      if(junk.getClassNode().getName().equals(ServiceData.class.getSimpleName())) {
        return;
      }
    }
    
    final var uncachedtype = ClassHelper.make(io.resys.limaone.model.FlowTask.ServiceData.class.getCanonicalName());
    source.addAnnotation(uncachedtype);
  }
  private void addSerializable(InnerClassNode source) {
    for(final var junk : source.getInterfaces()) {
      if(junk.getNameWithoutPackage().equals(Serializable.class.getSimpleName())) {
        return;
      }
    }
    source.addInterface(serializableNode);
  }
}
