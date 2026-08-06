package io.digiexpress.eveli.app.assets.demo_object;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.assets.property_object.spi.BaseJsonPropertyObjectDescriptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DemoLocationPropertyObjectDescriptor extends BaseJsonPropertyObjectDescriptor<DemoLocationPropertyObject> {

  
  private static final String OBJECT_TYPE = "demo-location";

  public DemoLocationPropertyObjectDescriptor(ObjectMapper mapper) {
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
  protected Class<DemoLocationPropertyObject> getObjectClass() {
    return DemoLocationPropertyObject.class;
  }


}
