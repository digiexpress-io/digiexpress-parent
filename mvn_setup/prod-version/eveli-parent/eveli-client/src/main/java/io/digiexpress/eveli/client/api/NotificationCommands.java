
package io.digiexpress.eveli.client.api;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;



public interface NotificationCommands {
  NotificationBuilder create();
  EmailNotificationBuilder createEmail();
  GroupMembershipQuery createMembershipQuery();

  interface NotificationBuilder {
    NotificationBuilder title(String title);
    NotificationBuilder content(String content);
    NotificationBuilder userId(String userId);
    NotificationBuilder userIdType(String userId, ClientType userType);
    NotificationBuilder notificationId(String notificationId);
    NotificationResponse build();
  }

  interface EmailNotificationBuilder {
    EmailNotificationBuilder title(String notificationTitle); 
    EmailNotificationBuilder message(String notificationMessage);
    EmailNotificationBuilder address(String recipientAddress);
    EmailNotificationBuilder refId(String refId);
    EmailNotificationBuilder addresses(List<String> recipientAddress);
    void build();
  }

  
  interface GroupMembershipQuery {
    Set<String> queryMembership(String groupName); 
  }
  
  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public class EmailRequest {

    private String notificationTitle; 
    private String notificationMessage;
    @Singular
    private List<String> recipientAddresses;
    private String refId;
  }

  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public class EmailResponse {
    /**
     * Possible codes:
     * <ul>
     * <li> 0 - message sent
     * <li> 403 - No valid email addresses
     * <li> 404 - No email addresses
     * <li> 405 - Email sending disabled
     * <li> 500 - technical error
     * </ul>
     */
    private int responseCode;
    private String message;
    @Builder.Default
    private int emailCount = 0;
  }

  
  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public class NotificationRequest {
    private Client client;
    private String notificationId;
    private String notificationTitle; 
    private String notificationMessage;
  }
  
  public enum ClientType {
    SSN,
    CRN
  }
  
  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Client {
    private String clientId;
    private ClientType clientType;
  }

  @Data
  @Builder(toBuilder = true)
  @NoArgsConstructor
  @AllArgsConstructor
  public class NotificationResponse {
    /**
     * Possible codes:
     * <ul>
     * <li> 0 - message sent
     * <li> 204 - Client has not enabled message receiving 
     * <li> 400..499 - error code, see https://palveluhallinta.suomi.fi/fi/tuki/artikkelit/6231a819e014bf0100455b70 for TilaKoodi values
     * <li> 500 - technical error
     * <li> 307 - message sending is disabled in system
     * </ul>
     */
    private int responseCode;
    private String message;
  }

}