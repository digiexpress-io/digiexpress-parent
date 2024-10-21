package io.digiexpress.eveli.client.spi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.client.api.WorkflowCommands;
import io.digiexpress.eveli.client.spi.asserts.WorkflowAssert;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WorkflowCommandsJson implements WorkflowCommands {
  
  private WorkflowTag tag;

  private WorkflowCommandsJson(WorkflowTag tag) {
    super();
    this.tag = tag;
  }

  @Override
  public Workflow create(WorkflowInit init) {
    throw new UnsupportedOperationException("Workflow creation not supported");
  }
  @Override
  public WorkflowQuery query() {
    return new WorkflowQuery() {
      @Override
      public Optional<Workflow> get(String id) {
        return tag.getEntries().stream().filter(w->id.equals(w.getId().toString())).findFirst().map(w->(Workflow)w);
      }
      @Override
      public List<Workflow> findAll() {
        return tag.getEntries().stream().map(w->(Workflow)w).collect(Collectors.toList());
      }
      @Override
      public Optional<Workflow> getByName(String name) {
        return tag.getEntries().stream().filter(w->name.equals(w.getName())).findFirst().map(w->(Workflow)w);
      }
    };
  }
  @Override
  public void delete(String workflowId) {
    throw new UnsupportedOperationException("Workflow deletion not supported");
  }
  @Override
  public Optional<Workflow> update(String workflowId, Workflow workflow) {
    throw new UnsupportedOperationException("Workflow update not supported");
  }
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private WorkflowTag tag;
    public Builder workflows(WorkflowTag tag) {
      this.tag = tag;
      return this;
    }
    public Builder workflows(Resource workflowLocation, ObjectMapper mapper) {
      try {
        InputStream inputStream = workflowLocation.getInputStream();
        WorkflowTag tag = mapper.readValue(inputStream, WorkflowTag.class);
        this.tag = tag;
      } catch (IOException e){
        log.error("Can't read resources from location {}", workflowLocation.getFilename());
      }
      return this;
    }
    public WorkflowCommandsJson build() {
      WorkflowAssert.notNull(tag, () -> "tag must be defined!");
      return new WorkflowCommandsJson(tag); 
    }
  }

  @Override
  public WorkflowTagCommands release() {
    return new WorkflowTagCommands(){
      @Override
      public List<WorkflowTag> findAll() {
        return Collections.singletonList(tag);
      }

      @Override
      public Optional<WorkflowTag> getByName(String name) {
        return Optional.of(tag);
      }

      @Override
      public WorkflowTag createTag(AssetTagInit init) {
        throw new UnsupportedOperationException("Workflow tag creation not supported");
      }
    };
  }
}
