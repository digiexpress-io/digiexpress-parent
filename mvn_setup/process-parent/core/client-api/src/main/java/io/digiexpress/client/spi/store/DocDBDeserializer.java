package io.digiexpress.client.spi.store;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.client.api.ClientStore.StoreEntity;
import io.digiexpress.client.api.ImmutableStoreEntity;
import io.resys.thena.docdb.api.models.ThenaObject.Blob;

public class DocDBDeserializer implements DocDBConfig.DocDBDeserializer {
  private ObjectMapper objectMapper;
  
  public DocDBDeserializer(ObjectMapper objectMapper) {
    super();
    this.objectMapper = objectMapper;
  }
  @Override
  public StoreEntity fromString(Blob value) {
    try {
      final ImmutableStoreEntity src = objectMapper.readValue(value.getValue().toString(), ImmutableStoreEntity.class);
      return src;
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage() + System.lineSeparator() + value, e);
    }
  }
}
