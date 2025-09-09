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

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.digiexpress.eveli.client.api.HealthClient;
import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.api.HealthClient.HealthEntry;
import io.digiexpress.eveli.client.api.HealthClient.UserActivity;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;

/**
 * Rest controller to handle external requests from admin UI.
 */
@RestController
@RequestMapping("/worker/rest/api/health")
@RequiredArgsConstructor
public class HealthApiController {
  private final HealthClient healthClient;
  private final WorkerAuthClient worker;
  
  @GetMapping("/task-activity")
  public Multi<HealthEntry> tasksActivity() {
    if(!worker.getUser().getPrincipal().isAdmin()) {
      throw new ResponseStatusException(HttpStatusCode.valueOf(403));
    }
    return healthClient.createHealthQuery().findAll();
  }
  
  @GetMapping("/user-activity")
  public Multi<UserActivity> userActivity() {
    if(!worker.getUser().getPrincipal().isAdmin()) {
      throw new ResponseStatusException(HttpStatusCode.valueOf(403));
    }
    return healthClient.createUserActivityQuery().findAllAfter(OffsetDateTime.now().minusDays(30));
  }
}
