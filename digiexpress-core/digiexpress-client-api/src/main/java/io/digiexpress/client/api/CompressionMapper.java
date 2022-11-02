package io.digiexpress.client.api;

import org.immutables.value.Value;

import io.dialob.api.form.Form;
import io.resys.hdes.client.api.ast.AstTag;
import io.thestencil.client.api.MigrationBuilder.Sites;



public interface CompressionMapper {
  Compressed compress(AstTag hdes);  
  Compressed compress(Sites stencil);  
  Compressed compress(Form form);
  
  @Value.Immutable
  interface Compressed {
    String getValue();
    String getHash();
  }
}
