package io.digiexpress.eveli.client.assets.property_object.spi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.digiexpress.eveli.client.assets.property_object.api.PropertyObjectDescriptor;
import io.digiexpress.eveli.client.assets.property_object.api.PropertyObjectDescriptorFactory;

public class PropertyObjectDescriptorFactoryImpl implements PropertyObjectDescriptorFactory {

  private Map<String, PropertyObjectDescriptor> descriptors = new HashMap<>();
  private final PropertyObjectDescriptor defaultDescriptor;
  
  public PropertyObjectDescriptorFactoryImpl(PropertyObjectDescriptor defaultDesc, PropertyObjectDescriptor ...propDescriptors) {
    this.defaultDescriptor = defaultDesc;
    for (var desc : propDescriptors) {
      descriptors.put(desc.getPropertyObjectType(), desc);
    }
  }
  
  @Override
  public Optional<PropertyObjectDescriptor> getDescriptor(String propertyObjectType) {
    return Optional.ofNullable(descriptors.get(propertyObjectType));
  }

  @Override
  public List<String> getRegisteredPropertyObjectTypes() {
    return descriptors.keySet().stream().toList();
  }

  public PropertyObjectDescriptor getDefaultDescriptor() {
    return defaultDescriptor;
  }

}
