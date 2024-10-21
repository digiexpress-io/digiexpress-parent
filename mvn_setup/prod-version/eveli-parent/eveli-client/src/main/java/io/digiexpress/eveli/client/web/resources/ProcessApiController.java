package io.digiexpress.eveli.client.web.resources;

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
