package io.digiexpress.thena.mq.client.spi;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ComparisonChain;

import io.digiexpress.thena.mq.client.api.entities.Channel;
import io.digiexpress.thena.mq.client.api.persistence.ThenaMqChannelState;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.jackson.QuarkusJacksonJsonCodec;
import io.vertx.core.json.JsonObject;

public class ChannelPrinter {
  private final ThenaMqChannelState state;

  public ChannelPrinter(ThenaMqChannelState state) {
    super();
    this.state = state;
  } 
 
  public String print(Channel repo) {
   return internalPrinting(repo, false, null);
  }
  public String printWithStaticIds(Channel repo, Map<String, String> replacements) {
    return internalPrinting(repo, true, replacements);
  }
  
  
  public String internalPrinting(Channel repo, boolean isStatic, final Map<String, String> collector) {
    final Map<String, String> wipes = new HashMap<>();
    final Map<String, String> replacements = collector != null ? collector : new HashMap<>();
    final Function<String, String> ID = (id) -> {
      if(!isStatic) {
        return id;
      }
      if(id == null) {
        return null;
      }
      
      if(replacements.containsKey(id)) {
        return replacements.get(id);
      }
      final var next = String.valueOf(replacements.size() + 1);
      replacements.put(id, next);
      return next;
    };
    
    final Function<GrimOneOfRelations, String> RELS = (id) -> {

      if(id == null) {
        return "";
      }
      if(!isStatic) {
        return id.getTargetId();
      }

      if(replacements.containsKey(id.getTargetId())) {
        return replacements.get(id.getTargetId());
      }
      final var next = String.valueOf(replacements.size() + 1);
      replacements.put(id.getTargetId(), next);
      return next;
    };

    final Function<Object, String> TR = (input) -> {

      if(input == null) {
        return "null";
      }
      if(!isStatic) {
        return "null";
      }
      final var id = JsonObject.mapFrom(input).encode();
      if(wipes.containsKey(id)) {
        return wipes.get(id);
      }
      wipes.put(id, "null");
      return "null";
    };
    
    
    final Function<OffsetDateTime, String> DATES = (input) -> {
      if(input == null) {
        return null;
      }
      try {
        final var id = QuarkusJacksonJsonCodec.mapper().writeValueAsString(input);
        if(!isStatic) {
          return id.toString();
        }
  
        if(replacements.containsKey(id)) {
          return replacements.get(id);
        }
        final var next = "\"OffsetDateTime.now()\"";
        replacements.put(id, next);
        return next;
      } catch(Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    };

    final var ctx = state.withChannel(repo);
    
    StringBuilder result = new StringBuilder();

    result
    .append(System.lineSeparator())
    .append("Channel").append(System.lineSeparator())
    .append("  - id: ").append(ID.apply(repo.getId()))
    .append("    name: ").append(repo.getChannelName()).append(System.lineSeparator())
    .append("    prefix: ").append(ID.apply(repo.getPrefix())).append(System.lineSeparator());
    
    ctx.queryContainers().findAll()
    .onItem()
    .transform(items -> {
     
      result.append("  Messages: ").append(System.lineSeparator());
      for(final var data : items.getPublishedMessages().values()
          .stream()
          .sorted((a, b) -> ComparisonChain.start()
              .compare(a.getCreatedAt(), b.getCreatedAt())
              .result())
          .toList()
          ) {
        ID.apply(data.getId());
        DATES.apply(data.getCreatedAt());
        
        result
          .append("  - ").append(ID.apply(data.getId())).append("::").append(System.lineSeparator())
          .append("    body id: ").append(data.getBodyId()).append(System.lineSeparator())
          .append("    body type: ").append(data.getBodyType()).append(System.lineSeparator())
          .append("    body value: ").append(data.getBodyValue()).append(System.lineSeparator())
          
          .append("    starts at: ").append(data.getStartsAt()).append(System.lineSeparator())
          .append("    expires at: ").append(data.getExpiresAt()).append(System.lineSeparator())          
          .append("    comment: ").append(data.getComment()).append(System.lineSeparator())
          ;
      }
      
      
      result
        .append(System.lineSeparator())
        .append(System.lineSeparator())
        .append("  Queues: ").append(System.lineSeparator());
      for(final var data : items.getQueues().values()
          .stream()
          .sorted((a, b) -> ComparisonChain.start()
              .compare(a.getCreatedAt(), b.getCreatedAt())
              .result())
          .toList()
          ) {
        ID.apply(data.getId());
        DATES.apply(data.getCreatedAt());
        
        result
          .append("  - ").append(ID.apply(data.getId())).append("::").append(System.lineSeparator())
          .append("    queue name: ").append(data.getQueueName()).append(System.lineSeparator())
          .append("    created by: ").append(data.getCreatedBy()).append(System.lineSeparator())
          .append("    comment: ").append(data.getComment()).append(System.lineSeparator())
          ;
      }

      
    result
      .append(System.lineSeparator())
      .append(System.lineSeparator())
      .append("  Deliveries: ").append(System.lineSeparator());
    for(final var data : items.getDeliveries().values()
        .stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getCreatedAt(), b.getCreatedAt())
            .result())
        .toList()
        ) {
      ID.apply(data.getId());
      DATES.apply(data.getCreatedAt());
      
      result
        .append("  - ").append(ID.apply(data.getId())).append("::").append(System.lineSeparator())
        .append("    status: ").append(data.getStatus()).append(System.lineSeparator())
        .append("    starts at: ").append(data.getStartsAt()).append(System.lineSeparator())
        .append("    expires at: ").append(data.getExpiresAt()).append(System.lineSeparator())
        ;
    }
    
    result
    .append(System.lineSeparator())
    .append(System.lineSeparator())
    .append("  Deliveries Attempts: ").append(System.lineSeparator());
  for(final var data : items.getDeliveryAttempts().values()
      .stream()
      .sorted((a, b) -> ComparisonChain.start()
          .compare(a.getCreatedAt(), b.getCreatedAt())
          .result())
      .toList()
      ) {
    ID.apply(data.getId());
    DATES.apply(data.getCreatedAt());
    
    result
      .append("  - ").append(ID.apply(data.getId())).append("::").append(System.lineSeparator())
      .append("    ack comment: ").append(data.getConsumerComment()).append(System.lineSeparator())
      .append("    ack error: ").append(data.getConsumerError()).append(System.lineSeparator())
      .append("    ack error: ").append(data.getConsumerStatus()).append(System.lineSeparator())
      ;
  }
  
    
      return items;
    })
    .await().indefinitely();
    
    return result.toString();
  }
}
