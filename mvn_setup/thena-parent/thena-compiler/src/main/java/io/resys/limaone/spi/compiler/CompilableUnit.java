package io.resys.limaone.spi.compiler;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.Model;
import io.resys.limaone.program.Compiler.BundleBuilder;
import io.resys.limaone.program.Program;
import io.resys.limaone.program.Program.ProgramAssociation;
import io.resys.limaone.program.Program.ProgramMessage;
import io.resys.limaone.program.Program.ProgramStatus;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface CompilableUnit {
  OpenProgram compile(NewArtifact resolution);
  
  interface OpenProgram {
    String getId();
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
    RequireDependency requireDependnecy();
    void build();
  }
  
  interface RequireDependency {
    RequireDependency id(String id); 
    RequireDependency bodyType(Model.BodyType bodyType); 
    RequireDependency validator(@Nullable Validator validator);
    void build();
  }
  
  
  interface Validator {
    ValidatorResult validate(Optional<Simple_AST> dependency);
  }
  
  @Value.Immutable
  interface ValidatorResult {
    List<ProgramMessage> getMessages();
    ProgramStatus getProgramStatus();
  }
  
  
  @Value.Immutable
  interface Artifact {
    String getArtifactId(); // who am I
    String getArtifactName(); // who am I
    
    Model.BodyType getArtifactType();
    
    List<Dependency> getChildDeps(); // who do I depend on aka children
    List<Dependency> getParentDeps(); // who depends on me
    
    List<ProgramMessage> getErrors();
    List<ProgramAssociation> getAssociations(); 
    ProgramStatus getProgramStatus();
  }

  interface Dependency {
    String getArtifactId();
    String getArtifactName();
    Model.BodyType getArtifactType();
  }
}
