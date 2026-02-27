package io.resys.limaone.spi.compiler;

import java.util.List;
import java.util.Optional;

import io.resys.limaone.model.Model;
import io.resys.limaone.program.Compiler.Bundle;
import io.resys.limaone.program.Program;
import io.resys.limaone.program.Program.ProgramAssociation;
import io.resys.limaone.program.Program.ProgramMessage;
import io.resys.limaone.program.Program.ProgramStatus;
import io.smallrye.mutiny.Uni;

public interface CompilableUnit {
  OpenProgram compile(NewArtifact resolution);
  
  interface OpenProgram {
    String getId();
    Program close(Artifact artifact);
  }


  interface BundleBuilder {
    NewArtifact newArtifact();
    Uni<Bundle> build(List<OpenProgram> openProgram);
  }
  
  interface NewArtifact {
    NewArtifact id(String id);
    NewArtifact name(String name);
    NewArtifact type(Model.BodyType bodyType);
    RequireDependency requireDependnecy();
    void build();
  }
  
  interface RequireDependency {
    RequireDependency id(String id); 
    RequireDependency bodyType(Model.BodyType bodyType); 
    RequireDependency validator(Validator validator);
    void build();
  }
  
  
  interface Validator {
    ValidatorResult validate(Optional<Artifact> dependency);
  }
  
  interface ValidatorResult {
    List<ProgramMessage> getMessages();
    ProgramStatus getProgramStatus();
  }
  
  
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
    Validator getValidator();
  }
}
