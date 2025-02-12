package io.digiexpress.eveli.client.spi.comms;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.ResponseEntity;

import io.digiexpress.eveli.client.spi.comms.EmailBuilderDelegate.EmailResponse;
import io.vertx.core.json.JsonObject;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmailBuilderLogger {
  private List<LogEvent> events;

  private final long start = System.currentTimeMillis();
  
  @Data @Builder
  private static class LogEvent {
    private final Map<String, String> props;
    private final LogEventType type;
  }
  
  private static enum LogEventLevel {
    INFO, ERROR, WARN, DEBUG
  }
  
  @Getter
  private static enum LogEventType {
    EMAIL_CREATED("EMAIL_CREATED", LogEventLevel.INFO),
    EMAIL_SENDING_WITH_DELEGATE("EMAIL_SENDING_WITH_DELEGATE", LogEventLevel.DEBUG),
    EMAIL_SENDING_OK("EMAIL_SENDING_OK", LogEventLevel.INFO),
    EMAIL_SENDING_SERVICE_URL_MISSING("EMAIL_SENDING_SERVICE_URL_MISSING", LogEventLevel.WARN),
    EMAIL_SENDING_ERROR("EMAIL_SENDING_ERROR", LogEventLevel.ERROR),
    EMAIL_SENDING_SKIPPED("EMAIL_SENDING_SKIPPED", LogEventLevel.WARN),
    EMAIL_SENDING_DIABLED("EMAIL_SENDING_DIABLED", LogEventLevel.WARN),
    
    INVALID_RECIPIENT_SKIPPED("INVALID_RECIPIENT_SKIPPED", LogEventLevel.WARN),
    CURRUPT_RECIPIENT_SKIPPED("CURRUPT_RECIPIENT_SKIPPED", LogEventLevel.WARN),
    BLOCKED_RECIPIENT_SKIPPED("BLOCKED_RECIPIENT_SKIPPED", LogEventLevel.WARN);
    
    
    private final LogEventLevel level; 
    private final String name;
    
    private LogEventType(String name, LogEventLevel level) {
      this.name = name;
      this.level = level;
    }
  }

  public EmailBuilderLogger emailCreated(List<String> recipients, String title, String refId) {    
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
  
  public EmailBuilderLogger noValidRecipients() {
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
  
  public EmailBuilderLogger noRecipients() {
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
  
  public EmailBuilderLogger invalidRecipientSkipped(String invalidRecipient) {
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
  public EmailBuilderLogger corrupRecipientSkipped(String invalidRecipient, AddressException e) {
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
  
  public EmailBuilderLogger blockedRecipientSkipped(InternetAddress invalidRecipient) {
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
  public EmailBuilderLogger emailSentWithDelegate(String serviceUrl) {
    if(log.isDebugEnabled()) {
      events.add(LogEvent.builder()
        .props(Map.of(
            "text", "trying to send emails using rest api",
            "serviceUrl", serviceUrl
        ))
        .type(LogEventType.EMAIL_SENDING_WITH_DELEGATE)
        .build()
      );
    }
    return this;
  }
  
  public EmailBuilderLogger emailSent(List<?> recipient) {
    return emailSent(recipient.size());
  }
  public EmailBuilderLogger emailSent(int recipient) {
    if(log.isInfoEnabled()) {
      events.add(LogEvent.builder()
        .props(Map.of(
            "text", "emails sent successfully",
            "recipients", String.valueOf(recipient)
        ))
        .type(LogEventType.EMAIL_SENDING_OK)
        .build()
      );
    }
    return this;
  }
  public EmailBuilderLogger emailFailed(List<?> recipient, Exception e) {
    events.add(LogEvent.builder()
        .props(Map.of(
            "text", "failed to send any emails",
            "recipients", String.valueOf( recipient.size()),
            "error", e.getMessage(),
            "stack", ExceptionUtils.getStackTrace(e)
        ))
        .type(LogEventType.EMAIL_SENDING_ERROR)
        .build()
      );
    return this;
  }

  public EmailBuilderLogger emailFailed(ResponseEntity<EmailResponse> resp) {
    events.add(LogEvent.builder()
        .props(Map.of(
            "text", "failed to send any emails using rest api",
            "status code", String.valueOf(resp.getStatusCode().value()),
            "body", JsonObject.mapFrom(resp.getBody()).encode()
        ))
        .type(LogEventType.EMAIL_SENDING_ERROR)
        .build()
      );
    return this;
  }

  
  public EmailBuilderLogger emailDisabled() {
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
  
  public EmailBuilderLogger emailDisabledServiceUrlMissing() {
    if(log.isWarnEnabled()) {
      events.add(LogEvent.builder()
          .props(Map.of(
              "text", "email sending disabled because service url is not configured"
          ))
          .type(LogEventType.EMAIL_SENDING_SERVICE_URL_MISSING)
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
    ;
    
    final var isError = events.stream()
      .filter(e -> e.getType().getLevel() == LogEventLevel.ERROR)
      .findFirst().isPresent();
    if(isError) {
      log.error("Failed to send any emails, cost: {} millis, \r\n{}", cost, result.toString());
      return;
    } 
    
    final var isWarn = events.stream()
      .filter(e -> e.getType().getLevel() == LogEventLevel.WARN)
      .findFirst()
      .isPresent();
    if(isWarn) {
      log.warn("Some email's where skipped, cost: {} millis, \r\n{}", cost, result.toString());
      return;
    }
    
    final var isDebug = events.stream()
        .filter(e -> e.getType().getLevel() == LogEventLevel.DEBUG)
        .findFirst()
        .isPresent();
    if(isDebug) {
      log.debug("Sent all emails, cost: {} millis, \r\n{}", cost, result.toString());
      return;
    }
    
    log.info("Sent all emails, cost: {} millis, \r\n{}", cost, result.toString());
  }
}
