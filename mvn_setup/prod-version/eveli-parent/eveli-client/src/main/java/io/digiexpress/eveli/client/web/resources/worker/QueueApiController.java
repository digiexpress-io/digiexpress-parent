package io.digiexpress.eveli.client.web.resources.worker;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.thena.mq.client.api.ThenaMqClient;
import io.digiexpress.thena.mq.client.api.entities.ChannelConfig;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/worker/rest/api/queues")
@RequiredArgsConstructor
public class QueueApiController {
  private final ThenaMqClient client;
  private final String channelName;
  
  @GetMapping(path = "/configs")
  public Uni<List<ChannelConfig>> findAllFeedback() {
    return client.channelConfigQuery()
        .getOne(channelName)
        .onItem().transform(resp -> {
            if(resp.getObject() == null) {
              return Collections.emptyList();
            }
            return Arrays.asList(resp.getObject()); 
        });
  }
}
