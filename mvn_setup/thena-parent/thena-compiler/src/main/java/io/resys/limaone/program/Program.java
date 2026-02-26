package io.resys.limaone.program;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.resys.limaone.model.Model;
import io.resys.limaone.model.Parameter;
import jakarta.annotation.Nullable;

public interface Program extends Serializable {
  
  String getId();
  Model.BodyType getType();
  ProgramStatus getStatus();
  
  List<ProgramMessage> getWarnings();
  List<ProgramMessage> getErrors();
  List<Parameter> getHeaders();
  List<ProgramAssociation> getAssociations();
  
  interface ProgramResult extends Serializable {}
  
  interface ProgramLog extends Serializable {}
  
  interface ProgramInput {
    
  }
  
  @Value.Immutable
  interface ProgramAssociation {
    Optional<String> getId();
    String getRef();
    Model.BodyType  getRefType();
    ProgramStatus getRefStatus();
    Boolean getOwner();
  }
  
  @Value.Immutable
  interface ProgramMessage {
    String getId();
    String getMsg();
    @JsonIgnore
    @Nullable Integer getRow();
    @Nullable Integer getColumn();
    @Nullable Exception getException();
  }
  enum ProgramStatus { 
    UP, 
    AST_ERROR, 
    PROGRAM_ERROR, 
    DEPENDENCY_ERROR }

}
