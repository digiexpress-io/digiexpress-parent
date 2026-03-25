package io.digiexpress.eveli.client.spi.mq;

import org.apache.commons.lang3.StringUtils;

import io.digiexpress.eveli.client.spi.mq.WrenchFlowCommand.TaskNotification;

public class DefaultTaskNotificationTransformer implements TaskNotificationTransformer {

  private final String TASK_REF_MARKER = "$TASK_REF";
  @Override
  public String transform(String message, TaskNotification notification, String locale) {
    if (StringUtils.isNotBlank(message)) {
      return message.replace(TASK_REF_MARKER, notification.getTaskRef() != null ? notification.getTaskRef() : "");
    }
    return message;
  }
}
