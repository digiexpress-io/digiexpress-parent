package io.resys.limaone.spi.compiler;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.DialobForm;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.program.Program;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.limaone.spi.program.DialobProgramImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Dialob implements CompilableUnit {
  private final FormDb formDb;
  private final ScheduledExecutorService workerPool;
  private final Duration maxTimeout;
  private final AST_Parser parser;
  private final ModelWorld world;
  private final Model<DialobForm> target;
  
  @Override
  public OpenProgram compile(NewArtifact resolution) {
    
    final var ast = parser.parseDialobForm().model(target).parse();
    resolution.ast(ast).id(target.getId()).name(ast.getName()).build();
    
    return new OpenProgram() {
      @Override
      public Simple_AST getAst() {
        return ast;
      }
      @Override
      public Program close(Artifact artifact) {
        return new DialobProgramImpl(
            formDb, workerPool, maxTimeout,
            target,
            ast, 
            artifact.getErrors().isEmpty() ? artifact.getProgramStatus() : ProgramStatus.ERROR,
            artifact.getErrors(), 
            artifact.getAssociations());
      }
    };
  }
}
