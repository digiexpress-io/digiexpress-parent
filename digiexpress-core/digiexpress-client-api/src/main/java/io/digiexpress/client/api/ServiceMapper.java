package io.digiexpress.client.api;

import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceStore.StoreEntity;

public interface ServiceMapper {
  ServiceConfigDocument toConfig(StoreEntity entity);
  String toBody(ServiceConfigDocument entity);
}
