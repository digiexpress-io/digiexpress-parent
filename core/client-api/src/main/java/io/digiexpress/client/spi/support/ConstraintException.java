package io.digiexpress.client.spi.support;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.digiexpress.client.api.ServiceClient.ServiceClientException;
import io.digiexpress.client.api.ServiceDocument;



public class ConstraintException extends RuntimeException implements ServiceClientException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  private final List<ServiceDocument> entity = new ArrayList<>();
  
  public ConstraintException(ServiceDocument entity, String msg) {
    super(msg(Arrays.asList(entity), msg));
    this.entity.add(entity);
  }
  
  public List<ServiceDocument> getEntity() {
    return entity;
  }
  
  private static String msg(List<ServiceDocument> entity, String msg) {
    StringBuilder messages = new StringBuilder()
      .append(System.lineSeparator())
      .append("  - ").append(msg);
    return new StringBuilder("Can't save entity: ")
        .append(entity.get(0).getType())
        .append(", because of: ").append(messages)
        .toString();
  }
}
