package io.digiexpress.eveli.client.web.resources;

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
