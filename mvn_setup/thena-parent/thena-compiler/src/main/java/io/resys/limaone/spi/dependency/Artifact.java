package io.resys.limaone.spi.dependency;

import java.util.List;

import io.resys.limaone.model.Model;
import io.resys.limaone.program.Program.ProgramAssociation;
import io.resys.limaone.program.Program.ProgramMessage;
import io.resys.limaone.program.Program.ProgramStatus;

public interface Artifact {
  
  String getArtifactId(); // who am I
  String getArtifactName(); // who am I
  
  Model.BodyType getArtifactType();
  
  List<Dependency> getChildDeps(); // who do I depend on aka children
  List<Dependency> getParentDeps(); // who depends on me
  
  List<ProgramMessage> getErrors();
  List<ProgramAssociation> getAssociations(); 
  ProgramStatus getProgramStatus();
  
  
  
}
