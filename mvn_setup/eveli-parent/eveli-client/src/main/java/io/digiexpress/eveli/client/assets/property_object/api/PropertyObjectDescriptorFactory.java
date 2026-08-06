package io.digiexpress.eveli.client.assets.property_object.api;

import java.util.List;

public interface PropertyObjectDescriptorFactory {
  PropertyObjectDescriptor getDescriptor(String propertyObjectType);
  List<String> getRegisteredPropertyObjectTypes();
}
