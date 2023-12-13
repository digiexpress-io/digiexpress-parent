package io.resys.thena.tasks.dev.app.mig;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.dialob.client.api.DialobDocument.FormRevisionEntryDocument;
import io.resys.sysconfig.client.api.model.ImmutableCreateSysConfig;
import io.resys.sysconfig.client.api.model.ImmutableSysConfigService;
import io.resys.sysconfig.client.api.model.SysConfig.SysConfigService;
import io.resys.sysconfig.client.api.model.SysConfigCommand.CreateSysConfig;
import io.resys.sysconfig.client.spi.support.SysConfigAssert;
import io.resys.thena.tasks.client.spi.store.MainBranch;
import io.resys.thena.tasks.dev.app.mig.DialobMigration.FormsAndRevs;
import io.resys.thena.tasks.dev.app.mig.HdesMigration.HdesState;
import io.thestencil.client.api.StencilComposer.SiteState;
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
  
  @Jacksonized
  @lombok.Data @lombok.Builder
  public static class CommandData {
    private final CreateSysConfig command;
    private final String log;
  }
  
  public CommandData execute(HdesState hdes, FormsAndRevs dialob, SiteState stencil) {
    final var dir = new File(src);
    SysConfigAssert.isTrue(dir.isDirectory() && dir.canRead() && dir.exists(), () -> src + " must be a directory with *workflows.json-s");
    final var files = dir.listFiles((file, name) -> name.endsWith(".json"));
    
    final var summary_workflows = MigrationsDefaults.summary("file name", "name", "form name", "flow name", "status", "type");
    final var missing_workflows = MigrationsDefaults.summary("wk name", "form id", "doc", "rev");
    
    final var workflows = new ArrayList<Workflow>();
    final var stencilWks = stencil.getWorkflows().values().stream()
        .filter(v -> !Boolean.TRUE.equals(v.getBody().getDevMode()))
        .collect(Collectors.toMap(e -> e.getBody().getValue(), e -> e));
    
    for(final var file : files) {
      try {      
        for(final var wk : om.readValue(file, Workflow[].class)) {

          if(!stencilWks.containsKey(wk.getName())) {
            continue;
          }
          
          try {
            final var form = dialob.getForms().stream().filter(f -> f.getId().equals(wk.getFormId())).findFirst().get();
            final var formId = form.getName();
            workflows.add(wk);
            summary_workflows.addRow(file.getName(), wk.getName(), formId, wk.getFlowName(), "OK", "process");
          } catch (Exception e) {
            FormRevisionDocument foundDoc = null;
            FormRevisionEntryDocument foundRev = null;
            for(final var rev : dialob.getRevs()) {
              final var found = rev.getEntries().stream().filter(entry -> entry.getFormId().equals(wk.getFormId())).findFirst();
              if(found.isEmpty()) {
                continue;
              }
              foundDoc = rev;
              foundRev = found.get();
              break;
            }
            
            if(foundRev != null) {
              missing_workflows.addRow(wk.getName(), wk.getFormId(), foundDoc.getName(), foundRev.getRevisionName());
            } else {
              missing_workflows.addRow(wk.getName(), wk.getFormId(), "-", "-");              
            }

          }
        }
      } catch (Exception e) {
        summary_workflows.addRow(file.getName(), null, "FAIL", "workflows");
        throw new RuntimeException("Failed to read workflows form json: " + file.getName() + e.getMessage(), e);
      }

    }
    log.error("Reading workflows from: '" + src + "', not found: " + files.length + missing_workflows.toString());
    log.info("Reading workflows from: '" + src + "', found: " + files.length + summary_workflows.toString());
    
    final var summary_service = MigrationsDefaults.summary("workflow name", "form id", "flow id", "status");
    final List<SysConfigService> processes = new ArrayList<>();
    for(final var workflow : workflows) {
      
      
      try {
        final var descriptor = ImmutableSysConfigService.builder()
          .id(workflow.getId())
          .serviceName(workflow.getName())
          .flowName(hdes.getProgramEnvir().getFlowsByName().get(workflow.getFlowName()).getAst().get().getName())
          .formId(workflow.getFormId())
          .build();
        processes.add(descriptor);
        summary_service.addRow(descriptor.getServiceName(), descriptor.getFormId(), descriptor.getFlowName(), "OK");
      } catch(Exception e) {
        final var flowId = hdes.getProgramEnvir().getFlowsByName().containsKey(workflow.getFlowName()) ? "FOUND" : "MISSING";
        final var formId = dialob.getForms().stream().filter(f -> f.getId().equals(workflow.getFormId())).findAny().isPresent() ? "FOUND" : "MISSING";
        summary_service.addRow(workflow.getName(), formId, flowId, "FAIL");        
      }
    }
    
    log.info("Creating services from: '" + src + "', found workflows: " + workflows.size() + summary_service.toString());

    
    final var command = ImmutableCreateSysConfig.builder()
        .tenantId("migration-test-tenant-id")
        .userId("migration-test-user-id")
        .name("migration")
        .targetDate(Instant.now())
        .wrenchHead(MainBranch.HEAD_NAME)
        .stencilHead(MainBranch.HEAD_NAME)
        .services(processes)
        .build();
    
    return new CommandData(command, summary_service.toString());
  }
  
  
}
