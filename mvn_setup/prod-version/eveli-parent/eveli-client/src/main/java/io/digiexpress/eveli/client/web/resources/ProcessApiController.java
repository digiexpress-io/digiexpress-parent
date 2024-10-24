package io.digiexpress.eveli.client.web.resources;

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

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.ProcessCommands;

@RestController
/**
 * Rest controller to handle external requests from admin UI.
 */
public class ProcessApiController extends ProcessBaseController {

  public ProcessApiController(PortalClient client) {
    super(client);
  }
  
  @Transactional
  @GetMapping("/api/processesSearch")
  public ResponseEntity<List<ProcessCommands.Process>> searchProcesses(
      @RequestParam(name="workflow.name", defaultValue="") String name, 
      @RequestParam(name="status", required=false) List<String> status,
      @RequestParam(name="userId", defaultValue="") String userId, 
      Pageable pageable) {
    
    return super.searchProcesses(name, status, userId, pageable);
  }
}
