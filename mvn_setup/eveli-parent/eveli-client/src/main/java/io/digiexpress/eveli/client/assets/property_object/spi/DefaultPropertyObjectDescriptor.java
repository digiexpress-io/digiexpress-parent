package io.digiexpress.eveli.client.assets.property_object.spi;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultPropertyObjectDescriptor extends BaseJsonPropertyObjectDescriptor<DefaultPropertyObject> {

  
  public DefaultPropertyObjectDescriptor(ObjectMapper mapper) {
    super(mapper);
  }
  
  @Override
  public String getPropertyObjectType() {
    return "default";
  }

  @Override
  protected Class<DefaultPropertyObject> getObjectClass() {
    return DefaultPropertyObject.class;
  }


}
