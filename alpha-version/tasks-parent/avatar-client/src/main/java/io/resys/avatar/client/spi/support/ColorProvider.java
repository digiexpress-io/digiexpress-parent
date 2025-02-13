package io.resys.avatar.client.spi.support;

import java.util.List;

import com.google.common.collect.ImmutableMap;

public class ColorProvider {
  private final List<String> reserved;
  private final int TOTAL_COLORS = 500;
  
  //SEED colors
  private final static ImmutableMap<String, String> PALETTE = ImmutableMap.<String, String>builder()
      .put("red: bittersweet", "#FF595E")
      .put("green: emerald", "#26C485")
      .put("yellow: sunglow", "#FFCA3A")
      .put("blue: steelblue", "#1982C4")
      .put("violet: ultraviolet", "#6A4C93")
      .build();
  

  public ColorProvider(List<String> reserved) {
    super();
    this.reserved = reserved;
  }
  
  public String getNextColor() {
    int R = (int) (Math.random( )*256);
    int G = (int)(Math.random( )*256);
    int B= (int)(Math.random( )*256);

    return "rgb(" + R + ", " + G + "," + B + ")";
  }

  public static ColorProvider getInstance(List<String> reserved) {
    return new ColorProvider(reserved);
  }
  
}
