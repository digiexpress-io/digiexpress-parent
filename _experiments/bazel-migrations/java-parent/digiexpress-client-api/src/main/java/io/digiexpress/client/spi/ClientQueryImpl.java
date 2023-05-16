package io.digiexpress.client.spi;

import java.util.List;

import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.digiexpress.client.api.Client.ClientConfig;
import io.digiexpress.client.api.ClientEntity.Project;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ClientQuery;
import io.digiexpress.client.api.ClientStore.StoreState;
import io.digiexpress.client.api.ProjectTags;
import io.digiexpress.client.spi.query.GetDialobFormRev;
import io.digiexpress.client.spi.query.GetHdesFlow;
import io.digiexpress.client.spi.query.GetHdesTag;
import io.digiexpress.client.spi.query.GetRefTags;
import io.digiexpress.client.spi.query.GetStencilTag;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.Sites;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClientQueryImpl implements ClientQuery {
  public static final String FIXED_ID = "SERVICE_CONFIG";
  private final ClientConfig config;
  
  public static ClientQueryImpl from(ClientConfig config) {
    return new ClientQueryImpl(config);
  }
  @Override
  public Uni<ProjectTags> getProjectTags() {
    return new GetRefTags(config).build();
  }
  @Override
  public Uni<StoreState> getProjectHead() {
    return config.getStore().query().get();
  }
  @Override
  public String getProjectDefaultId() {
    return FIXED_ID;
  }
  @Override
  public Uni<Project> getProject(String id) {
    return config.getStore().query().get(id)
        .onItem().transform(entityState -> config.getParser().toProject(entityState));
  }
  @Override
  public Uni<List<Repo>> getRepos() {
    return config.getDocDb().repo().query().find().collect().asList();
  }
  @Override
  public Uni<FormRevisionDocument> getDialobFormRev(String formId) {
    return new GetDialobFormRev(config, formId).build();
  }
  @Override
  public Uni<FormDocument> getDialobForm(String formId) {
    return config.getDialob().getConfig().getStore().query().get(formId)
        .onItem().transform(state -> config.getDialob().getConfig().getMapper().toFormDoc(state));
  }
  @Override
  public Uni<ServiceDefinition> getProjectServiceDef(String id) {
    return config.getStore().query().get(id)
        .onItem().transform(entityState -> config.getParser().toDefinition(entityState));
  }
  @Override
  public Uni<FlowDocument> getHdesFlow(String tagId, String flowId) {
    return new GetHdesFlow(config, tagId, flowId).build();
  }
  @Override
  public Uni<Sites> getStencilTag(String tagName) {
    return new GetStencilTag(config, tagName).build();
  }
  @Override
  public Uni<AstTag> getHdesTag(String tagName) {
    return new GetHdesTag(config, tagName).build();
  }
}
