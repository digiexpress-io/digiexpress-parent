package io.digiexpress.client.spi;

import java.util.Optional;

import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.ClientEntity.ClientEntityType;
import io.digiexpress.client.api.ClientEntity.Project;
import io.digiexpress.client.api.ClientEntity.ServiceDefinition;
import io.digiexpress.client.api.ClientEntity.ServiceRelease;
import io.digiexpress.client.api.Composer;
import io.digiexpress.client.api.ComposerCache;
import io.digiexpress.client.api.ComposerEntity.CreateDescriptor;
import io.digiexpress.client.api.ComposerEntity.CreateMigration;
import io.digiexpress.client.api.ComposerEntity.CreateProjectRevision;
import io.digiexpress.client.api.ComposerEntity.CreateRelease;
import io.digiexpress.client.api.ComposerEntity.MigrationState;
import io.digiexpress.client.api.ImmutableCreateStoreEntity;
import io.digiexpress.client.api.ImmutableServiceDefinition;
import io.digiexpress.client.api.ImmutableServiceDescriptor;
import io.digiexpress.client.api.ImmutableUpdateStoreEntity;
import io.digiexpress.client.spi.composer.CreateMigrationVisitor;
import io.digiexpress.client.spi.composer.CreateReleaseVisitor;
import io.digiexpress.client.spi.composer.CreateRevisionVisitor;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ComposerBuilderImpl implements Composer.ComposerBuilder {
  private final Client client;
  private final ComposerCache cache;

  @Override
  public Uni<ServiceDefinition> serviceDescriptor(CreateDescriptor process) {
    final var query = client.getQuery();
    
    final var getDef = query.getProjectServiceDef(process.getDefId());
    final var getForm = query.getDialobForm(process.getFormId());
    
    return Uni.combine().all().unis(getDef, getForm).asTuple()
    .onItem().transformToUni(tuple -> {
      final var def = tuple.getItem1();
      ServiceAssert.isTrue(
          process.getDefVersionId().equals(def.getVersion()), 
          () -> "ServiceDefinition.version is not matching, expecting = '" + def.getVersion() + "' but was: '" + process.getDefVersionId() + "'!");

      final var flowTag = def.getHdes().getTagName();
      return query.getHdesFlow(flowTag, process.getFlowId())
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
    final var query = ClientQueryImpl.from(client.getConfig());
    final var projectId = Optional.ofNullable(init.getProjectId()).orElse(query.getProjectDefaultId());
    
    return query.getRepos()
      .onItem().transformToUni(repos -> query.getProject(projectId)
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
    final var query = ClientQueryImpl.from(client.getConfig());
    return query.getProjectServiceDef(init.getServiceDefinitionId())
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
    cache.flush();
    return new CreateMigrationVisitor(mig, client).visit();
  }
}
