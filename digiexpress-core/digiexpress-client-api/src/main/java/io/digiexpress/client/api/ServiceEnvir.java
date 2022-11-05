package io.digiexpress.client.api;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.digiexpress.client.api.ProcessState.ServiceRef;
import io.digiexpress.client.api.ProcessState.ServiceRel;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;

public interface ServiceEnvir {
  Map<String, ServiceWrapper> getValues();

  interface ServiceWrapper extends Serializable {
    String getId();
    ServiceRef getRefId();
    ServiceRel getRelId();
    
    ServiceDefinitionDocument getDef();
    ServiceReleaseDocument getRel();
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
  enum ProgramType { PROCESS, HDES, DIALOB, STENCIL }

}