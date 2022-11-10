package io.digiexpress.client.spi;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.form.Form;
import io.digiexpress.client.api.ImmutableServiceConfigDocument;
import io.digiexpress.client.api.ImmutableServiceDefinitionDocument;
import io.digiexpress.client.api.ImmutableServiceReleaseDocument;
import io.digiexpress.client.api.ImmutableServiceRevisionDocument;
import io.digiexpress.client.api.ServiceDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceRevisionDocument;
import io.digiexpress.client.api.ServiceMapper;
import io.digiexpress.client.api.ServiceStore.StoreEntity;
import io.digiexpress.client.spi.support.JsonMappingException;
import io.resys.hdes.client.api.ast.AstTag;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.spi.beans.SitesBean;
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
  public ServiceRevisionDocument toRev(StoreEntity entity) {
    try {
      return ImmutableServiceRevisionDocument.builder()
          .from(om.readValue(entity.getBody(), ServiceRevisionDocument.class))
          .id(entity.getId())
          .version(entity.getVersion())
          .build();
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity.getBody(), e);
    }
  }

  @Override
  public ServiceReleaseDocument toService(StoreEntity entity) {
    try {
      return ImmutableServiceReleaseDocument.builder()
          .from(om.readValue(entity.getBody(), ServiceReleaseDocument.class))
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
    } else if(entity instanceof ServiceDefinitionDocument) {
      return toDefBody((ServiceDefinitionDocument) entity);
    } else if(entity instanceof ServiceRevisionDocument) {
      return toRevisionBody((ServiceRevisionDocument) entity);
    } else if(entity instanceof ServiceReleaseDocument) {
      return toServiceBody((ServiceReleaseDocument) entity);
    }
    throw new JsonMappingException("Unknown document: " + entity); 
  }
  protected String toServiceBody(ServiceReleaseDocument entity) {
    try {
      return om.writeValueAsString(ImmutableServiceReleaseDocument.builder().from(entity).id(null).version(null).build());
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }
  protected String toRevisionBody(ServiceRevisionDocument entity) {
    try {
      return om.writeValueAsString(ImmutableServiceRevisionDocument.builder().from(entity).id(null).version(null).build());
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
  
  protected String toDefBody(ServiceDefinitionDocument entity) {
    try {
      return om.writeValueAsString(ImmutableServiceDefinitionDocument.builder().from(entity).id(null).version(null).build());
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }

  @Override
  public ServiceDefinitionDocument toDef(StoreEntity entity) {
    try {
      return ImmutableServiceDefinitionDocument.builder()
          .from(om.readValue(entity.getBody(), ServiceDefinitionDocument.class))
          .id(entity.getId())
          .version(entity.getVersion())
          .build();
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity.getBody(), e);
    }
  }

  @Override
  public String toReleaseBody(AstTag entity) {
    try {
      return om.writeValueAsString(entity);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }

  @Override
  public String toReleaseBody(Form entity) {
    try {
      return om.writeValueAsString(entity);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }

  @Override
  public String toReleaseBody(Sites entity) {
    try {
      return om.writeValueAsString(entity);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }

  @Override
  public String toReleaseBody(ServiceDefinitionDocument entity) {
    try {
      return om.writeValueAsString(entity);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }

  @Override
  public AstTag toHdes(String body) {
    try {
      return om.readValue(body, AstTag.class);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + body, e);
    }
  }

  @Override
  public Sites toStencil(String body) {
    try {
      return om.readValue(body, SitesBean.class);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + body, e);
    }
  }

  @Override
  public Form toDialob(String body) {
    try {
      return om.readValue(body, Form.class);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + body, e);
    }
  }

  @Override
  public ServiceDefinitionDocument toService(String body) {
    try {
      return om.readValue(body, ServiceDefinitionDocument.class);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + body, e);
    }
  }
}
