package io.resys.limaone.spi.groovy;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;

import io.resys.limaone.model.FlowTask.ServiceRef;
import io.resys.limaone.model.Model;


public class Add_Ref extends CompilationCustomizer {
  private final ClassNode annotationNode = ClassHelper.make(ServiceRef.class);
  private final ClassNode bodyTypeNode = ClassHelper.make(Model.BodyType.class);
  
  public Add_Ref() {
    super(CompilePhase.CONVERSION);
 }
  
  
  @Override
  public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {

    final var method = JavaClassMeta.findExecuteMethod(classNode);
    if(method == null) {
      return;
    }
    
    if(!isContext(method)) {
      return;
    }

    //@ServiceRef( type=AstBodyType.DT, value="s" )
    final var refs = new FindAllRefs(classNode).accept();
    for(final var ref : refs) {
      AnnotationNode node = new AnnotationNode(this.annotationNode);
      node.addMember("value", new ConstantExpression(ref.getRefValue()));
      node.addMember("type", new PropertyExpression(
          new VariableExpression(Model.BodyType.class.getName(), bodyTypeNode), 
          new ConstantExpression(ref.getBodyType().name())));
      classNode.addAnnotation(node);
    }
    
    if(!refs.isEmpty()) {
      source.getAST().addImport(Model.BodyType.class.getName(), bodyTypeNode);
    }
  
  }
  
  
  private boolean isContext(org.codehaus.groovy.ast.MethodNode method) {
    for(final var param : method.getParameters()) {
      if(param.getType().getName().equals(io.resys.limaone.program.Runtime.class.getSimpleName())) {
        return true;    
      }
    }
    return false;
  }
  

}
