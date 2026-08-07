package io.digiexpress.eveli.client.assets.property_object.spi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.digiexpress.eveli.client.assets.property_object.api.BasePropertyObject;
import io.digiexpress.eveli.client.assets.property_object.api.PropertyObjectDescriptor;
import io.digiexpress.eveli.client.assets.property_object.api.PropertyObjectDescriptorFactory;

public class PropertyObjectDescriptorFactoryImpl implements PropertyObjectDescriptorFactory {

  private Map<String, PropertyObjectDescriptor<? extends BasePropertyObject>> descriptors = new HashMap<>();
  
  public PropertyObjectDescriptorFactoryImpl(PropertyObjectDescriptor<? extends BasePropertyObject> defaultDesc, List<PropertyObjectDescriptor<? extends BasePropertyObject>> propDescriptors) {
    for (var desc : propDescriptors) {
      descriptors.put(desc.getPropertyObjectType(), desc);
    }
  }
  
  @Override
  public Optional<PropertyObjectDescriptor<? extends BasePropertyObject>> getDescriptor(String propertyObjectType) {
    return Optional.ofNullable(descriptors.get(propertyObjectType));
  }

  @Override
  public List<String> getRegisteredPropertyObjectTypes() {
    return descriptors.keySet().stream().toList();
  }


}
