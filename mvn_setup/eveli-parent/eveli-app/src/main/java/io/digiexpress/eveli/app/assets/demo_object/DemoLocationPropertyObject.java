package io.digiexpress.eveli.app.assets.demo_object;

import io.digiexpress.eveli.client.assets.property_object.api.BasePropertyObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DemoLocationPropertyObject extends BasePropertyObject {

  private String street;
  private String city;
  private String postalCode;
  private String country;
  private Integer capacity;
}
