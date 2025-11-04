package io.resys.thena.contract.client.tables;

import java.util.UUID;

public class TableUtils {

  /**
   * Converts a UUID string to a UUID object, handling various input formats.
   * 
   * @param uuidString the UUID string to convert
   * @return UUID object
   * @throws IllegalArgumentException if the string cannot be converted to a valid UUID
   */
  public static UUID toUuid(String uuidString) {
    if (uuidString == null) {
      throw new IllegalArgumentException("UUID string cannot be null");
    }
    
    String trimmed = uuidString.trim();
    
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("UUID string cannot be empty");
    }
    
    // Handle standard UUID format (with hyphens)
    if (trimmed.length() == 36 && trimmed.charAt(8) == '-') {
      return UUID.fromString(trimmed);
    }
    
    // Handle compact UUID format (32 hex characters without hyphens)
    if (trimmed.length() == 32 && trimmed.matches("[0-9a-fA-F]{32}")) {
      return UUID.fromString(
        trimmed.substring(0, 8) + "-" +
        trimmed.substring(8, 12) + "-" +
        trimmed.substring(12, 16) + "-" +
        trimmed.substring(16, 20) + "-" +
        trimmed.substring(20, 32)
      );
    }
    
    // Handle uppercase compact format
    if (trimmed.length() == 32 && trimmed.matches("[0-9A-F]{32}")) {
      String lowercase = trimmed.toLowerCase();
      return UUID.fromString(
        lowercase.substring(0, 8) + "-" +
        lowercase.substring(8, 12) + "-" +
        lowercase.substring(12, 16) + "-" +
        lowercase.substring(16, 20) + "-" +
        lowercase.substring(20, 32)
      );
    }
    
    // Handle mixed case and other variations - try direct parsing first
    try {
      return UUID.fromString(trimmed);
    } catch (IllegalArgumentException e) {
      // If direct parsing fails, try to clean and format
      String cleaned = trimmed.replaceAll("[^0-9a-fA-F]", "");
      if (cleaned.length() == 32) {
        cleaned = cleaned.toLowerCase();
        return UUID.fromString(
          cleaned.substring(0, 8) + "-" +
          cleaned.substring(8, 12) + "-" +
          cleaned.substring(12, 16) + "-" +
          cleaned.substring(16, 20) + "-" +
          cleaned.substring(20, 32)
        );
      }
      
      throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
    }
  }
  
  /**
   * Safely converts a UUID string to a UUID object, returning null for null input.
   * 
   * @param uuidString the UUID string to convert (can be null)
   * @return UUID object or null if input is null
   * @throws IllegalArgumentException if the string is not null but cannot be converted to a valid UUID
   */
  public static UUID toUuidSafe(String uuidString) {
    return uuidString == null ? null : toUuid(uuidString);
  }
}
