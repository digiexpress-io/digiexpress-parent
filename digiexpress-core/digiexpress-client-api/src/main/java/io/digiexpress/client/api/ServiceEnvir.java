package io.digiexpress.client.api;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.dialob.api.form.Form;
import io.digiexpress.client.api.ProcessState.ServiceRef;
import io.digiexpress.client.api.ProcessState.ServiceRel;
import io.digiexpress.client.api.ServiceDocument.ConfigType;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.resys.hdes.client.api.ast.AstTag;
import io.thestencil.client.api.MigrationBuilder.Sites;

public interface ServiceEnvir {
  Map<String, ServiceEnvirValue> getValues();

  interface ProgramSource extends Serializable {
    String getHash();     // hash of the uncompressed body
    String getBody();     // compressed source code -> stencil/hdes/dialob/service
    ConfigType getType(); // body content type
    
    Sites toStencil(CompressionMapper mapper);
    Form toForm(CompressionMapper mapper);
    AstTag toHdes(CompressionMapper mapper);
    ServiceDefinitionDocument toService(CompressionMapper mapper);
  }

  interface Program<T> extends Serializable {
    String getId();
    ProgramSource getSource();
    T getBody();
  }
  
  
  interface ServiceEnvirValue extends Serializable {
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