package io.digiexpress.eveli.client.assets.property_object.spi;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefaultPropertyObject {

  @Data
  public static class Prop {
    private String name;
    private Object value;
  }
  private List<Prop> values;
}
