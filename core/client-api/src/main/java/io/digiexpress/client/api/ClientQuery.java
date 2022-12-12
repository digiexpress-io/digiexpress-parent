package io.digiexpress.client.api;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.digiexpress.client.api.ClientEntity.Project;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ClientStore.StoreState;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.Sites;

public interface ClientQuery {
  Uni<StoreState> head();
  Uni<List<Repo>> getRepos();
  Uni<Project> getDefaultProject();
  Uni<FormRevisionDocument> getFormRev(String formId);
  Uni<FormDocument> getForm(String formId);
  Uni<Project> getProject(String id);
  Uni<ServiceDefinition> getServiceDef(String id);
  Uni<FlowDocument> getFlow(String tagName, String flowName);
  Uni<Sites> getStencil(String tagName);
  Uni<AstTag> getHdes(String tagName);
  
  @Value.Immutable @JsonSerialize(as = ImmutableFlowDocument.class) @JsonDeserialize(as = ImmutableFlowDocument.class)
  interface FlowDocument {
    String getId();
    AstFlow getData();
  }
  
}
