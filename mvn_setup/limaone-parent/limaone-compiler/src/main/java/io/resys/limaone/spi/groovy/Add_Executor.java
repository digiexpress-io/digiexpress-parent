package io.resys.limaone.spi.groovy;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;

import io.resys.limaone.program.FlowTaskProgram.ServiceExecutorType0;
import io.resys.limaone.program.FlowTaskProgram.ServiceExecutorType1;
import io.resys.limaone.program.FlowTaskProgram.ServiceExecutorType2;



public class Add_Executor extends CompilationCustomizer {
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

    //script16332575927621894006461.groovy: 24: A transform used a generics containing ClassNode io.resys.hdes.client.api.execution.Service$ServiceExecutorType0 <Integer> 
    //for the super class io.resys.wrench.assets.bundle.groovy.businesslogic.RuleGroup2 directly. 
    //You are not supposed to do this. Please create a new ClassNode referring to the old ClassNode and use the new ClassNode instead of the old one. 
    //Otherwise the compiler will create wrong descriptors and a potential NullPointerException in TypeResolver in the OpenJDK. 
    //If this is not your own doing, please report this bug to the writer of the transform.    
    classNode.addInterface(GenericsUtils.makeClassSafeWithGenerics(type, types));
  }
}
