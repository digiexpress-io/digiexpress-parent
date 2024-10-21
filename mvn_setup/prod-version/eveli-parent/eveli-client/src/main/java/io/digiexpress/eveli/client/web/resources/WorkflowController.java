package io.digiexpress.eveli.client.web.resources;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.PortalClient;
import io.digiexpress.eveli.client.api.WorkflowCommands.Workflow;
import io.digiexpress.eveli.client.api.WorkflowCommands.WorkflowInit;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class WorkflowController {

  private final PortalClient client;

  @GetMapping("/workflows/")
  @Transactional
  public ResponseEntity<List<Workflow>> getAllWorkflows() {
    return new ResponseEntity<>(client.workflow().query().findAll(), HttpStatus.OK);
  }
  
  @PostMapping("/workflows/")
  @Transactional
  public ResponseEntity<Workflow> create(@RequestBody WorkflowInit workflow) {
    return new ResponseEntity<>(client.workflow().create(workflow), HttpStatus.CREATED);
  }
  
  @GetMapping("/workflows/{id}")
  @Transactional
  public ResponseEntity<Workflow> get(@PathVariable("id") String id) {
    final var workflow = client.workflow().query().get(id);
    if(workflow.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(workflow.get(), HttpStatus.OK);
  }
  
  @PutMapping("/workflows/{id}")
  @Transactional
  public ResponseEntity<Workflow> save(@PathVariable("id") String id, @RequestBody Workflow workflow) {
    final var entity = client.workflow().update(id, workflow);
    if(entity.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return new ResponseEntity<>(entity.get(), HttpStatus.OK);
  }
  
  @GetMapping(path="/workflowAssets")
  public ResponseEntity<List<String>> allFlows() {
    return ResponseEntity.status(HttpStatus.OK).body(client.hdes().query().findFlowNames());
  }
}
