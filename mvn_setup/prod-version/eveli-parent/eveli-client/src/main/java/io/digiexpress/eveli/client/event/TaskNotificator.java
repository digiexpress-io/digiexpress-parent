package io.digiexpress.eveli.client.event;

import io.digiexpress.eveli.client.api.TaskCommands;

public interface TaskNotificator {

  void sendNewCommentNotificationToClient(TaskCommands.TaskComment comment, TaskCommands.Task taskModel);
  void handleTaskUpdate(TaskCommands.Task newTask, TaskCommands.Task previous, String userEmail);
  void handleTaskCreation(TaskCommands.Task createdTask, String userEmail);
}
