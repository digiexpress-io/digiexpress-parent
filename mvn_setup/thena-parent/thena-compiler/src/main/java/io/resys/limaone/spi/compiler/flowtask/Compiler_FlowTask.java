package io.resys.limaone.spi.compiler.flowtask;

import org.apache.commons.lang3.Validate;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.FlowTask_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.FlowTask.FlowTaskExecutable;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.program.Program;
import io.resys.limaone.spi.compiler.CompilableUnit;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_FlowTask implements CompilableUnit {
  private final AST_Parser parser;
  private final ModelWorld world;
  private final Model<FlowTask> flowTask;
  
  @Override
  public OpenProgram compile(NewArtifact resolution) {
    final FlowTask_AST ast = parser.parseFlowTask().id(flowTask.getId()).syntax(flowTask.getBody().getTaskValue()).parse();
    
    return new OpenProgram() {
      @Override
      public String getId() {
        return ast.getId();
      }
      @Override
      public Simple_AST getAst() {
        return ast;
      }
      
      @Override
      public Program close(Artifact artifact) {
        final var constructors = ast.getBeanType().getConstructors();
        Validate.isTrue(constructors.length == 1, () -> "There can be only one constructor for flow task, expected: 1 actual: " + constructors.length);
        
        final FlowTaskExecutable beanInstance = (FlowTaskExecutable) constructors[0].newInstance();
        
        final var flowTask = new ImmutableFlowTaskProgram(ast);
        
        return null;
      }
    };
  }
}
