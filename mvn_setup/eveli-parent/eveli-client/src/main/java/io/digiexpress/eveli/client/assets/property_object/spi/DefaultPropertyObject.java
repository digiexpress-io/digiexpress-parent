package io.digiexpress.eveli.client.assets.property_object.spi;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefaultPropertyObject {

  private Map<String, Object> values;
}
