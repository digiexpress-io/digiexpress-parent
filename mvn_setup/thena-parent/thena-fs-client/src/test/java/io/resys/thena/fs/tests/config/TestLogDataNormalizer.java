package io.resys.thena.fs.tests.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestLogDataNormalizer {

  private final Map<String, String> COMMIT_MAP = new HashMap<>();
  private final AtomicInteger commitCounter = new AtomicInteger(1);

  private static final Pattern COMMIT_PATTERN = Pattern.compile("\\b[0-9a-f]{8}\\b");
  private static final Pattern DATETIME_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} UTC");
  private static final Pattern DATE_PATTERN = Pattern.compile("Date: \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} UTC");

  public String normalizeLogData(String logData) {
    String result = logData;

    // Replace commits
    Matcher commitMatcher = COMMIT_PATTERN.matcher(result);
    StringBuffer sb = new StringBuffer();
    while (commitMatcher.find()) {
      commitMatcher.appendReplacement(sb, getStaticCommit(commitMatcher.group()));
    }
    commitMatcher.appendTail(sb);
    result = sb.toString();

    // Replace dates and times
    result = DATETIME_PATTERN.matcher(result).replaceAll("2024-01-01 12:00:00 UTC");
    result = DATE_PATTERN.matcher(result).replaceAll("Date: 2024-01-01 12:00:00 UTC");

    return result;
  }

  private String getStaticCommit(String original) {
    return COMMIT_MAP.computeIfAbsent(original,
      k -> String.format("commit%02d", commitCounter.getAndIncrement()));
  }
}