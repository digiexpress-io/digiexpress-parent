package io.resys.thena.processor.support;

public class NamingUtils {
  public static String toCamelCaseCapitalized(String snakeCase) {
    final var parts = snakeCase.split("_");
    final var result = new StringBuilder();
    for (final var part : parts) {
      if (!part.isEmpty()) {
        result.append(Character.toUpperCase(part.charAt(0)))
              .append(part.substring(1));
      }
    }
    return result.toString();
  }
  public static String toCamelCase(String snakeCase) {
    final var parts = snakeCase.split("_");
    final var result = new StringBuilder();
    
    for (int i = 0; i < parts.length; i++) {
      final var part = parts[i];
      if (!part.isEmpty()) {
        if (i == 0) {
          result.append(part);
        } else {
          result.append(Character.toUpperCase(part.charAt(0)))
                .append(part.substring(1));
        }
      }
    }
    
    return result.toString();
  }
  
  
  public static String toPascalCase(String snakeCase) {
    final var parts = snakeCase.split("_");
    final var result = new StringBuilder();
    
    for (final var part : parts) {
      if (!part.isEmpty()) {
        result.append(Character.toUpperCase(part.charAt(0)))
              .append(part.substring(1));
      }
    }
    
    return result.toString();
  }
  
  public static String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }
  public static String lowerCamelCase(String pascalCase) {
    if (pascalCase == null || pascalCase.isEmpty()) {
      return pascalCase;
    }
    return Character.toLowerCase(pascalCase.charAt(0)) + pascalCase.substring(1);
  }
  public static String pluralize(String tableName) {
    // Convert snake_case to camelCase and attempt simple pluralization
    // grim_mission -> missions
    // grim_commit -> commits
    
    final var parts = tableName.split("_");
    final var result = new StringBuilder();
    
    for (int i = 0; i < parts.length; i++) {
      final var part = parts[i];
      if (!part.isEmpty()) {
        if (i == 0) {
          result.append(part);
        } else {
          result.append(Character.toUpperCase(part.charAt(0)))
                .append(part.substring(1));
        }
      }
    }
    
    // Simple pluralization: add 's' if doesn't end in 's'
    final var camelCase = result.toString();
    if (!camelCase.endsWith("s")) {
      return camelCase + "s";
    }
    return camelCase;
  }
}
