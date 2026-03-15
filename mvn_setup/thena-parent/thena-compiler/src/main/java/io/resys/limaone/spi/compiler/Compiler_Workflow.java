package io.resys.limaone.spi.compiler;

import java.util.stream.Stream;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.ArticleWorkflow_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.Model;
import io.resys.limaone.program.Program;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.spi.program.WorkflowProgramImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Workflow implements CompilableUnit {
  private final AST_Parser parser;
  private final Model<ArticleWorkflow> target;
  
  @Override
  public OpenProgram compile(NewArtifact resolution) {
    
    
    final ArticleWorkflow_AST ast = parser.parseArticleWorkflow()
        .model(target)
        .onDependency(dep -> resolution.requireDependnecy(dep))
        .parse();
    resolution.ast(ast).id(target.getId()).name(ast.getName()).build();
    
    return new OpenProgram() {
      @Override
      public Simple_AST getAst() {
        return ast;
      }
      @Override
      public Program close(Artifact artifact) {
        final var extraErrors = new Compiler_WorkflowDepsValidator(artifact, ast).walk();
        
        return new WorkflowProgramImpl(
            target,
            ast, 
            artifact.getErrors().isEmpty() ? artifact.getProgramStatus() : ProgramStatus.ERROR,
            Stream.concat(artifact.getErrors().stream(), extraErrors.stream()).toList(), 
            artifact.getAssociations());
      }
    }; 
  }
}
