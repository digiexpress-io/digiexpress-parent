package io.digiexpress.client.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.digiexpress.client.api.ServiceDocument.DocumentType;
import io.digiexpress.client.api.ServiceException.ProgramNotFoundException;

public interface ServiceEnvir {
  Map<String, Program> getValues();

  ArticleWrapper getArticle() throws ProgramNotFoundException;
  ProcessWrapper getProcess(String id) throws ProgramNotFoundException;
  ProcessWrapper getProcess(String id, String revision) throws ProgramNotFoundException;

  
  interface Program extends Serializable {
    String getSource();
    DocumentType getSourceType();
    ProgramStatus getStatus();
    List<ProgramMessage> getErrors();
  }
  
  
  @Value.Immutable
  interface ArticleWrapper extends Program {
    
  }

  @Value.Immutable
  interface ProcessWrapper extends Program {
    
  }
  
  
  @Value.Immutable
  interface ProgramMessage {
    String getId();
    @Nullable 
    String getMsg();
    
    @JsonIgnore @Nullable
    Exception getException();
  }
  
  enum ProgramStatus { CREATED, COMPILING, UP, ERROR, }
  

}