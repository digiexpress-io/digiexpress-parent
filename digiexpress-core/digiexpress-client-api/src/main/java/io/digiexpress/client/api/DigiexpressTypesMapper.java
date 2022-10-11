package io.digiexpress.client.api;

import java.io.InputStream;

public interface DigiexpressTypesMapper {  
  String toString(InputStream entity);
  String toJson(Object anyObject);  
}
