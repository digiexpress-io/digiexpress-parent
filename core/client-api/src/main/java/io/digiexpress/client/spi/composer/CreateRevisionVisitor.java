package io.digiexpress.client.spi.composer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.ClientEntity;
import io.digiexpress.client.api.ClientEntity.ClientEntityType;
import io.digiexpress.client.api.ClientEntity.ConfigType;
import io.digiexpress.client.api.ClientEntity.Project;
import io.digiexpress.client.api.ClientStore.ClientStoreCommand;
import io.digiexpress.client.api.ClientStore.CreateStoreEntity;
import io.digiexpress.client.api.ClientStore.UpdateStoreEntity;
import io.digiexpress.client.api.ImmutableCreateStoreEntity;
import io.digiexpress.client.api.ImmutableProject;
import io.digiexpress.client.api.ImmutableProjectRevision;
import io.digiexpress.client.api.ImmutableRefIdValue;
import io.digiexpress.client.api.ImmutableServiceDefinition;
import io.digiexpress.client.api.ImmutableUpdateStoreEntity;
import io.digiexpress.client.api.ComposerEntity.CreateProjectRevision;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.resys.thena.docdb.api.models.Repo;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;



@RequiredArgsConstructor
public class CreateRevisionVisitor {
  private final Client client;
  private final CreateProjectRevision init;
  private final List<Repo> repos;
  private final Project project;
  private final LocalDateTime now = LocalDateTime.now();
  
  @lombok.Builder @Data
  public static class Result {
    private final ClientEntity.ServiceDefinition service;
    private final ClientEntity.Project revision;
    private final List<ClientStoreCommand> batch;
  }
  
  public CreateRevisionVisitor.Result visit() {
    final var config = project.getConfig();
    ServiceAssert.isTrue(
        repos.stream().filter(repo -> repo.getName().equals(config.getStencil())).findFirst().isPresent(), 
        () -> "ServiceConfigDocument misconfiguration, can't find 'stencil' repo with name: '" + config.getStencil() +"'!");
    ServiceAssert.isTrue(
        repos.stream().filter(repo -> repo.getName().equals(config.getDialob())).findFirst().isPresent(), 
        () -> "ServiceConfigDocument misconfiguration, can't find 'dialob' repo with name: '" + config.getDialob() +"'!");    
    ServiceAssert.isTrue(
        repos.stream().filter(repo -> repo.getName().equals(config.getHdes())).findFirst().isPresent(), 
        () -> "ServiceConfigDocument misconfiguration, can't find 'hdes' repo with name: '" + config.getHdes() +"'!");
    ServiceAssert.isTrue(
        repos.stream().filter(repo -> repo.getName().equals(config.getProject())).findFirst().isPresent(), 
        () -> "ServiceConfigDocument misconfiguration, can't find 'service' repo with name: '" + config.getProject() +"'!");
    
    
    final var service = ImmutableServiceDefinition.builder()
      .id(nextId(ClientEntityType.SERVICE_DEF)).version(nextId(ClientEntityType.SERVICE_DEF)).created(now).updated(now)
      .projectId(project.getId())
      .addRefs(
          visitRef().type(ConfigType.HDES).repoId(config.getHdes()).build(),
          visitRef().type(ConfigType.STENCIL).repoId(config.getStencil()).build(),
          visitRef().type(ConfigType.PROJECT).repoId(config.getProject()).build(),
          visitRef().type(ConfigType.DIALOB).repoId(config.getDialob()).build()
          )
      .build();

    final var head = ImmutableProjectRevision.builder()
      .id(nextId(ClientEntityType.PROJECT)).created(now).updated(now)
      .revisionName(init.getName())
      .defId(service.getId())
      .build();
      
    final var rev = ImmutableProject.builder().from(project)
      .updated(now)
      .name(init.getName())
      .head(head.getId())
      .type(ClientEntityType.PROJECT)
      .addRevisions(head)
      .build();
    
    return Result.builder()
        .service(service)
        .revision(rev)
        .batch(Arrays.asList(
          toUpdateCommand(rev),
          toCreateCommand(service)
        ))
        .build();
  }
  
  protected ImmutableRefIdValue.Builder visitRef() {
    return ImmutableRefIdValue.builder().id(nextId(ClientEntityType.SERVICE_DEF)).tagName(ServiceAssert.BRANCH_MAIN);
  }

  protected UpdateStoreEntity toUpdateCommand(ClientEntity doc) {
    return ImmutableUpdateStoreEntity.builder()
        .bodyType(doc.getType())
        .id(doc.getId())
        .version(doc.getVersion())
        .body(client.getConfig().getParser().toStore(doc))
        .build();
  } 
  
  protected CreateStoreEntity toCreateCommand(ClientEntity doc) {
    return ImmutableCreateStoreEntity.builder()
        .bodyType(doc.getType())
        .id(doc.getId())
        .version(doc.getVersion())
        .body(client.getConfig().getParser().toStore(doc))
        .build();
  } 
  
  protected String nextId(ClientEntityType type) {
    return client.getConfig().getStore().getGid().getNextId(type);
  }
  
  
  public static Builder builder() {
    return new Builder();
  }
  
  @Data @Getter(AccessLevel.NONE)
  @Accessors(fluent = true, chain = true)
  public static class Builder {
    private Client client;
    private CreateProjectRevision init;
    private List<Repo> repos;
    private Project config;
    
    public CreateRevisionVisitor build() {
      ServiceAssert.notNull(client, () -> "client: Client must be defined!");
      ServiceAssert.notNull(init, () -> "init: CreateServiceRevision must be defined!");
      ServiceAssert.notNull(repos, () -> "repos: List<Repo> must be defined!");
      ServiceAssert.notNull(config, () -> "config: ServiceConfigDocument must be defined!");
      return new CreateRevisionVisitor(client, init, repos, config);
    }
  }
}
