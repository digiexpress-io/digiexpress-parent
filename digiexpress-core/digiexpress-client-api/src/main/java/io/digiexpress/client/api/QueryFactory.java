package io.digiexpress.client.api;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.client.api.DialobDocument.FormDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceRevisionDocument;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;

public interface QueryFactory {
  Uni<List<Repo>> getRepos();
  Uni<FormDocument> getForm(String formId);
  Uni<ServiceConfigDocument> getConfigDoc();
  Uni<ServiceRevisionDocument> getServiceRevision(String id);
  Uni<ServiceDefinitionDocument> getServiceDef(String id);
  Uni<FlowDocument> getFlow(String tagName, String flowName);
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableFlowDocument.class) @JsonDeserialize(as = ImmutableFlowDocument.class)
  interface FlowDocument {
    String getId();
    AstFlow getData();
  }
}
