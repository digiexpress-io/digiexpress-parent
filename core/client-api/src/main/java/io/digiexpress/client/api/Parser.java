package io.digiexpress.client.api;

import io.dialob.api.form.Form;
import io.digiexpress.client.api.ClientEntity.Project;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ClientEntity.ServiceRelease;
import io.digiexpress.client.api.ClientStore.StoreEntity;
import io.resys.hdes.client.api.ast.AstTag;
import io.thestencil.client.api.MigrationBuilder.Sites;

public interface Parser {
  Project toProject(StoreEntity entity);
  ServiceDefinition toDefinition(StoreEntity entity);
  ServiceDefinition toDefinition(String body);
  ServiceRelease toRelease(StoreEntity entity);
  ServiceRelease toRelease(String body);
  String toStore(ClientEntity entity);
  
  AstTag toHdes(String body);
  Sites toStencil(String body);
  Form toDialob(String body);

  String toRelease(ServiceRelease release);
  String toRelease(ServiceDefinition service);
  String toRelease(AstTag hdes);
  String toRelease(Form form);
  String toRelease(Sites sites);

}
