package io.digiexpress.client.spi.support;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.digiexpress.client.api.Client.ClientException;
import io.digiexpress.client.api.ClientEntity;



public class ConstraintException extends RuntimeException implements ClientException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  private final List<ClientEntity> entity = new ArrayList<>();
  
  public ConstraintException(ClientEntity entity, String msg) {
    super(msg(Arrays.asList(entity), msg));
    this.entity.add(entity);
  }
  
  public List<ClientEntity> getEntity() {
    return entity;
  }
  
  private static String msg(List<ClientEntity> entity, String msg) {
    StringBuilder messages = new StringBuilder()
      .append(System.lineSeparator())
      .append("  - ").append(msg);
    return new StringBuilder("Can't save entity: ")
        .append(entity.get(0).getType())
        .append(", because of: ").append(messages)
        .toString();
  }
}
