package io.digiexpress.thena.mq.client.spi;

import java.time.OffsetDateTime;
import java.util.List;

import io.digiexpress.thena.mq.client.api.ThenaMqClient.ChannelBuilder;
import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.entities.ImmutableChannel;
import io.digiexpress.thena.mq.client.api.entities.ImmutableThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope;
import io.digiexpress.thena.mq.client.api.entities.ThenaMqEnvelope.OperationStatus;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class ChannelBuilderImpl implements ChannelBuilder {
  private final ThenaMqChannelState state;

  private String channelName;
  private String comment;
  private String createdBy;
  private String externalId;
  
  @Override
  public Uni<ThenaMqEnvelope<Channel>> build() {
    RepoAssert.notEmpty(comment, () -> "comment must be defined!");
    RepoAssert.notEmpty(createdBy, () -> "createdBy must be defined!");
    RepoAssert.notEmpty(channelName, () -> "channelName must be defined!");
    
    return state.queryChannels().getByNameOrId(channelName)
        .onItem().transformToUni(found -> {
          if(found.isEmpty()) {
            return state.queryChannels().findAll().collect().asList()
                .onItem().transformToUni(this::createChannel);
          }
          return Uni.createFrom().item(found.get());
        })
        .onItem().transform(channel -> ImmutableThenaMqEnvelope.<Channel>builder()
            .operationStatus(OperationStatus.OK)
            .channel(channel)
            .channelId(channel.getId())
            .build()
      );
  }
  
  private Uni<Channel> createChannel(List<Channel> channels) {
    final var codeName = channelName.toUpperCase();
    final var prefixStart = codeName.substring(0, Math.min(codeName.length(), 20));
    final var prefix = prefixStart.replace("-", "_") + "_" +(channels.size() + 10) + "_" ;
    return state.insertOne(ImmutableChannel.builder()
        .id(OidUtils.gen())
        .channelName(channelName)
        .createdBy(createdBy)
        .externalId(externalId)
        .comment(comment)
        .createdAt(OffsetDateTime.now())
        .prefix(prefix.toUpperCase())
        .build());
  }
}
