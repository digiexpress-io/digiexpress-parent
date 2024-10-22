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

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.ProcessCommands;
import io.digiexpress.eveli.client.spi.ProcessCommandsImpl;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class ProcessBaseController {
  protected final PortalClient client;
  
  protected ResponseEntity<List<ProcessCommands.Process>> searchProcesses(
      String name, 
      List<String> status,
      String userId, 
      Pageable pageable) {
    
    final var entries = client.process().query().find(name, status, userId, pageable);
    final List<ProcessCommands.Process> processes = new ArrayList<ProcessCommands.Process>();
    entries.map(process -> processes.add(ProcessCommandsImpl.map(process)));
    
    return new ResponseEntity<List<ProcessCommands.Process>>(processes, HttpStatus.OK);
  }
}
