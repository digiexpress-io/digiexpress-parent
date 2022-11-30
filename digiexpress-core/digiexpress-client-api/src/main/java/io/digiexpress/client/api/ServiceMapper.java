package io.digiexpress.client.api;

import io.dialob.api.form.Form;
import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceRevisionDocument;
import io.digiexpress.client.api.ServiceStore.StoreEntity;
import io.resys.hdes.client.api.ast.AstTag;
import io.thestencil.client.api.MigrationBuilder.Sites;

public interface ServiceMapper {
  ServiceConfigDocument toConfig(StoreEntity entity);
  ServiceRevisionDocument toRev(StoreEntity entity);
  ServiceDefinitionDocument toDef(StoreEntity entity);
  ServiceReleaseDocument toRel(StoreEntity entity);
  String toBody(ServiceDocument entity);
  
  AstTag toHdes(String body);
  Sites toStencil(String body);
  Form toDialob(String body);
  ServiceDefinitionDocument toService(String body);
  ServiceReleaseDocument toRelease(String body);

  String toReleaseBody(ServiceReleaseDocument release);
  String toReleaseBody(ServiceDefinitionDocument service);
  String toReleaseBody(AstTag hdes);
  String toReleaseBody(Form form);
  String toReleaseBody(Sites sites);

}
