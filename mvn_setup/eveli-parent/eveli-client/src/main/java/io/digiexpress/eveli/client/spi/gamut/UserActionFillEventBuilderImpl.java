package io.digiexpress.eveli.client.spi.gamut;

/*-
 * #%L
 * eveli-client
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

import io.digiexpress.eveli.client.api.GamutClient.UserActionFillEvent;
import io.digiexpress.eveli.client.api.GamutClient.UserActionFillEventBuilder;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Data @Accessors(fluent = true)
@RequiredArgsConstructor
public class UserActionFillEventBuilderImpl implements UserActionFillEventBuilder {

  private String sessionId;
  private String requestBody;
  private String responseBody;

  @Override
  public UserActionFillEvent create() {
    TaskAssert.notNull(sessionId, () -> "sessionId can't be null!");    
    TaskAssert.notNull(requestBody, () -> "requestBody can't be null!");    
    TaskAssert.notNull(responseBody, () -> "responseBody can't be null!");    
    
    final var event = UserActionFillEvent.builder()
        .requestBody(requestBody)
        .responseBody(responseBody)
        .sessionId(sessionId)
        .build();
    return event;
  }
  
}
