package io.digiexpress.thena.mq.client.spi;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.List;

import jakarta.annotation.Nullable;

import org.immutables.value.Value;

import io.digiexpress.thena.mq.client.api.entities.Channel;


public class ChannelException extends RuntimeException {
  private static final long serialVersionUID = 4311634600357697485L;

  public ChannelException(String msg) {
    super(msg);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    public ChannelExceptionMessage notChannelWithName(String channelName) {
      final var text = new StringBuilder()
          .append("Channel with name: '").append(channelName).append("' does not exist!")
          .toString();
      return ImmutableChannelExceptionMessage.builder()
            .text(text)
          .build();
    }
    public ChannelExceptionMessage notChannelWithName(String channel, List<Channel> others) {
      final var text = new StringBuilder()
          .append("Channel with name: '").append(channel).append("' does not exist!")
          .append(" known channels: '").append(String.join(",", others.stream().map(r -> r.getChannelName()).toList())).append("'")
          .toString();
      return ImmutableChannelExceptionMessage.builder()
            .text(text)
          .build();
    }
    public ChannelExceptionMessage nameNotUnique(String name, String id) {
      return ImmutableChannelExceptionMessage.builder()
            .text(new StringBuilder()
            .append("Repo with name: '").append(name).append("' already exists,")
            .append(" id: '").append(id).append("'")
            .append("!")
            .toString())
          .build();
    }
  }
  
  @Value.Immutable
  public interface ChannelExceptionMessage {
    String getText();
    @Nullable Throwable getException();
  }

}
