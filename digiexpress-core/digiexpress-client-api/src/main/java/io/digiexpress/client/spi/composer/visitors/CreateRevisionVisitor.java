package io.digiexpress.client.spi.composer.visitors;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import io.digiexpress.client.api.ImmutableCreateStoreEntity;
import io.digiexpress.client.api.ImmutableRefIdValue;
import io.digiexpress.client.api.ImmutableServiceDefinitionDocument;
import io.digiexpress.client.api.ImmutableServiceRevisionDocument;
import io.digiexpress.client.api.ImmutableServiceRevisionValue;
import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceComposer.CreateServiceRevision;
import io.digiexpress.client.api.ServiceDocument;
import io.digiexpress.client.api.ServiceDocument.ConfigType;
import io.digiexpress.client.api.ServiceDocument.DocumentType;
import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceStore.CreateStoreEntity;
import io.digiexpress.client.api.ServiceStore.StoreCommand;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.resys.thena.docdb.api.models.Repo;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;



@RequiredArgsConstructor
public class CreateRevisionVisitor {
  private final ServiceClient client;
  private final CreateServiceRevision init;
  private final List<Repo> repos;
  private final ServiceConfigDocument config;
  private final LocalDateTime now = LocalDateTime.now();
  
  @lombok.Builder @Data
  public static class Result {
    private final ServiceDocument.ServiceDefinitionDocument service;
    private final ServiceDocument.ServiceRevisionDocument revision;
    private final List<StoreCommand> batch;
  }
  
  public CreateRevisionVisitor.Result visit() {
    ServiceAssert.isTrue(
        repos.stream().filter(repo -> repo.getName().equals(config.getStencil().getId())).findFirst().isPresent(), 
        () -> "ServiceConfigDocument misconfiguration, can't find 'stencil' repo with name: '" + config.getStencil().getId() +"'!");
    ServiceAssert.isTrue(
        repos.stream().filter(repo -> repo.getName().equals(config.getDialob().getId())).findFirst().isPresent(), 
        () -> "ServiceConfigDocument misconfiguration, can't find 'dialob' repo with name: '" + config.getDialob().getId() +"'!");    
    ServiceAssert.isTrue(
        repos.stream().filter(repo -> repo.getName().equals(config.getHdes().getId())).findFirst().isPresent(), 
        () -> "ServiceConfigDocument misconfiguration, can't find 'hdes' repo with name: '" + config.getHdes().getId() +"'!");
    ServiceAssert.isTrue(
        repos.stream().filter(repo -> repo.getName().equals(config.getService().getId())).findFirst().isPresent(), 
        () -> "ServiceConfigDocument misconfiguration, can't find 'service' repo with name: '" + config.getService().getId() +"'!");
    
    final var service = ImmutableServiceDefinitionDocument.builder()
      .id(nextId(DocumentType.SERVICE_DEF)).version(nextId(DocumentType.SERVICE_DEF)).created(now).updated(now)
      .addRefs(
          visitRef().type(ConfigType.HDES).repoId(config.getHdes().getId()).build(),
          visitRef().type(ConfigType.STENCIL).repoId(config.getStencil().getId()).build(),
          visitRef().type(ConfigType.SERVICE).repoId(config.getService().getId()).build(),
          visitRef().type(ConfigType.DIALOB).repoId(config.getDialob().getId()).build()
          )
      .build();

    final var head = ImmutableServiceRevisionValue.builder()
      .id(nextId(DocumentType.SERVICE_REV)).created(now).updated(now)
      .revisionName(init.getName())
      .defId(service.getId())
      .build();
      
    final var rev = ImmutableServiceRevisionDocument.builder()
      .id(nextId(DocumentType.SERVICE_REV)).version(nextId(DocumentType.SERVICE_REV)).created(now).updated(now)
      .name(init.getName())
      .head(head.getId())
      .type(DocumentType.SERVICE_REV)
      .addValues(head)
      .build();
    
    return Result.builder()
        .service(service)
        .revision(rev)
        .batch(Arrays.asList(
          toStoreCommand(rev),
          toStoreCommand(service)
        ))
        .build();
  }
  
  protected ImmutableRefIdValue.Builder visitRef() {
    return ImmutableRefIdValue.builder().id(nextId(DocumentType.SERVICE_DEF)).tagName(ServiceAssert.BRANCH_MAIN);
  }
  
  protected CreateStoreEntity toStoreCommand(ServiceDocument doc) {
    return ImmutableCreateStoreEntity.builder()
        .bodyType(doc.getType())
        .id(doc.getId())
        .version(doc.getVersion())
        .body(client.getConfig().getMapper().toBody(doc))
        .build();
  } 
  
  protected String nextId(DocumentType type) {
    return client.getConfig().getStore().getGid().getNextId(type);
  }
  
  
  public static Builder builder() {
    return new Builder();
  }
  
  @Data @Getter(AccessLevel.NONE)
  @Accessors(fluent = true, chain = true)
  public static class Builder {
    private ServiceClient client;
    private CreateServiceRevision init;
    private List<Repo> repos;
    private ServiceConfigDocument config;
    
    public CreateRevisionVisitor build() {
      ServiceAssert.notNull(client, () -> "client: ServiceClient must be defined!");
      ServiceAssert.notNull(init, () -> "init: CreateServiceRevision must be defined!");
      ServiceAssert.notNull(repos, () -> "repoStencil: List<Repo> must be defined!");
      ServiceAssert.notNull(config, () -> "repoDialob: ServiceConfigDocument must be defined!");
      return new CreateRevisionVisitor(client, init, repos, config);
    }
  }
}
