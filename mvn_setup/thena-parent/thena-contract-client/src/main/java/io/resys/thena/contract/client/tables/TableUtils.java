package io.resys.thena.contract.client.tables;

import java.time.Duration;
import java.util.UUID;
import io.vertx.pgclient.data.Interval;

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
  
  /**
   * Converts a Duration to PostgreSQL Interval for database storage.
   * 
   * @param duration the Duration to convert
   * @return Interval object
   * @throws IllegalArgumentException if the duration is null
   */
  public static Interval toInterval(Duration duration) {
    if (duration == null) {
      throw new IllegalArgumentException("Duration cannot be null");
    }
    
    long totalSeconds = duration.getSeconds();
    int nanos = duration.getNano();
    
    // Handle negative durations
    boolean isNegative = totalSeconds < 0 || (totalSeconds == 0 && nanos < 0);
    if (isNegative) {
      totalSeconds = Math.abs(totalSeconds);
      nanos = Math.abs(nanos);
    }
    
    int days = (int) (totalSeconds / 86400);
    totalSeconds %= 86400;
    
    int hours = (int) (totalSeconds / 3600);
    totalSeconds %= 3600;
    
    int minutes = (int) (totalSeconds / 60);
    int seconds = (int) (totalSeconds % 60);
    
    int microseconds = nanos / 1000;
    
    Interval interval = new io.vertx.pgclient.data.Interval()
        .days(days)
        .hours(hours)
        .minutes(minutes)
        .seconds(seconds)
        .microseconds(microseconds);

    
    return interval;
  }
  
  /**
   * Safely converts a Duration to PostgreSQL Interval, returning null for null input.
   * 
   * @param duration the Duration to convert (can be null)
   * @return Interval object or null if input is null
   * @throws IllegalArgumentException if the duration is not null but cannot be converted
   */
  public static Interval toIntervalSafe(Duration duration) {
    return duration == null ? null : toInterval(duration);
  }
}
