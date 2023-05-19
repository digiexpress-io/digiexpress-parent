package io.digiexpress.client.api;

import org.immutables.value.Value;

import io.dialob.api.form.Form;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ClientEntity.ServiceRelease;
import io.resys.hdes.client.api.ast.AstTag;
import io.thestencil.client.api.MigrationBuilder.Sites;


public interface Archiver {
  Compressed compress(AstTag hdes);  
  Compressed compress(Sites stencil);  
  Compressed compress(Form form);
  Compressed compress(ServiceDefinition def);
  Compressed compress(ServiceRelease def);
  
  AstTag decompressionHdes(String body);
  Sites decompressionStencil(String body);
  Form decompressionDialob(String body);
  
  ServiceDefinition decompressionService(String body);
  ServiceRelease decompressionRelease(String body);
  
  @Value.Immutable
  interface Compressed {
    String getValue();
    String getHash();
  }
}
