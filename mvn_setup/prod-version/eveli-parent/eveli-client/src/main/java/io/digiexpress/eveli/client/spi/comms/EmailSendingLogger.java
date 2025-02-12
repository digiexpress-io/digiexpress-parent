package io.digiexpress.eveli.client.spi.comms;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmailSendingLogger {
  private List<LogEvent> events;

  private final long start = System.currentTimeMillis();
  
  @Data @Builder
  private static class LogEvent {
    private final Map<String, String> props;
    private final LogEventType type;
  }
  
  private static enum LogEventType {
    EMAIL_CREATED,
    EMAIL_SENDING_OK,
    EMAIL_SENDING_ERROR,
    EMAIL_SENDING_SKIPPED,
    EMAIL_SENDING_DIABLED,
    
    
    INVALID_RECIPIENT_SKIPPED,
    CURRUPT_RECIPIENT_SKIPPED,
    BLOCKED_RECIPIENT_SKIPPED
  }

  public EmailSendingLogger emailCreated(List<String> recipients, String title, String refId) {    
    events.add(LogEvent.builder()
      .props(Map.of(
          "refId", refId,
          "title", title,
          "recipients", log.isDebugEnabled() ? String.join(", ", recipients) : String.valueOf(recipients.size()) 
      ))
      .type(LogEventType.EMAIL_CREATED)
      .build());
    
    return this;
  }
  
  public EmailSendingLogger noValidRecipients() {
    if(log.isWarnEnabled()) {
      events.add(LogEvent.builder()
        .props(Map.of(
            "text", "no valid or allowed email addresses"
        ))
        .type(LogEventType.EMAIL_SENDING_SKIPPED)
        .build()
      );
    }
    return this;
  }
  
  public EmailSendingLogger noRecipients() {
    if(log.isWarnEnabled()) {
      events.add(LogEvent.builder()
        .props(Map.of(
            "text", "no email addresses"
        ))
        .type(LogEventType.EMAIL_SENDING_SKIPPED)
        .build()
      );
    }
    return this;
  }
  
  public EmailSendingLogger invalidRecipientSkipped(String invalidRecipient) {
    if(log.isWarnEnabled()) {
      events.add(LogEvent.builder()
        .props(Map.of(
            "text", "invalid recipient skipped"
        ))
        .type(LogEventType.INVALID_RECIPIENT_SKIPPED)
        .build()
      );
    }
    
    return this;
  }
  public EmailSendingLogger corrupRecipientSkipped(String invalidRecipient, AddressException e) {
    if(log.isWarnEnabled()) {
      events.add(LogEvent.builder()
        .props(Map.of(
            "text", "corrupt recipient skipped",
            "recipient", invalidRecipient,
            "error", e.getMessage(),
            "stack", ExceptionUtils.getStackTrace(e)
        ))
        .type(LogEventType.CURRUPT_RECIPIENT_SKIPPED)
        .build()
      );
    }
    return this;
  }
  
  public EmailSendingLogger blockedRecipientSkipped(InternetAddress invalidRecipient) {
    if(log.isInfoEnabled()) {
      events.add(LogEvent.builder()
        .props(Map.of(
            "text", "blocked recipient skipped",
            "recipient", invalidRecipient.toUnicodeString()
        ))
        .type(LogEventType.BLOCKED_RECIPIENT_SKIPPED)
        .build()
      );
    }
    return this;
  }

  public EmailSendingLogger emailSent(List<InternetAddress> recipient) {
    
    if(log.isInfoEnabled()) {
      events.add(LogEvent.builder()
        .props(Map.of(
            "text", "emails sent successfully",
            "recipients", String.valueOf( recipient.size())
        ))
        .type(LogEventType.EMAIL_SENDING_OK)
        .build()
      );
    }
    return this;
  }
  public EmailSendingLogger emailFailed(List<InternetAddress> recipient, Exception e) {
    events.add(LogEvent.builder()
        .props(Map.of(
            "text", "failed to send any email",
            "recipients", String.valueOf( recipient.size()),
            "error", e.getMessage(),
            "stack", ExceptionUtils.getStackTrace(e)
        ))
        .type(LogEventType.EMAIL_SENDING_ERROR)
        .build()
      );
    return this;
  }
  
  
  public EmailSendingLogger emailDisabled() {
    if(log.isInfoEnabled()) {
      events.add(LogEvent.builder()
          .props(Map.of(
              "text", "email sending disabled in configuration"
          ))
          .type(LogEventType.EMAIL_SENDING_DIABLED)
          .build()
        );
    }
    return this;
  }
  
  public void close() {
    final var cost = System.currentTimeMillis() - start;
    
    final var result = new StringBuilder("events:").append(System.lineSeparator());
    for(final var event : events) {
      result.append("  - ").append(event.getType()).append(":").append(System.lineSeparator());
      event.props.entrySet().forEach(e -> 
        result.append("    ").append(e.getKey()).append(": ").append(e.getValue()).append(System.lineSeparator())
      );
    }
    
    final var warnings = Arrays.asList(
        LogEventType.EMAIL_SENDING_SKIPPED, 
        LogEventType.INVALID_RECIPIENT_SKIPPED, 
        LogEventType.CURRUPT_RECIPIENT_SKIPPED,
        LogEventType.BLOCKED_RECIPIENT_SKIPPED);
    
    final var isError = !events.stream()
      .filter(e -> e.getType() == LogEventType.EMAIL_SENDING_ERROR)
      .findFirst().isEmpty();
    if(isError) {
      log.error("Failed to send any emails, cost: {} millis, \r\n{}", cost, result.toString());
      return;
    } 
    
    final var isWarn = !events.stream()
      .filter(e -> warnings.contains(e.getType()))
      .findFirst()
      .isEmpty();
    if(isWarn) {
      log.warn("Some email's where skipped, cost: {} millis, \r\n{}", cost, result.toString());
      return;
    } 
    
    log.info("Sent all emails, cost: {} millis, \r\n{}", cost, result.toString());
  }
}
