package io.digiexpress.eveli.client.web.resources.worker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.TaskClient;
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
  public Multi<ProcessClient.ProcessInstance> findLast6Months() {
    return taskClient.queryTaskProcesess().findLast6Months();
  }
}
