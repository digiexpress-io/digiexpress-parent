package io.digiexpress.eveli.client.assets.property_object.spi;

import java.util.List;

import io.digiexpress.eveli.client.assets.property_object.api.BasePropertyObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefaultPropertyObject extends BasePropertyObject {

  @Data
  public static class Prop {
    private String name;
    private Object value;
  }
  private List<Prop> values;
}
