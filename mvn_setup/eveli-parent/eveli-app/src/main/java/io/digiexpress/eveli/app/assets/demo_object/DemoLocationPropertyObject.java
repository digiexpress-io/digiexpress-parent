package io.digiexpress.eveli.app.assets.demo_object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DemoLocationPropertyObject {

  private String name;
  private String street;
  private String city;
  private String postalCode;
  private String country;
  private Integer capacity;
}
