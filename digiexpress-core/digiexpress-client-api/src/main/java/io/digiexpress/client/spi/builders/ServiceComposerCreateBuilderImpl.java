package io.digiexpress.client.spi.builders;

import io.digiexpress.client.api.ImmutableCreateStoreEntity;
import io.digiexpress.client.api.ImmutableProcessValue;
import io.digiexpress.client.api.ImmutableServiceDefinitionDocument;
import io.digiexpress.client.api.ImmutableUpdateStoreEntity;
import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceComposer;
import io.digiexpress.client.api.ServiceComposer.CreateProcess;
import io.digiexpress.client.api.ServiceComposer.CreateRelease;
import io.digiexpress.client.api.ServiceComposer.CreateServiceRevision;
import io.digiexpress.client.api.ServiceDocument.DocumentType;
import io.digiexpress.client.api.ServiceDocument.ServiceDefinitionDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.digiexpress.client.api.ServiceDocument.ServiceRevisionDocument;
import io.digiexpress.client.spi.builders.visitors.CreateReleaseVisitor;
import io.digiexpress.client.spi.builders.visitors.CreateRevisionVisitor;
import io.digiexpress.client.spi.query.QueryFactoryImpl;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServiceComposerCreateBuilderImpl implements ServiceComposer.CreateBuilder {
  private final ServiceClient client;

  @Override
  public Uni<ServiceDefinitionDocument> process(CreateProcess process) {

    final var query = client.getQuery();
    
    return query.getServiceRevision(process.getServiceRevisionId())
    .onItem().transformToUni(rev -> {
        
        ServiceAssert.isTrue(
            process.getServiceRevisionVersionId().equals(rev.getVersion()), 
            () -> "ServiceRevisionDocument.version is not matching, expecting = '" + rev.getVersion() + "' but was: '" + process.getServiceRevisionVersionId() + "'!");

        return Uni.combine().all()
        .unis(query.getServiceDef(rev.getHeadDefId()), query.getForm(process.getFormId()))
        .asTuple().onItem().transformToUni(tuple -> {
          final var flowTag = tuple.getItem1().getHdes().getTagName();
          return query.getFlow(flowTag, process.getFlowId())
              .onItem().transformToUni(flow -> {
                
                final var newDef = ImmutableServiceDefinitionDocument.builder()
                    .from(tuple.getItem1())
                    .addProcesses(ImmutableProcessValue.builder()
                        .id(client.getConfig().getStore().getGid().getNextId(DocumentType.SERVICE_DEF))
                        .flowId(flow.getId())
                        .formId(tuple.getItem2().getId())
                        .name(process.getName())
                        .desc(process.getDesc())
                        .build())
                    .build(); 
                
                return client.getConfig().getStore()
                    .update(ImmutableUpdateStoreEntity.builder()
                        .id(newDef.getId())
                        .version(newDef.getVersion())
                        .body(client.getConfig().getMapper().toBody(newDef))
                        .bodyType(newDef.getType())
                        .build())
                    .onItem().transform((_resp) -> newDef);
              });
        });
    });
  }

  @Override
  public Uni<ServiceRevisionDocument> revision(CreateServiceRevision init) {
    final var query = QueryFactoryImpl.from(client.getConfig());
    return query.getRepos()
      .onItem().transformToUni(repos -> query.getConfigDoc()
      .onItem().transformToUni(configDoc -> {
        final var start = CreateRevisionVisitor.builder().client(client).init(init);
        final var toBeSaved = start.repos(repos).config(configDoc).build().visit();
        
        return client.getConfig().getStore()
          .batch(toBeSaved.getBatch())
          .onItem().transform((_resp) -> toBeSaved.getRevision());
      }));
  }

  @Override
  public Uni<ServiceReleaseDocument> release(CreateRelease init) {
    final var query = QueryFactoryImpl.from(client.getConfig());
    return query.getServiceDef(init.getServiceDefinitionId())
      .onItem().transformToUni(def ->  
          new CreateReleaseVisitor(client.getConfig(), query, init.getTargetDate())
          .visit(def, init.getName(), init.getActiveFrom()))
      .onItem().transformToUni((release) -> client.getConfig().getStore()
          .create(ImmutableCreateStoreEntity.builder()
              .id(release.getId())
              .version(release.getVersion())
              .body(client.getConfig().getMapper().toBody(release))
              .bodyType(DocumentType.SERVICE_RELEASE)
              .build())
          .onItem().transform((_resp) -> release));
  }
}
