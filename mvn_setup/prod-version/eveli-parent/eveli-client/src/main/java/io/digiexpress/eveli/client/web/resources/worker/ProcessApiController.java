package io.digiexpress.eveli.client.web.resources.worker;

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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.ProcessClient;
import lombok.RequiredArgsConstructor;

/**
 * Rest controller to handle external requests from admin UI.
 */
@RestController
@RequestMapping("/worker/rest/api/processes")
@RequiredArgsConstructor
public class ProcessApiController {
  protected final ProcessClient client;

  @Transactional
  @GetMapping
  public ResponseEntity<Page<ProcessClient.ProcessInstance>> processesSearch(
      @RequestParam(name="workflow.name", defaultValue= "") String name, 
      @RequestParam(name="status", required = false) List<String> status,
      @RequestParam(name="userId", defaultValue = "") String userId, 
      Pageable pageable) {
    
    final var processes = client.paginateInstances()
        .name(name)
        .status(status)
        .userId(userId)
        .page(pageable)
        .findAll();
    return new ResponseEntity<>(processes, HttpStatus.OK);
  }
}
