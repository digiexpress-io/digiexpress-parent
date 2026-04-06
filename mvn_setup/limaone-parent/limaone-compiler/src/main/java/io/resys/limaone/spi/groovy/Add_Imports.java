package io.resys.limaone.spi.groovy;

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;



public class Add_Imports extends CompilationCustomizer {


  private final List<ClassNode> imports = Arrays.asList(
      ClassHelper.make(java.time.LocalDate.class),
      ClassHelper.make(java.time.Period.class),
      ClassHelper.make(java.time.format.DateTimeFormatter.class),
      ClassHelper.make(java.time.DayOfWeek.class),
      ClassHelper.make(java.time.Duration.class),
      ClassHelper.make(java.util.Collections.class),
      ClassHelper.make(java.util.ArrayList.class),
      ClassHelper.make(java.util.Arrays.class),
      ClassHelper.make(java.util.List.class),
      ClassHelper.make(java.util.Optional.class),
      ClassHelper.make(java.util.Objects.class),
      ClassHelper.make(java.io.Serializable.class),

      ClassHelper.make(io.smallrye.mutiny.Uni.class),
      ClassHelper.make(io.smallrye.mutiny.Multi.class),

      ClassHelper.make(io.resys.limaone.program.Runtime.class),
      ClassHelper.make(io.resys.limaone.program.ProgramInput.class),

      ClassHelper.make(io.resys.limaone.model.FlowTask.class)
      //io.resys.limaone.model.FlowTask.ServiceRef.class),
      //io.resys.limaone.model.FlowTask.ServiceRefs.class),
      //io.resys.limaone.model.FlowTask.ServiceData.class),

      );


  public Add_Imports() {
    super(CompilePhase.CONVERSION);
  }


  @Override
  public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
    final var ast = source.getAST();
    for(final var node : imports) {
      if(ast.getImport(node.getNameWithoutPackage()) == null) {
        ast.addImport(node.getNameWithoutPackage(), node);
      }
    }
  }
}
