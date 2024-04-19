package io.resys.avatar.client.spi.support;

import java.util.List;

import com.google.common.collect.ImmutableMap;

public class ColorProvider {
  private final List<String> reserved;
  private final int TOTAL_COLORS = 500;
  
  //SEED colors
  private final ImmutableMap<String, String> PALETTE = ImmutableMap.<String, String>builder()
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
  

  private String initColor(int index) {
    final var color = index * (360 / TOTAL_COLORS) % 360;
    return "hsl( " + color + ", 100%, 50% )";
  }
  
  public String getNextColor() {
    // try first polite colors
    final var seedColors = PALETTE.entrySet().asList();
    for(var index = reserved.size(); index < PALETTE.size(); index++) {
      final var newColor = seedColors.get(index).getValue();
      if(!reserved.contains(newColor)) {
        return newColor;
      }
    }

    // fallback
    final var index = reserved.size() + 1;
    final var colorIndex = index * 50;

    final var newColor = initColor(colorIndex);
    if(!reserved.contains(newColor)) {
      return newColor;
    }

    for(var add = 0; add < 1000; add++) {
      final var addColor = initColor(colorIndex+add);
      if(!reserved.contains(addColor)) {
        return addColor;
      }
    }
    return newColor;
  }

  public static ColorProvider getInstance(List<String> reserved) {
    return new ColorProvider(reserved);
  }
  
}
