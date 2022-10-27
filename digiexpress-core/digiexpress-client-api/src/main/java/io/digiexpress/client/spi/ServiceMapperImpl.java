package io.digiexpress.client.spi;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.client.api.ImmutableServiceConfigDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceMapper;
import io.digiexpress.client.api.ServiceStore.StoreEntity;
import io.digiexpress.client.spi.support.JsonMappingException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServiceMapperImpl implements ServiceMapper {
  private final ObjectMapper om;

  @Override
  public ServiceConfigDocument toConfig(StoreEntity entity) {
    try {
      return ImmutableServiceConfigDocument.builder()
          .from(om.readValue(entity.getBody(), ServiceConfigDocument.class))
          .id(entity.getId())
          .version(entity.getVersion())
          .build();
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity.getBody(), e);
    }
  }

  @Override
  public String toBody(ServiceConfigDocument entity) {
    try {
      return om.writeValueAsString(ImmutableServiceConfigDocument.builder().from(entity).id(null).version(null).build());
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }
}
