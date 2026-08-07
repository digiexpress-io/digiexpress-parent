package io.digiexpress.eveli.client.assets.property_object.spi;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultPropertyObjectDescriptor extends BaseJsonPropertyObjectDescriptor<DefaultPropertyObject> {

  
  private static final String OBJECT_TYPE = "default";

  public DefaultPropertyObjectDescriptor(ObjectMapper mapper) {
    super(mapper);
  }
  
  @Override
  public String getPropertyObjectType() {
    return OBJECT_TYPE;
  }

  public static String getObjectType() {
    return OBJECT_TYPE;
  }
  
  @Override
  protected Class<DefaultPropertyObject> getObjectClass() {
    return DefaultPropertyObject.class;
  }


}
