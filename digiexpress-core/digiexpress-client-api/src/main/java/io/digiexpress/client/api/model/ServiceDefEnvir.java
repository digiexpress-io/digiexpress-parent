package io.digiexpress.client.api.model;

import java.util.List;
import java.util.Optional;

import io.dialob.client.api.DialobDocument.FormReleaseDocument;
import io.resys.hdes.client.api.ast.AstTag;
import io.thestencil.client.api.StencilClient;

public interface ServiceDefEnvir {
  List<ServiceDef> findAll();
  ServiceDef get(String id);
  ServiceDef get(String name, String version);
  Optional<ServiceDef> findFirst();
  
  
  
  interface ServiceDefEnvirBuilder {
    ServiceDefEnvirBuilder from(ServiceDefEnvir envir);
    EnvirCommandFormatBuilder addCommand();
    ServiceDefEnvir build();
  }
  interface EnvirCommandFormatBuilder {
    EnvirCommandFormatBuilder id(String externalId);
    EnvirCommandFormatBuilder cachless();
    
    EnvirCommandFormatBuilder formRev(FormReleaseDocument release);
    EnvirCommandFormatBuilder hdes(AstTag release);
    EnvirCommandFormatBuilder stencil(StencilClient.Release release);

    ServiceDefEnvirBuilder build();
  }
}
