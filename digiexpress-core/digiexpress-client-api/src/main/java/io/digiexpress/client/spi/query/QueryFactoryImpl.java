package io.digiexpress.client.spi.query;

import java.util.List;

import io.dialob.client.api.DialobDocument.FormDocument;
import io.digiexpress.client.api.ImmutableFlowDocument;
import io.digiexpress.client.api.QueryFactory;
import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceRevisionDocument;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QueryFactoryImpl implements QueryFactory {
  public static final String FIXED_ID = ServiceDocument.DocumentType.SERVICE_CONFIG.name();
  public static final String HEAD_NAME = "main";
  
  private final ServiceClientConfig config;
  
  public static QueryFactoryImpl from(ServiceClient client) {
    return new QueryFactoryImpl(client.getConfig());
  }  
  public static QueryFactoryImpl from(ServiceClientConfig config) {
    return new QueryFactoryImpl(config);
  }
  @Override
  public Uni<ServiceConfigDocument> getConfigDoc() {
    final var result = config.getStore().query().get(FIXED_ID);
    return result.onItem().transform(entityState -> config.getMapper().toConfig(entityState));
  }
  @Override
  public Uni<ServiceRevisionDocument> getServiceRevision(String id) {
    final var result = config.getStore().query().get(id);
    return result.onItem().transform(entityState -> config.getMapper().toRev(entityState));
  }
  @Override
  public Uni<List<Repo>> getRepos() {
    return config.getDocDb().repo().query().find().collect().asList();
  }
  @Override
  public Uni<FormDocument> getForm(String formId) {
    return config.getDialob().getConfig().getStore()
        .query().get(formId)
        .onItem().transform(state -> config.getDialob().getConfig().getMapper().toFormDoc(state));
  }
  @Override
  public Uni<FlowDocument> getFlow(String tagId, String flowId) {
    if(tagId.equals(HEAD_NAME)) {
      return config.getHdes().store().query().get(flowId)
      .onItem().transform(e -> (FlowDocument) ImmutableFlowDocument.builder().id(e.getId()).data(config.getHdes().ast().commands(e.getBody()).flow()).build())
      .onFailure().recoverWithUni((_error) -> 
        
        config.getHdes().store().query().get()
        .onItem().transform(state -> {
          
          final var flow = state.getFlows().values().stream()
              .filter(e -> e.getBodyType().equals(AstBodyType.FLOW))
              .map(e -> ImmutableFlowDocument.builder().data(config.getHdes().ast().commands(e.getBody()).flow()).id(e.getId()).build())
              .filter(e -> e.getData().getName().equals(flowId) || e.getId().equals(flowId))
              .findFirst();
          
          ServiceAssert.isTrue(flow.isPresent(), () -> "Can't find hdes flow by id or name: '" + flowId + "' for tag: '" + tagId + "'");
          
          return flow.get();
        })
      );
    }
    return config.getHdes().store().query().get()
    .onItem().transform(state -> {
      
      final var findById = state
        .getTags().values().stream()
        .filter(tag -> tag.getId().equals(tagId)).map(e -> config.getHdes().ast().commands(e.getBody()).tag())
        .findFirst();
      
      final var foundTag = findById.orElseGet(() -> state.getTags().values().stream()
        .map(e -> config.getHdes().ast().commands(e.getBody()).tag())
        .filter(e -> e.getName().equals(tagId))
        .findFirst().orElse(null)
      ); 
      
      ServiceAssert.notNull(foundTag, () -> "Can't find hdes tag with id or name: '" + tagId + "'");
      
      final var flow = foundTag.getValues().stream()
        .filter(e -> e.getBodyType().equals(AstBodyType.FLOW))
        .map(e -> ImmutableFlowDocument.builder().data(config.getHdes().ast().commands(e.getCommands()).flow()).id(e.getId()).build())
        .filter(e -> e.getData().getName().equals(flowId) || e.getId().equals(flowId))
        .findFirst();
      
      ServiceAssert.isTrue(flow.isPresent(), () -> "Can't find hdes flow by id or name: '" + flowId + "' for tag: '" + tagId + "'");
      return flow.get();
    });
  }
  @Override
  public Uni<ServiceDefinitionDocument> getServiceDef(String id) {
    final var result = config.getStore().query().get(id);
    return result.onItem().transform(entityState -> config.getMapper().toDef(entityState));
  }
}
