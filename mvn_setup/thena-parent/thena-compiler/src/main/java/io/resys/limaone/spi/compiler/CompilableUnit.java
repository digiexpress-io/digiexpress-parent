package io.resys.limaone.spi.compiler;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.limaone.ast.AST_Parser.Dependency_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.program.Compiler.BundleBuilder;
import io.resys.limaone.program.Program;
import io.resys.limaone.program.Program.ProgramAssociation;
import io.resys.limaone.program.Program.ProgramStatus;
import io.smallrye.mutiny.Uni;

public interface CompilableUnit {
  OpenProgram compile(NewArtifact resolution);
  
  interface OpenProgram {
    Simple_AST getAst();
    Program close(Artifact artifact);
  }

  interface Bundler {
    NewArtifact newArtifact();
    Uni<BundleBuilder> build(List<OpenProgram> openProgram);
  }
  
  interface NewArtifact {
    NewArtifact id(String id);
    NewArtifact name(String name);
    NewArtifact ast(Simple_AST ast);
    NewArtifact requireDependnecy(Dependency_AST ast);
    NewArtifact requireDependnecy(Dependency_AST ast, Validator validator);
    void build();
  }
  
  interface Validator {
    ValidatorResult validate(Optional<Simple_AST> dependency);
  }
  
  @Value.Immutable
  interface ValidatorResult {
    List<ModelError> getMessages();
    ProgramStatus getProgramStatus();
  }
  
  
  @Value.Immutable
  interface Artifact {
    String getArtifactId(); // who am I
    String getArtifactName(); // who am I
    
    Model.BodyType getArtifactType();
    
    List<Dependency_AST> getChildDeps(); // who do I depend on aka children
    List<Dependency_AST> getParentDeps(); // who depends on me
    
    List<ModelError> getErrors();
    List<ProgramAssociation> getAssociations(); 
    ProgramStatus getProgramStatus();
  }
}
