package io.digiexpress.client.spi.composer;

import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.ClientEntity.ClientEntityType;
import io.digiexpress.client.api.ClientEntity.Project;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ClientEntity.ServiceRelease;
import io.digiexpress.client.api.Composer;
import io.digiexpress.client.api.ImmutableCreateStoreEntity;
import io.digiexpress.client.api.ImmutableServiceDefinition;
import io.digiexpress.client.api.ImmutableServiceDescriptor;
import io.digiexpress.client.api.ImmutableUpdateStoreEntity;
import io.digiexpress.client.api.ServiceComposerCommand.CreateMigration;
import io.digiexpress.client.api.ServiceComposerCommand.CreateProjectRevision;
import io.digiexpress.client.api.ServiceComposerCommand.CreateRelease;
import io.digiexpress.client.api.ServiceComposerCommand.CreateServiceDescriptor;
import io.digiexpress.client.api.ServiceComposerState.MigrationState;
import io.digiexpress.client.spi.composer.visitors.CreateMigrationVisitor;
import io.digiexpress.client.spi.composer.visitors.CreateReleaseVisitor;
import io.digiexpress.client.spi.composer.visitors.CreateRevisionVisitor;
import io.digiexpress.client.spi.query.QueryFactoryImpl;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ComposerBuilderImpl implements Composer.ComposerBuilder {
  private final Client client;

  @Override
  public Uni<ServiceDefinition> serviceDescriptor(CreateServiceDescriptor process) {
    final var query = client.getQuery();
    
    final var getDef = query.getServiceDef(process.getDefId());
    final var getForm = query.getForm(process.getFormId());
    
    return Uni.combine().all().unis(getDef, getForm).asTuple()
    .onItem().transformToUni(tuple -> {
      final var def = tuple.getItem1();
      ServiceAssert.isTrue(
          process.getDefVersionId().equals(def.getVersion()), 
          () -> "ServiceDefinition.version is not matching, expecting = '" + def.getVersion() + "' but was: '" + process.getDefVersionId() + "'!");

      final var flowTag = def.getHdes().getTagName();
      return query.getFlow(flowTag, process.getFlowId())
          .onItem().transformToUni(flow -> {
            
            final var newDef = ImmutableServiceDefinition.builder()
                .from(tuple.getItem1())
                .addDescriptors(ImmutableServiceDescriptor.builder()
                    .id(client.getConfig().getStore().getGid().getNextId(ClientEntityType.SERVICE_DEF))
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
                    .body(client.getConfig().getParser().toStore(newDef))
                    .bodyType(newDef.getType())
                    .build())
                .onItem().transform((_resp) -> newDef);
          });
    });

  }

  @Override
  public Uni<Project> revision(CreateProjectRevision init) {
    final var query = QueryFactoryImpl.from(client.getConfig());
    return query.getRepos()
      .onItem().transformToUni(repos -> query.getDefaultProject()
      .onItem().transformToUni(configDoc -> {
        final var start = CreateRevisionVisitor.builder().client(client).init(init);
        final var toBeSaved = start.repos(repos).config(configDoc).build().visit();
        
        return client.getConfig().getStore()
          .batch(toBeSaved.getBatch())
          .onItem().transform((_resp) -> toBeSaved.getRevision());
      }));
  }

  @Override
  public Uni<ServiceRelease> release(CreateRelease init) {
    final var query = QueryFactoryImpl.from(client.getConfig());
    return query.getServiceDef(init.getServiceDefinitionId())
      .onItem().transformToUni(def ->  
          new CreateReleaseVisitor(client.getConfig(), query, init.getTargetDate())
          .visit(def, init.getName(), init.getActiveFrom()))
      .onItem().transformToUni((release) -> client.getConfig().getStore()
          .create(ImmutableCreateStoreEntity.builder()
              .id(release.getId())
              .version(release.getVersion())
              .body(client.getConfig().getParser().toStore(release))
              .bodyType(ClientEntityType.SERVICE_RELEASE)
              .build())
          .onItem().transform((_resp) -> release));
  }

  @Override
  public Uni<MigrationState> migrate(CreateMigration mig) {
    return new CreateMigrationVisitor(mig, client).visit();
  }
}
