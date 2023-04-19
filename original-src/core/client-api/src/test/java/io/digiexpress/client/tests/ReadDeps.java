package io.digiexpress.client.tests;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class ReadDeps {


  @Test
  public void toLib() {
    final var src = getDeps()
        .replaceAll("\r\n", "\n")
        .split("\n");
    final var result = Arrays.asList(src).stream()
        .map(r -> r.replaceAll("\\[INFO\\]", "").replaceAll("\"", "").replaceAll(",", "").trim())
        .filter(r -> !r.isBlank())
        .sorted()
        .distinct()
        .toList();

    System.out.println("DEPS VERSIONS");
    for(final var r : result) {
      final var segments = r.split(":");
      System.out.println("\"" + 
          segments[0] + ":" + segments[1] + ":" + segments[3]
          + "\",");
    }
    
    System.out.println("DEPS");    
    for(final var r : result) {
      final var segments = r.replaceAll("\\.", "_").replaceAll("-", "_").split(":");
      System.out.println("\"@maven//:" + 
          segments[0] + "_" + segments[1]
          + "\",");
    }
  }
  
  
  

  private String getDeps() {
    try {
      final var type = ReadDeps.class;
      return new String(type.getClassLoader().getResourceAsStream("deps.txt").readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
