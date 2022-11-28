package io.digiexpress.client.tests.migration;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.client.spi.support.ServiceAssert;
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
  
  public List<Workflow> execute() {
    final var dir = new File(src);
    ServiceAssert.isTrue(dir.isDirectory() && dir.canRead() && dir.exists(), () -> src + " must be a directory with *workflows.json-s");
    final var files = dir.listFiles((file, name) -> name.endsWith(".json"));
    
    final var summary = MigrationsDefaults.summary("file name", "name", "form id", "flow name", "status", "type");
    final var workflows = new ArrayList<Workflow>();
    for(final var file : files) {
      try {
        for(final var wk : om.readValue(file, Workflow[].class)) {
          workflows.add(wk);
          summary.addRow(file.getName(), wk.getName(), wk.getFormId(), wk.getFlowName(), "OK", "process");
        }
      } catch (Exception e) {
        summary.addRow(file.getName(), null, "FAIL", "workflows");
        throw new RuntimeException("Failed to read workflows form json: " + file.getName() + e.getMessage(), e);
      }
    }
    
  
    log.info("Reading workflows from: '" + src + "', found: " + files.length + summary.toString());

    return workflows;
  }
  
  
}
