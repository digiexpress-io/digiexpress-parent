package io.digiexpress.eveli.client.assets.property_object.api;

import java.util.List;
import java.util.Optional;

public interface PropertyObjectDescriptorFactory {
  Optional<PropertyObjectDescriptor<? extends BasePropertyObject>> getDescriptor(String propertyObjectType);
  PropertyObjectDescriptor<? extends BasePropertyObject> getDefaultDescriptor();
  List<String> getRegisteredPropertyObjectTypes();
}
