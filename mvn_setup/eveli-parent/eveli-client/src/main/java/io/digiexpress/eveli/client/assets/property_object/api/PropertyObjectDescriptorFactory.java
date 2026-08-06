package io.digiexpress.eveli.client.assets.property_object.api;

import java.util.List;
import java.util.Optional;

public interface PropertyObjectDescriptorFactory {
  Optional<PropertyObjectDescriptor> getDescriptor(String propertyObjectType);
  PropertyObjectDescriptor getDefaultDescriptor();
  List<String> getRegisteredPropertyObjectTypes();
}
