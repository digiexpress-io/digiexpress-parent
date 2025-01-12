package io.digiexpress.thena.mq.client.spi;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import io.digiexpress.thena.mq.client.api.ThenaMqClient.QueueBuilder;
import io.digiexpress.thena.mq.client.api.entities.ImmutableLog;
import io.digiexpress.thena.mq.client.api.entities.ImmutableQueue;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ImmutableChannelBatch;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Setter @Getter @Accessors(fluent = true)
public class OneQueueBuilderImpl implements QueueBuilder {
  private final List<Queue> allQueues;
  private final String createdBy;
  private final ImmutableChannelBatch.Builder batch;
  private boolean built = false;
  private String queueName;
  private String comment;
  private Optional<Queue> existingQueue;
  private Queue result;

  @Override
  public Queue build() {
    RepoAssert.notEmpty(queueName, () -> "queueName must be defined!");
    RepoAssert.notEmpty(comment, () -> "queue comment must be defined!");
   
    this.built = true;
    this.existingQueue = allQueues.stream()
        .filter(queue -> queue.getQueueName().equals(queueName) || queue.getId().equals(queueName))
        .findFirst();
    
    if(existingQueue.isPresent()) {
      this.result = existingQueue.get();
      batch.addLogs(ImmutableLog.builder().text("Skipping queue: '" + queueName + "' creation, because it already exists.").build());
      return this.result;
    }
    
    final var newQueue = ImmutableQueue.builder()
        .id(OidUtils.gen())
        .createdAt(OffsetDateTime.now())
        .comment(comment)
        .createdBy(createdBy)
        .queueName(queueName)
        .build();
    
    this.result = newQueue;
    
    batch
      .batchStatus(OperationStatus.OK)
      .addLogs(ImmutableLog.builder().text("Created new queue: '" + queueName + "'.").build())
      .addNewQueues(newQueue);
    return newQueue;
  }
}