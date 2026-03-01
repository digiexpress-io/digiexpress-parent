package io.resys.limaone.program;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.limaone.model.Model;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;

public interface Program extends Serializable {
  
  String getId();
  String getName();
  Model.BodyType getType();
  ProgramStatus getStatus();

  List<Parameter> getHeaders();
  List<ModelError> getErrors();
  List<ProgramAssociation> getAssociations();
  
  interface ProgramResult extends Serializable {}
  
  interface ProgramLog extends Serializable {}
  
  @Value.Immutable
  interface ProgramAssociation {
    Optional<String> getId();
    String getRef();
    Model.BodyType  getRefType();
    ProgramStatus getRefStatus();
    Boolean getOwner();
  }

  enum ProgramStatus { UP, ERROR }

}
