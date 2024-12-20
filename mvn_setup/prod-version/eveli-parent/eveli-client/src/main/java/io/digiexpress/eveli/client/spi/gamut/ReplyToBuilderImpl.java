package io.digiexpress.eveli.client.spi.gamut;

import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.GamutClient.ProcessNotFoundException;
import io.digiexpress.eveli.client.api.GamutClient.ReplayToInit;
import io.digiexpress.eveli.client.api.GamutClient.ReplyToBuilder;
import io.digiexpress.eveli.client.api.GamutClient.UserMessage;
import io.digiexpress.eveli.client.api.ImmutableCreateTaskCommentCommand;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class ReplyToBuilderImpl implements ReplyToBuilder {
  private final ProcessClient processRepository;
  private final TaskClient taskClient;
  private final CrmClient authClient;
  private String actionId;
  private ReplayToInit from;


  @Override
  public UserMessage createOne() throws ProcessNotFoundException {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    TaskAssert.notNull(from, () -> "from can't be null!");
    
    final var process = processRepository.queryInstances().findOneById(actionId)
        .orElseThrow(() -> new ProcessNotFoundException("Process not found by id: " + actionId + "!"));
    
    final var customer = authClient.getCustomer().getPrincipal();    
    final var taskId = process.getTaskId();
    
   final var savedComment = taskClient.taskBuilder()
       .userId(customer.getUsername(), null)
       .createTaskComment(ImmutableCreateTaskCommentCommand.builder()
        .taskId(taskId)
        .commentText(from.getText())
        .external(true)
        .source(TaskCommentSource.PORTAL)
        .build())
       .await().atMost(TaskMapper.atMost);
    ;
    
    return UserMessagesQueryImpl.visitUserMessage(savedComment, authClient.getCustomer());
  }

}
