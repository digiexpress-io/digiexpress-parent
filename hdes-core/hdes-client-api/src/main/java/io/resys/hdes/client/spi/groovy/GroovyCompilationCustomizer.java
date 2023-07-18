package io.resys.hdes.client.spi.groovy;

/*-
 * #%L
 * hdes-script
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import java.lang.reflect.Modifier;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;

import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstService.ServiceExecutorType0;
import io.resys.hdes.client.api.ast.AstService.ServiceExecutorType1;
import io.resys.hdes.client.api.ast.AstService.ServiceExecutorType2;
import io.resys.hdes.client.api.programs.Program.ProgramContext;
import io.resys.hdes.client.api.programs.ServiceData.ServiceRef;

public class GroovyCompilationCustomizer extends CompilationCustomizer {
  private final ClassNode type0Node = ClassHelper.make(ServiceExecutorType0.class);
  private final ClassNode type1Node = ClassHelper.make(ServiceExecutorType1.class);
  private final ClassNode type2Node = ClassHelper.make(ServiceExecutorType2.class);
  private final ClassNode annotationNode = ClassHelper.make(ServiceRef.class);
  private final ClassNode bodyTypeNode = ClassHelper.make(AstBodyType.class);
  
  public GroovyCompilationCustomizer() {
    super(CompilePhase.CONVERSION);
 }
  
  
  @Override
  public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
    int length = 0;
    ClassNode inputType1 = null;
    ClassNode inputType2 = null;
    ClassNode returnType = null;
    
    for(org.codehaus.groovy.ast.MethodNode method : classNode.getMethods()) {
      final var isExecute = method.getName().equals("execute") && Modifier.isPublic(method.getModifiers()) && !Modifier.isVolatile(method.getModifiers());
      if(!isExecute) {
        continue;
      }

      length = method.getParameters().length;
      if(length == 0) {
        
      } else if(length == 1) {
        inputType1 = method.getParameters()[0].getType();
      } else if(length == 2) {
        inputType1 = method.getParameters()[0].getType();
        inputType2 = method.getParameters()[1].getType();
      } else {
        return;
      }
      returnType = method.getReturnType();
      break;
    }
    
    if(returnType == null) {
      return;
    }
    
    
    final ClassNode type;
    final GenericsType[] types;
    final boolean isContext;
    if(length == 0) {
      isContext = false;
      type = type0Node;
      types = new GenericsType[] {
          new GenericsType(returnType)
      };
    } else if(length == 1) {
      isContext = isContext(inputType1);
      type = type1Node;
      types = new GenericsType[] {
          new GenericsType(inputType1),
          new GenericsType(returnType)
      };
    } else {
      isContext = isContext(inputType1, inputType2);
      type = type2Node;
      types = new GenericsType[] {
          new GenericsType(inputType1),
          new GenericsType(inputType2),
          new GenericsType(returnType)
      };
    }
    
    //@ServiceRef( type=AstBodyType.DT, value="s" )
    if(isContext) {
      final var refs = new RefsParser(classNode).visit();
      for(final var ref : refs) {
        AnnotationNode node = new AnnotationNode(this.annotationNode);
        node.addMember("value", new ConstantExpression(ref.getRefValue()));
        node.addMember("type", new PropertyExpression(
            new VariableExpression(AstBodyType.class.getName(), bodyTypeNode), 
            new ConstantExpression(ref.getBodyType().name())));
        classNode.addAnnotation(node);
      }
      
      if(!refs.isEmpty()) {
        source.getAST().addImport(AstBodyType.class.getName(), bodyTypeNode);
      }
    }
    
    
    //script16332575927621894006461.groovy: 24: A transform used a generics containing ClassNode io.resys.hdes.client.api.execution.Service$ServiceExecutorType0 <Integer> 
    //for the super class io.resys.wrench.assets.bundle.groovy.businesslogic.RuleGroup2 directly. 
    //You are not supposed to do this. Please create a new ClassNode referring to the old ClassNode and use the new ClassNode instead of the old one. 
    //Otherwise the compiler will create wrong descriptors and a potential NullPointerException in TypeResolver in the OpenJDK. 
    //If this is not your own doing, please report this bug to the writer of the transform.
    

    classNode.addInterface(GenericsUtils.makeClassSafeWithGenerics(type, types));
  }
  
  
  private boolean isContext(ClassNode ... inputTypes) {
    for(ClassNode node : inputTypes) {
      if(node.getName().equals(ProgramContext.class.getSimpleName())) {
        return true;    
      }
    }
    return false;
  }
  

  public static class UnknownInputTypeException extends RuntimeException {
    private static final long serialVersionUID = -5119010536538764035L;

    public UnknownInputTypeException(String message) {
      super(message);
    }
  }
}
