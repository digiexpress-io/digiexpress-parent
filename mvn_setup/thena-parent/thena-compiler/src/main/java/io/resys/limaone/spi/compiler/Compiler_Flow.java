package io.resys.limaone.spi.compiler;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.program.Program;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.spi.program.FlowProgramImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Flow implements CompilableUnit {
  private final AST_Parser parser;
  private final ScheduledExecutorService workerPool;
  private final Duration maxTimeout;
  @SuppressWarnings("unused")
  private final ModelWorld world;
  private final Model<Flow> flow;
  
  @Override
  public OpenProgram compile(NewArtifact resolution) {
    
    final Flow_AST ast = parser.parseFlow()
        .syntax(flow.getBody().getFlowValue())
        .onDependency(dep -> resolution.requireDependnecy(dep))
        .parse();
    resolution.ast(ast).id(flow.getId()).name(ast.getName()).build();
    
    return new OpenProgram() {
      @Override
      public Simple_AST getAst() {
        return ast;
      }
      @Override
      public Program close(Artifact artifact) {
        final var extraErrors = new Compiler_FlowDepsValidator(artifact, ast).walk();
        return new FlowProgramImpl(
            flow.getId(), workerPool, maxTimeout, ast, 
            artifact.getErrors().isEmpty() ? artifact.getProgramStatus() : ProgramStatus.ERROR, 
            Stream.concat(artifact.getErrors().stream(), extraErrors.stream()).toList(), 
            artifact.getAssociations());
      }
    };
  }
}
