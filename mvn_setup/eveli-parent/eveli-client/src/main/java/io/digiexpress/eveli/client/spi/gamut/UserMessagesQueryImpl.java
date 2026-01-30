package io.digiexpress.eveli.client.spi.gamut;

import io.digiexpress.eveli.client.api.GamutAuthClient.Customer;
import io.digiexpress.eveli.client.api.GamutClient.UserMessage;
import io.digiexpress.eveli.client.api.GamutClient.UserMessagesQuery;
import io.digiexpress.eveli.client.api.ImmutableUserMessage;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskComment;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UserMessagesQueryImpl implements UserMessagesQuery {

  private final TaskClient taskClient;
  
  @Override
  public Multi<UserMessage> findAllByActionId(Customer customer, String actionId) {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    
    return taskClient.queryTaskProcesess().findOneById(actionId)
      .onItem().transformToMulti(process -> {
        if(process.isEmpty()) {
          return Multi.createFrom().empty();
        }
        
        final var taskId = process.get().getTaskId();
        return taskClient.queryTaskComments()
          .findAllByTaskId(taskId)
          .filter(comment -> Boolean.TRUE.equals(comment.getExternal()))
          .map(comment -> visitUserMessage(comment, customer))
          .onSubscription().call(sub -> taskClient.taskBuilder()
            .userId(customer.getPrincipal().getUsername(), null)
            .addCustomerCommitViewer(taskId)
          );
      });
  }
  
  @Override
  public Multi<UserMessage> findAllByUserId(Customer customer) {
    return taskClient.queryTaskComments()
        .findAllByReporterId(customer.getPrincipal().getUsername())
        .filter(comment -> Boolean.TRUE.equals(comment.getExternal()))
        .map(comment -> visitUserMessage(comment, customer));
  }
  
  public static UserMessage visitUserMessage(TaskComment msg, Customer customer) {
    final var replyToId = msg.getReplyToId();
    final var userMsg = ImmutableUserMessage.builder()
        .id(msg.getId().toString())
        .taskId(msg.getTaskId())
        .replyToId(replyToId)
        .created(msg.getCreated().toString())
        .userName(UserMessagesQueryImpl.visitMessageUserName(msg, customer))
        .commentText(msg.getCommentText())
        .build();
    
    return userMsg;
  }

  public static String visitMessageUserName(TaskComment entity, Customer customer) {
    
    final var user = customer.getPrincipal();
    
    final String userName;
    if(user.getRepresentedPerson() != null) {
      final var personNames = user.getRepresentedPerson().getRepresentativeName();
      userName = personNames[1] + " " + personNames[0];
    } else if(user.getRepresentedCompany() != null) {
      userName = user.getRepresentedCompany().getName();
    } else {
      userName = null;
    }
    
    final var representativeUserName = user.getUsername();
    
    if(entity.getUserName().equals(userName)) {
      return entity.getUserName();
    }
    if(entity.getUserName().equals(representativeUserName)) {
      return entity.getUserName();
    } 
    return "";
  }

}
