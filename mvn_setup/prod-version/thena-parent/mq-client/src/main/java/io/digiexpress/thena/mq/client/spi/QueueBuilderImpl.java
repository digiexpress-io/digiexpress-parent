package io.digiexpress.thena.mq.client.spi;

import java.util.function.Consumer;

import io.digiexpress.thena.mq.client.api.ThenaMqClient.ConsumerBuilder;
import io.digiexpress.thena.mq.client.api.ThenaMqClient.QueueBuilder;
import io.digiexpress.thena.mq.client.api.entities.Queue;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class QueueBuilderImpl implements QueueBuilder {
  private final ThenaMqChannelState state;

  private String queueName;
  private String comment;
  private String createdBy;

  @Override
  public QueueBuilder addConsumer(Consumer<ConsumerBuilder> worker) {
    // TODO Auto-generated method stub
    return this;
  }

  @Override
  public Uni<ThenaMqEnvelope<Queue>> build() {
    RepoAssert.notEmpty(comment, () -> "comment must be defined!");
    RepoAssert.notEmpty(createdBy, () -> "createdBy must be defined!");
    RepoAssert.notEmpty(queueName, () -> "queueName must be defined!");
    return null;
  }
}
