package io.digiexpress.eveli.client.web.resources.worker;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;

/**
 * Rest controller to handle external requests from admin UI.
 */
@RestController
@RequestMapping("/worker/rest/api/processes")
@RequiredArgsConstructor
public class ProcessApiController {
  protected final TaskClient taskClient;


  @GetMapping("/last-6-months")
  public Multi<ProcessInstance> findLast6Months() {
    return taskClient.queryTaskProcesess().findLast6Months();
  }
}
