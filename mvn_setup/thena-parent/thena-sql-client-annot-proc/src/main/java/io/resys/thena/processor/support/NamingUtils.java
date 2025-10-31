package io.resys.thena.processor.support;

/*-
 * #%L
 * thena-sql-client-annot-proc
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
