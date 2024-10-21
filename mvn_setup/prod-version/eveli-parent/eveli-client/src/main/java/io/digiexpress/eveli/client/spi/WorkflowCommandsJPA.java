package io.digiexpress.eveli.client.spi;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.digiexpress.eveli.client.api.ImmutableWorkflow;
import io.digiexpress.eveli.client.api.ImmutableWorkflowTag;
import io.digiexpress.eveli.client.api.WorkflowCommands;
import io.digiexpress.eveli.client.persistence.entities.WorkflowEntity;
import io.digiexpress.eveli.client.persistence.entities.WorkflowReleaseEntity;
import io.digiexpress.eveli.client.persistence.entities.WorkflowReleaseEntryEntity;
import io.digiexpress.eveli.client.persistence.repositories.WorkflowReleaseRepository;
import io.digiexpress.eveli.client.persistence.repositories.WorkflowRepository;
import io.digiexpress.eveli.client.spi.asserts.WorkflowAssert;


public class WorkflowCommandsJPA implements WorkflowCommands {
  
  private final WorkflowRepository jpa;
  private final WorkflowReleaseRepository releaseJpa;

  private WorkflowCommandsJPA(WorkflowRepository workflowJPA, WorkflowReleaseRepository releaseJpa) {
    super();
    this.jpa = workflowJPA;
    this.releaseJpa = releaseJpa;
  }

  @Override
  public Workflow create(WorkflowInit init) {
    return map(jpa.save(new WorkflowEntity()
        .setStartDate(init.getStartDate())
        .setEndDate(init.getEndDate())
        .setFlowName(init.getFlowName())
        .setFormName(init.getFormName())
        .setFormTag(init.getFormTag())
        .setName(init.getName())
        .setUpdated(ZonedDateTime.now(ZoneId.of("UTC"))))
        );
  }
  @Override
  public WorkflowQuery query() {
    return new WorkflowQuery() {
      @Override
      public Optional<Workflow> get(String id) {
        return jpa.findById(Long.parseLong(id)).map(WorkflowCommandsJPA::map);
      }
      @Override
      public List<Workflow> findAll() {
        return StreamSupport.stream(jpa.findAll().spliterator(), false)
            .map(WorkflowCommandsJPA::map)
            .collect(Collectors.toList());
      }
      @Override
      public Optional<Workflow> getByName(String name) {
        return jpa.findByName(name).map(WorkflowCommandsJPA::map);
      }
    };
  }
  @Override
  public void delete(String workflowId) {
    jpa.deleteById(Long.parseLong(workflowId));
  }
  @Override
  public Optional<Workflow> update(String workflowId, Workflow workflow) {
    WorkflowAssert.notNull(workflow, () -> "workflow src must be defiend!");
    WorkflowAssert.notEmpty(workflowId, () -> "workflowId src must be defiend!");
    final var foundEntity = jpa.findById(Long.parseLong(workflowId));
    if(foundEntity.isEmpty()) {
      return Optional.empty();
    }
    
    return Optional.of(jpa.save(foundEntity.get()
        .setEndDate(workflow.getEndDate())
        .setFlowName(workflow.getFlowName())
        .setFormName(workflow.getFormName())
        .setFormTag(workflow.getFormTag())
        .setName(workflow.getName())
        .setStartDate(workflow.getStartDate())
        .setUpdated(ZonedDateTime.now(ZoneId.of("UTC"))))
        ).map(WorkflowCommandsJPA::map);
  }
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private WorkflowRepository workflowJPA;
    private WorkflowReleaseRepository releaseJPA;
    public Builder workflowJPA(WorkflowRepository workflowJPA) {
      this.workflowJPA = workflowJPA;
      return this;
    }
    public Builder releaseJPA(WorkflowReleaseRepository releaseJPA) {
      this.releaseJPA = releaseJPA;
      return this;
    }
    public WorkflowCommandsJPA build() {
      WorkflowAssert.notNull(workflowJPA, () -> "workflowJPA must be defined!");
      WorkflowAssert.notNull(releaseJPA, () -> "releaseJPA must be defined!");
      return new WorkflowCommandsJPA(workflowJPA, releaseJPA); 
    }
  }

  @Override
  public WorkflowTagCommands release() {
    return new WorkflowTagCommands(){

      @Override
      public List<WorkflowTag> findAll() {
        return StreamSupport.stream(releaseJpa.findAll().spliterator(), false)
            .map(wr -> toTag(wr))
            .collect(Collectors.toList());
      }

      @Override
      public Optional<WorkflowTag> getByName(String name) {
        return toTag(releaseJpa.findByName(name));
      }

      private Optional<WorkflowTag> toTag(Optional<WorkflowReleaseEntity> release) {
        return release.map(rel -> toTag(rel));
      }

      @Override
      public WorkflowTag createTag(AssetTagInit init) {
        Iterable<WorkflowEntity> workflows = jpa.findAll();
        final var builder = WorkflowReleaseEntity.builder();
        builder
          .name(init.getName())
          .description(init.getDescription())
          .createdBy(init.getUser());
        WorkflowReleaseEntity release = builder.build();
        release.setEntries(new ArrayList<>());
        
        
        workflows.forEach(wf-> {
            var entry = buildReleaseEntry(release, wf);
            release.getEntries().add(entry);
        });
        return toTag(releaseJpa.save(release));
      }

      private WorkflowReleaseEntryEntity buildReleaseEntry(WorkflowReleaseEntity release, WorkflowEntity wf) {
        return WorkflowReleaseEntryEntity.builder()
        .name(wf.getName())
        .flowName(wf.getFlowName())
        .formName(wf.getFormName())
        .formTag(wf.getFormTag())
        .startDate(wf.getStartDate())
        .endDate(wf.getEndDate())
        .updated(wf.getUpdated())
        .workflowRelease(release)
        .build();
      }
      
      private WorkflowTag toTag(WorkflowReleaseEntity wf) {
        final var builder = ImmutableWorkflowTag.builder()
        .name(wf.getName())
        .description(wf.getDescription())
        .created(wf.getCreated().toLocalDateTime())
        .user(wf.getCreatedBy())
        .id(wf.getId().toString());
        List<Workflow> entries = new ArrayList<>();
        wf.getEntries().forEach(entry -> {
          entries.add(ImmutableWorkflow.builder()
          .id(entry.getId())
          .name(entry.getName())
          .updated(entry.getUpdated())
          .formName(entry.getFormName())
          .formTag(entry.getFormTag())
          .flowName(entry.getFlowName())
          .build());
        });
        builder.addAllEntries(entries);
        return builder.build();
      }
    };
  }
  
  
  public static ImmutableWorkflow map(WorkflowEntity entity) {
    return ImmutableWorkflow.builder()
        .id(entity.getId())
        .name(entity.getName())
        .formName(entity.getFormName())
        .formTag(entity.getFormTag())
        .flowName(entity.getFlowName())
        .startDate(entity.getStartDate())
        .endDate(entity.getEndDate())
        .updated(entity.getUpdated())
        .build();
  }
}
