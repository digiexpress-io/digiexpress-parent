package io.digiexpress.eveli.client.spi.health;

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

import java.time.OffsetDateTime;

import io.digiexpress.eveli.client.api.HealthClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HealthClientImpl implements HealthClient {

  private final TaskClient taskClient;
  
  
  @Override
  public HealthQuery createHealthQuery() {
    return new HealthQuery() {
      @Override
      public Multi<HealthEntry> findAll() {
        return new HealthEntryVisitor(taskClient).accept();
      }
    };
  }

  @Override
  public UserActivityQuery createUserActivityQuery() {
    return new UserActivityQuery() {
      @Override
      public Multi<UserActivity> findAllAfter(OffsetDateTime createdFromInclusive) {
        return new UserActivityVisitor(taskClient).accept(createdFromInclusive);
      }
    };
  }
}
