package io.digiexpress.client.spi;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.client.api.ImmutableProcessDocument;
import io.digiexpress.client.api.ImmutableProcessRevisionDocument;
import io.digiexpress.client.api.ImmutableServiceConfigDocument;
import io.digiexpress.client.api.ServiceDocument;
import io.digiexpress.client.api.ServiceDocument.ProcessDocument;
import io.digiexpress.client.api.ServiceDocument.ProcessRevisionDocument;
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
  public String toBody(ServiceDocument entity) {
    if(entity instanceof ServiceConfigDocument) {
      return toConfigBody((ServiceConfigDocument) entity);
    } else if(entity instanceof ProcessDocument) {
      return toProcessBody((ProcessDocument) entity);
    } else if(entity instanceof ProcessRevisionDocument) {
      return toRevisionBody((ProcessRevisionDocument) entity);
    }
    throw new JsonMappingException("Unknown document: " + entity); 
  }

  protected String toRevisionBody(ProcessRevisionDocument entity) {
    try {
      return om.writeValueAsString(ImmutableProcessRevisionDocument.builder().from(entity).id(null).version(null).build());
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }
  
  protected String toConfigBody(ServiceConfigDocument entity) {
    try {
      return om.writeValueAsString(ImmutableServiceConfigDocument.builder().from(entity).id(null).version(null).build());
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }
  
  protected String toProcessBody(ProcessDocument entity) {
    try {
      return om.writeValueAsString(ImmutableProcessDocument.builder().from(entity).id(null).version(null).build());
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }
}
