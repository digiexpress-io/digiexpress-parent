package io.digiexpress.client.api;

import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceRevisionDocument;
import io.digiexpress.client.api.ServiceStore.StoreEntity;

public interface ServiceMapper {
  ServiceConfigDocument toConfig(StoreEntity entity);
  ServiceRevisionDocument toRev(StoreEntity entity);
  ServiceDefinitionDocument toDef(StoreEntity entity);
  String toBody(ServiceDocument entity);
}
