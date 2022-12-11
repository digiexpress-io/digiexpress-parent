package io.digiexpress.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.dialob.program.DialobProgram;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceDocument.ConfigType;
import io.digiexpress.client.api.ServiceDocument.RefIdValue;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;

public interface ServiceEnvir {
  Map<String, ServiceProgramSource> getSources(); //id to source
  ServiceProgram getByHash(String hash);
  ServiceProgram getById(String objectId);
  ServiceProgram getByRefId(RefIdValue ref);
  ServiceProgramDialob getForm(String objectId);
  ServiceProgramHdes getHdes(LocalDateTime targetDate);
  ServiceProgramStencil getStecil(LocalDateTime targetDate);
  ServiceProgramDef getDef(LocalDateTime targetDate);
  ServiceProgramRel getRel(LocalDateTime targetDate);
  
  
  interface ServiceProgramSource extends Serializable {
    String getId();       // unique id of resource
    String getHash();     // hash of the uncompressed body
    String getBody();     // compressed source code -> stencil/hdes/dialob/service
    ConfigType getType(); // body content type
  }
  
  interface ServiceProgram extends Serializable {
    String getId();
    ServiceProgramStatus getStatus();
    List<ProgramMessage> getErrors();
    ServiceProgramSource getSource();
  }
  
  interface ServiceProgramHdes extends ServiceProgram {
    String getFlowName(String flowId, ServiceClientConfig config);
    io.resys.hdes.client.api.ast.AstTag getDelegate(ServiceClientConfig config);
    Optional<io.resys.hdes.client.api.programs.ProgramEnvir> getCompiled(ServiceClientConfig config);
  }
  interface ServiceProgramDialob extends ServiceProgram {
    io.dialob.api.form.Form getDelegate(ServiceClientConfig config);
    Optional<DialobProgram> getCompiled(ServiceClientConfig config);
  }
  interface ServiceProgramStencil extends ServiceProgram {
    io.thestencil.client.api.MigrationBuilder.Sites getDelegate(ServiceClientConfig config);
  }
  
  interface ServiceProgramRel extends ServiceProgram {
    ServiceReleaseDocument getDelegate(ServiceClientConfig config);
  }
  
  interface ServiceProgramDef extends ServiceProgram {
    ServiceDefinitionDocument getDelegate(ServiceClientConfig config);
  }
  
  
  @Value.Immutable
  interface ProgramMessage {
    String getId();
    @Nullable 
    String getMsg();
    
    @JsonIgnore @Nullable
    Exception getException();
  }
  
  enum ServiceProgramStatus { CREATED, PARSED, UP, ERROR, }

}