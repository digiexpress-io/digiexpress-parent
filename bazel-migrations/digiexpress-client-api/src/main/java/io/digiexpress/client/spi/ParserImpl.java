package io.digiexpress.client.spi;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.api.form.Form;
import io.digiexpress.client.api.ClientEntity;
import io.digiexpress.client.api.ClientEntity.Project;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ClientEntity.ServiceRelease;
import io.digiexpress.client.api.ClientStore.StoreEntity;
import io.digiexpress.client.api.ImmutableProject;
import io.digiexpress.client.api.ImmutableServiceDefinition;
import io.digiexpress.client.api.ImmutableServiceRelease;
import io.digiexpress.client.api.Parser;
import io.digiexpress.client.spi.support.JsonMappingException;
import io.resys.hdes.client.api.ast.AstTag;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.spi.beans.SitesBean;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ParserImpl implements Parser {
  private final ObjectMapper om;

  @Override
  public Project toProject(StoreEntity entity) {
    try {
      return ImmutableProject.builder()
          .from(om.readValue(entity.getBody(), Project.class))
          .id(entity.getId())
          .version(entity.getVersion())
          .build();
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity.getBody(), e);
    }
  }

  @Override
  public ServiceRelease toRelease(StoreEntity entity) {
    try {
      return ImmutableServiceRelease.builder()
          .from(om.readValue(entity.getBody(), ServiceRelease.class))
          .id(entity.getId())
          .version(entity.getVersion())
          .build();
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity.getBody(), e);
    }
  }

  @Override
  public String toStore(ClientEntity entity) {
    if(entity instanceof ServiceDefinition) {
      return toDefBody((ServiceDefinition) entity);
    } else if(entity instanceof Project) {
      return toProjectBody((Project) entity);
    } else if(entity instanceof ServiceRelease) {
      return toReleaseBody((ServiceRelease) entity);
    }
    throw new JsonMappingException("Unknown document: " + entity); 
  }
  protected String toReleaseBody(ServiceRelease entity) {
    try {
      return om.writeValueAsString(ImmutableServiceRelease.builder().from(entity).id(null).version(null).build());
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }
  protected String toProjectBody(Project entity) {
    try {
      return om.writeValueAsString(ImmutableProject.builder().from(entity).id(null).version(null).build());
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }
  
  protected String toDefBody(ServiceDefinition entity) {
    try {
      return om.writeValueAsString(ImmutableServiceDefinition.builder().from(entity).id(null).version(null).build());
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }

  @Override
  public ServiceDefinition toDefinition(StoreEntity entity) {
    try {
      return ImmutableServiceDefinition.builder()
          .from(om.readValue(entity.getBody(), ServiceDefinition.class))
          .id(entity.getId())
          .version(entity.getVersion())
          .build();
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity.getBody(), e);
    }
  }

  @Override
  public String toRelease(AstTag entity) {
    try {
      return om.writeValueAsString(entity);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }

  @Override
  public String toRelease(Form entity) {
    try {
      return om.writeValueAsString(entity);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }

  @Override
  public String toRelease(Sites entity) {
    try {
      return om.writeValueAsString(entity);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }

  @Override
  public String toRelease(ServiceDefinition entity) {
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
  public ServiceDefinition toDefinition(String body) {
    try {
      return om.readValue(body, ServiceDefinition.class);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + body, e);
    }
  }

  @Override
  public ServiceRelease toRelease(String body) {
    try {
      return om.readValue(body, ServiceRelease.class);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + body, e);
    }
  }

  @Override
  public String toRelease(ServiceRelease entity) {
    try {
      return om.writeValueAsString(entity);
    } catch (Exception e) {
      throw new JsonMappingException(e.getMessage() + System.lineSeparator() + entity, e);
    }
  }
}
