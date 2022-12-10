package io.digiexpress.client.spi.query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.dialob.client.api.DialobDocument.FormDocument;
import io.dialob.client.api.DialobDocument.FormRevisionDocument;
import io.digiexpress.client.api.ImmutableFlowDocument;
import io.digiexpress.client.api.QueryFactory;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceRevisionDocument;
import io.digiexpress.client.api.ServiceStore.StoreState;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.AstTag.AstTagValue;
import io.resys.hdes.client.api.ast.ImmutableAstTag;
import io.resys.hdes.client.api.ast.ImmutableAstTagValue;
import io.resys.hdes.client.api.ast.ImmutableHeaders;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilComposer.SiteState;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QueryFactoryImpl implements QueryFactory {
  public static final String FIXED_ID = ServiceDocument.DocumentType.SERVICE_CONFIG.name();
  public static final String HEAD_NAME = ServiceAssert.BRANCH_MAIN;
  
  private final String imagePath = "/images";
  private final List<AstBodyType> HDES_ASSETS = Arrays.asList(AstBodyType.DT, AstBodyType.FLOW, AstBodyType.FLOW_TASK);
  private final ServiceClientConfig config;
  
  public static QueryFactoryImpl from(ServiceClientConfig config) {
    return new QueryFactoryImpl(config);
  }
  @Override
  public Uni<StoreState> head() {
    return config.getStore().query().get();
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
  public Uni<FormRevisionDocument> getFormRev(String formId) {
    final var mapper = config.getDialob().getConfig().getMapper();
    return config.getDialob().getConfig().getStore()
        .query().get()
        .onItem().transform(state -> {
          final Optional<FormRevisionDocument> result = state.getRevs().values().stream().map(mapper::toFormRevDoc)
          .filter(rev -> {
            if(rev.getId().equals(formId) || rev.getName().equals(formId)) {
              return true;
            }
            final var containsEntry = rev.getEntries().stream()
                .filter((e) -> 
                    e.getFormId().equals(formId) || 
                    e.getRevisionName().equals(formId) || 
                    formId.equals(e.getId())  
                ).findFirst().isPresent();
            return containsEntry;
          })
          .findFirst();
          
          ServiceAssert.isTrue(result.isPresent(), () -> "Can't find form rev by id or name: '" + formId + "'!");
          return result.get();
        });
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
  @Override
  public Uni<Sites> getStencil(String tagName) {

    return config.getStencil().getStore().query().head()
    .onItem().transformToUni((SiteState e) -> {
      if(HEAD_NAME.equals(tagName)) {        
        final var md = config.getStencil().markdown().json(e, false).build();
        final var site = config.getStencil().sites().created(System.currentTimeMillis()).imagePath(imagePath).source(md).build();
        return Uni.createFrom().item(site);
      }
      
      final var foundTag = e.getReleases().values().stream()
          .filter(r -> r.getId().equals(tagName) || r.getBody().getName().equals(tagName))
          .findFirst();
      ServiceAssert.notNull(foundTag, () -> "Can't find stencil tag with id or name: '" + tagName + "'");
      
      
      return config.getStencil().getStore().query().release(foundTag.get().getId()).onItem()
      .transform(release -> {
        final var md = config.getStencil().markdown().json(release, false).build();
        return config.getStencil().sites().created(System.currentTimeMillis()).source(md).build();
      });
    });
  
  }
  @Override
  public Uni<AstTag> getHdes(String tagName) {
    return config.getHdes().store().query().get()
    .onItem().transform(state -> {
      
      if(tagName.equals(HEAD_NAME)) {
        final List<AstTagValue> values = new ArrayList<>();
        Stream.of(
            state.getFlows().values().stream(),
            state.getDecisions().values().stream(),
            state.getServices().values().stream()
        )
        .flatMap(e -> e)
        .forEach(entity -> {
          final var tag = ImmutableAstTagValue.builder()
              .id(entity.getId())
              .hash(entity.getHash())
              .bodyType(entity.getBodyType())
              .commands(entity.getBody())
              .build(); 
          values.add(tag);
        });
        
        
        return ImmutableAstTag.builder()
            .created(LocalDateTime.now())
            .bodyType(AstBodyType.TAG)
            .name(tagName).description("snapshot")
            .values(values)
            .headers(ImmutableHeaders.builder().build())
            .build();
      }
      
      final var foundTag = state.getTags().values().stream()
        .map(e -> AstTagDoc.builder().id(e.getId()).data(config.getHdes().ast().commands(e.getBody()).tag()).build())
        .filter(e -> e.getData().getName().equals(tagName) || e.getId().equals(tagName))
        .map(e -> ImmutableAstTag.builder()
            .from(e.getData())
            .values(
                e.getData().getValues().stream()
                .filter(v -> HDES_ASSETS.contains(v.getBodyType()))
                .collect(Collectors.toList())
            )
            .build())
        .findFirst();
      ServiceAssert.notNull(foundTag, () -> "Can't find hdes tag with id or name: '" + tagName + "'");
      
      return foundTag.get();
    });
  }
  
  
  @Builder @Data
  private static class AstTagDoc {
    private final String id;
    private final AstTag data;
  }
}
