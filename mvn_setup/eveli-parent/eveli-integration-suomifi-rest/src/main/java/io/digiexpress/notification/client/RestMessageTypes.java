package io.digiexpress.notification.client;

/*-
 * #%L
 * eveli-integration-suomifi-rest
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Data;

public class RestMessageTypes {
  @Data
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class NewMessageFromClientOrganization {
  
    private NewElectronicPart electronic;
    private String externalId;
    private NewNormalPaperMail paperMail;
    private Recipient recipient;
    private Sender sender;
  }
  
  @Data
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class NewElectronicMessageFromClientOrganization {
    
    private NewElectronicOnly electronic;
    private String externalId;
    private Recipient recipient;
    private Sender sender;
  }
  
  @Data
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class NewElectronicPart {
    private String body;
    private FileReference files;
    private MessageServiceType messageServiceType;
    private ReplyAllowedBy replyAllowedBy;
    private String title;
    private MessageNotifications notifications;
    private Visibility visibility;
  }
  
  @Data
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class NewElectronicOnly {
    private Object[] attachments;
    private String body;
    private BodyFormat bodyFormat;
    // optional
    private Long inReplyToMessageId;
    private MessageServiceType messageServiceType;    
    private MessageNotifications notifications;
    private ReplyAllowedBy replyAllowedBy;
    private String title;
    private Visibility visibility;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class NewNormalPaperMail {
    // no paper mail support needed currently, left empty
  }

  @Data
  public static class RestMessageResponse {
    private Long messageId;    
  }
  
  @Data
  public static class Recipient {
    // Personal identity code or Business identity code, length 9 or 11 characters 
    private String id;
  }

  @Data
  public static class Sender {
    private String serviceId;
  }

  @Data
  public static class FileReference {
    private String fileId;
  }

  public enum MessageServiceType {
    Normal, Verifiable    
  }

  public enum BodyFormat {
    Text, Markdown
  }
  
  public enum ReplyAllowedBy {
    Anyone("Anyone"), NoOne("No one");
    private String name;

    ReplyAllowedBy(String allowedBy) {
      this.name = allowedBy;
    }
    @JsonValue
    public String getName() {
      return name;
    }
  }

  @Data
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class MessageNotifications {
    // Optional
    private CustomisedMessageNotification customisedNewMessageNotification;
    private SenderDetailsInNotifications senderDetailsInNotifications;
    private UnreadMessageNotification unreadMessageNotification;
  }

  public enum Visibility {
    Normal("Normal"), RecipientOnly("Recipient only");
    private String name;

    Visibility(String visibility) {
      this.name = visibility;
    }
    @JsonValue
    public String getName() {
      return name;
    }
  }

  @Data
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class CustomisedMessageNotification {
    private CustomisedMessageNotificationContent content;
    private CustomisedMessageNotificationTitle title;
  }

  @Data
  public static class UnreadMessageNotification {
    private Reminder reminder;
  }

  @Data
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class CustomisedMessageNotificationTitle {
    private String en;
    private String fi;
    private String sv;
  }

  @Data
  public static class CustomisedMessageNotificationContent {
    private String en;
    private String fi;
    private String sv;
  }

  public enum Reminder {
    DefaultReminder("Default reminder"), NoReminders("No reminders");
    private String name;

    Reminder(String reminder) {
      this.name = reminder;
    }
    @JsonValue
    public String getName() {
      return name;
    }
  }

  public enum SenderDetailsInNotifications {
    OrganisationAndServiceName("Organisation and service name"), None("None");
    private String name;

    SenderDetailsInNotifications(String details) {
      this.name = details;
    }
    @JsonValue
    public String getName() {
      return name;
    }
  }
}
