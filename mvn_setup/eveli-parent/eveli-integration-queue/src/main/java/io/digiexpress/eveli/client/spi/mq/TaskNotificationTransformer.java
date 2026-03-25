package io.digiexpress.eveli.client.spi.mq;

import io.digiexpress.eveli.client.spi.mq.WrenchFlowCommand.TaskNotification;

public interface TaskNotificationTransformer {
  String transform(String message, TaskNotification notification, String locale);
}
