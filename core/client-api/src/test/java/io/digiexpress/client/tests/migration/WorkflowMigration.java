package io.digiexpress.client.tests.migration;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ClientEntity.ServiceDescriptor;
import io.digiexpress.client.api.ImmutableServiceDefinition;
import io.digiexpress.client.api.ImmutableServiceDescriptor;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.digiexpress.client.tests.migration.DialobMigration.FormsAndRevs;
import io.digiexpress.client.tests.migration.HdesMigration.HdesState;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@lombok.Builder
public class WorkflowMigration {
  @lombok.Builder.Default
  private final String src = MigrationsDefaults.folder + "workflows";
  @lombok.Builder.Default
  private final ObjectMapper om = MigrationsDefaults.om;
  
  @Jacksonized
  @lombok.Data @lombok.Builder
  public static class Workflow {
    private String id;
    private String name;
    private String formId;
    private String flowName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime updated;
  }
  
  public ServiceDefinition execute(HdesState hdes, FormsAndRevs dialob) {
    final var dir = new File(src);
    ServiceAssert.isTrue(dir.isDirectory() && dir.canRead() && dir.exists(), () -> src + " must be a directory with *workflows.json-s");
    final var files = dir.listFiles((file, name) -> name.endsWith(".json"));
    
    final var summary_workflows = MigrationsDefaults.summary("file name", "name", "form id", "flow name", "status", "type");
    final var workflows = new ArrayList<Workflow>();
    for(final var file : files) {
      try {
        for(final var wk : om.readValue(file, Workflow[].class)) {
          workflows.add(wk);
          summary_workflows.addRow(file.getName(), wk.getName(), wk.getFormId(), wk.getFlowName(), "OK", "process");
        }
      } catch (Exception e) {
        summary_workflows.addRow(file.getName(), null, "FAIL", "workflows");
        throw new RuntimeException("Failed to read workflows form json: " + file.getName() + e.getMessage(), e);
      }
    }

    log.info("Reading workflows from: '" + src + "', found: " + files.length + summary_workflows.toString());

    final var summary_service = MigrationsDefaults.summary("workflow name", "form id", "flow id", "status");
    final List<ServiceDescriptor> processes = new ArrayList<>();
    for(final var workflow : workflows) {
      try {
        final var processValue = ImmutableServiceDescriptor.builder()
          .id(workflow.getId())
          .name(workflow.getName())
          .desc("")
          .flowId(hdes.getProgramEnvir().getFlowsByName().get(workflow.getFlowName()).getId())
          .formId(workflow.getFormId())
          .build();
        processes.add(processValue);
        summary_service.addRow(processValue.getName(), processValue.getFormId(), processValue.getFlowId(), "OK");
      } catch(Exception e) {
        final var flowId = hdes.getProgramEnvir().getFlowsByName().containsKey(workflow.getFlowName()) ? "FOUND" : "MISSING";
        final var formId = dialob.getForms().stream().filter(f -> f.getId().equals(workflow.getFormId())).findAny().isPresent() ? "FOUND" : "MISSING";
        summary_service.addRow(workflow.getName(), formId, flowId, "FAIL");        
      }
    }
    
    log.info("Creating services from: '" + src + "', found workflows: " + workflows.size() + summary_service.toString());

    
    return ImmutableServiceDefinition.builder()
        .projectId("")
        .created(LocalDateTime.now())
        .updated(LocalDateTime.now())
        .descriptors(processes)
        .build();
  }
  
  
}
