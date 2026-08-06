package io.digiexpress.eveli.client.assets.property_object.spi;

import java.util.List;
import java.util.Map;

import io.digiexpress.eveli.client.assets.property_object.api.PropertyObjectDescriptor;
import io.digiexpress.eveli.client.assets.property_object.api.PropertyObjectDescriptorFactory;

public class PropertyObjectDescriptorFactoryImpl implements PropertyObjectDescriptorFactory {

  private Map<String, PropertyObjectDescriptor> descriptors;
  
  public PropertyObjectDescriptorFactoryImpl(PropertyObjectDescriptor ...propDescriptors) {
    for (var desc : propDescriptors) {
      descriptors.put(desc.getPropertyObjectType(), desc);
    }
  }
  
  @Override
  public PropertyObjectDescriptor getDescriptor(String propertyObjectType) {
    return descriptors.get(propertyObjectType);
  }

  @Override
  public List<String> getRegisteredPropertyObjectTypes() {
    return descriptors.keySet().stream().toList();
  }

}
