package io.digiexpress.eveli.app.assets.demo_object;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.assets.property_object.spi.BaseJsonPropertyObjectDescriptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DemoLocationPropertyObjectDescriptor extends BaseJsonPropertyObjectDescriptor<DemoLocationPropertyObject> {

  
  public DemoLocationPropertyObjectDescriptor(ObjectMapper mapper) {
    super(mapper);
  }
  
  @Override
  public String getPropertyObjectType() {
    return "demo-location";
  }

  @Override
  protected Class<DemoLocationPropertyObject> getObjectClass() {
    return DemoLocationPropertyObject.class;
  }

}
