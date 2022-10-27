package io.digiexpress.client.spi.store;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.client.api.ImmutableStoreEntity;
import io.digiexpress.client.api.ServiceStore.StoreEntity;
import io.resys.thena.docdb.api.models.Objects.Blob;



public class BlobDeserializer implements ServiceStoreConfig.Deserializer {

  private ObjectMapper objectMapper;
  
  public BlobDeserializer(ObjectMapper objectMapper) {
    super();
    this.objectMapper = objectMapper;
  }


  @Override
  public StoreEntity fromString(Blob value) {
    try {
      final ImmutableStoreEntity src = objectMapper.readValue(value.getValue(), ImmutableStoreEntity.class);
      return src;
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage() + System.lineSeparator() + value, e);
    }
  }
}
