package io.digiexpress.client.spi.builders;


import static io.digiexpress.client.spi.query.QueryFactoryImpl.FIXED_ID;
import static io.digiexpress.client.spi.query.QueryFactoryImpl.HEAD_NAME;

import java.time.LocalDateTime;
import java.util.List;

import io.dialob.client.api.DialobClient;
import io.digiexpress.client.api.ImmutableCreateStoreEntity;
import io.digiexpress.client.api.ImmutableServiceClientConfig;
import io.digiexpress.client.api.ImmutableServiceConfigDocument;
import io.digiexpress.client.api.ImmutableServiceConfigValue;
import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceClient.ServiceRepoBuilder;
import io.digiexpress.client.api.ServiceDocument;
import io.digiexpress.client.api.ServiceDocument.ConfigType;
import io.digiexpress.client.api.ServiceDocument.ServiceConfigDocument;
import io.digiexpress.client.api.ServiceStore;
import io.digiexpress.client.spi.ServiceClientImpl;
import io.digiexpress.client.spi.support.ServiceAssert;
import io.resys.hdes.client.api.HdesClient;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.models.Repo;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;;

@Data
@Getter(AccessLevel.NONE)
@Accessors(fluent = true, chain = true)
@RequiredArgsConstructor
public class ServiceRepoBuilderImpl implements ServiceRepoBuilder {
  protected final ServiceClientConfig config;  
  protected final DocDB docDb;
  protected String repoStencil;
  protected String repoHdes;
  protected String repoDialob;
  protected String repoService;  

  
  @Data @Builder @Accessors(chain = true)
  private static class Namings {
    private final String repoService;
    private final String repoStencil;
    private final String repoDialob;
    private final String repoHdes;
  }
  
  @Data @Builder @Accessors(chain = true)
  private static class RepoTuple {
    private StencilClient stencil;
    private DialobClient dialob;
    private HdesClient hdes;
    private ServiceClient service;
  }
  
  
  @Override
  public Uni<ServiceClient> load() {
    ServiceAssert.notNull(repoService, () -> "repoService: string must be defined!");
    ServiceAssert.isNull(repoStencil, () -> "repoStencil: string must be undefined!");
    ServiceAssert.isNull(repoDialob, () -> "repoDialob: string must be undefined!");
    ServiceAssert.isNull(repoHdes, () -> "repoHdes: string must be undefined!");    

    return docDb.repo().query().find().collect().asList()
    .onItem().transformToUni(repos -> {
      final var existingServiceRepo = repos.stream().filter(e -> e.getName().equals(repoService)).findFirst();
      
      final var newStore = config.getStore().repo().repoName(existingServiceRepo.get().getName()).headName(HEAD_NAME).build();
      final Uni<ServiceConfigDocument> doc = newStore.query().get()
          .onItem().transform(state -> {
            final var result = state.getConfigs().values().stream().findFirst().map(entity -> config.getMapper().toConfig(entity));
          
            ServiceAssert.isTrue(result.isPresent(), () -> "repoService: string does not exists by name: '" + repoService + "'!");
            return result.get();
          });
      final var newCache = config.getCache().withName(newStore.getRepoName());
      final var newConfig = ImmutableServiceClientConfig.builder().from(config).cache(newCache).store(newStore);
      return doc.onItem().transform(body -> new ServiceClientImpl(newConfig
          .stencil(config.getStencil().repo().repoName(body.getStencil().getId()).headName(HEAD_NAME).build())
          .dialob(config.getDialob().repo().repoName(body.getDialob().getId()).headName(HEAD_NAME).build())
          .hdes(config.getHdes().repo().repoName(body.getHdes().getId()).headName(HEAD_NAME).build())
          .build()));
    });
  }

  @Override
  public Uni<ServiceClient> create() {
    final var namings = buildNamings();
    
    return docDb.repo().query().find().collect().asList()
    .onItem().transformToUni(repos -> {
      final var doc = getOrCreateDoc(repos, namings);
      return doc.onItem().transformToUni((_doc) -> create(repos, namings));
    });
  }
  
  @Override
  public ServiceClient build() {
    final var namings = buildNamings();
    
    final var newStore = config.getStore().repo().repoName(namings.getRepoService()).headName(HEAD_NAME).build();
    final var newCache = config.getCache().withName(namings.getRepoService());
    final var newConfig = ImmutableServiceClientConfig.builder()
        .from(config)
        .stencil(config.getStencil().repo().repoName(namings.getRepoStencil()).headName(HEAD_NAME).build())
        .dialob(config.getDialob().repo().repoName(namings.getRepoDialob()).headName(HEAD_NAME).build())
        .hdes(config.getHdes().repo().repoName(namings.getRepoHdes()).headName(HEAD_NAME).build())
        .cache(newCache)
        .store(newStore)
        .build();
    return new ServiceClientImpl(newConfig);
  }
  
  protected Namings buildNamings() {
    final var repoService = this.repoService == null ? "service" : this.repoService;
    final var repoStencil = this.repoStencil == null ? this.repoService + "-stencil" : this.repoStencil;
    final var repoDialob = this.repoDialob == null ? this.repoService + "-dialob" : this.repoDialob;
    final var repoHdes = this.repoHdes == null ? this.repoService + "-hdes" : this.repoHdes;
    ServiceAssert.isUnique(() -> "repo values must be unique!", repoStencil, repoHdes, repoDialob, repoService);

    return Namings.builder()
          .repoDialob(repoDialob)
          .repoHdes(repoHdes)
          .repoService(repoService)
          .repoStencil(repoStencil)
        .build();
  }
  

  protected Uni<ServiceConfigDocument> getOrCreateDoc(final List<Repo> repos, final Namings namings) {
    final var existingServiceRepo = repos.stream().filter(e -> e.getName().equals(repoService)).findFirst();
    
    if(existingServiceRepo.isEmpty()) {
      return config.getStore().repo().repoName(namings.getRepoService()).headName(HEAD_NAME).create()
          .onItem().transformToUni(newStore -> createServiceConfig(newStore, namings)); 
    }
    
    final var serviceStore = config.getStore().repo().repoName(existingServiceRepo.get().getName()).headName(HEAD_NAME).build();
    return serviceStore.query().get()
        .onItem().transformToUni(state -> state.getConfigs().values().stream().findFirst()
          .map(entity -> Uni.createFrom().item(config.getMapper().toConfig(entity)))
          .orElseGet(() -> createServiceConfig(serviceStore, namings)))
          .onItem().transform(newConfig -> {
            
            ServiceAssert.isTrue(
                repoStencil == null || newConfig.getStencil().getId().equals(repoStencil), 
                () -> "Incorrect user configuration.repoStencil expected = '" + newConfig.getStencil().getId() + "', actual = '" + repoStencil + "'");

            ServiceAssert.isTrue(
                repoStencil == null || newConfig.getDialob().getId().equals(repoDialob), 
                () -> "Incorrect user configuration.repoDialob expected = '" + newConfig.getDialob().getId() + "', actual = '" + repoDialob + "'");
            
            ServiceAssert.isTrue(
                repoStencil == null || newConfig.getHdes().getId().equals(repoHdes), 
                () -> "Incorrect user configuration.repoHdes expected = '" + newConfig.getHdes().getId() + "', actual = '" + repoHdes + "'");
            
            ServiceAssert.isTrue(
                repoService == null || newConfig.getService().getId().equals(repoService), 
                () -> "Incorrect user configuration.repoService expected = '" + newConfig.getService().getId() + "', actual = '" + repoService + "'");
            
            return newConfig;
          });
  }
  
  
  protected Uni<ServiceClient> create(final List<Repo> repos, final Namings namings) {
    final var serviceStore = config.getStore().repo().repoName(namings.getRepoService()).headName(HEAD_NAME).build();
    return Uni.createFrom().item(ImmutableServiceClientConfig.builder()
          .cache(config.getCache().withName(namings.getRepoService()))
          .mapper(config.getMapper())
          .store(serviceStore))
        // Stencil config
        .onItem().transformToUni(builder -> {
          final var existingStencilRepo = repos.stream().filter(e -> e.getName().equals(namings.getRepoStencil())).findFirst();
          if(existingStencilRepo.isPresent()) {
            final var stencil = config.getStencil().repo().repoName(namings.getRepoStencil()).headName(HEAD_NAME).build();
            return Uni.createFrom().item(builder.stencil(stencil));
          }
          return config.getStencil().repo().repoName(namings.getRepoStencil()).headName(HEAD_NAME).create()
              .onItem().transform(stencil -> builder.stencil(stencil));
        })
        
        // dialob config
        .onItem().transformToUni(builder -> {
          final var existingDialobRepo = repos.stream().filter(e -> e.getName().equals(namings.getRepoDialob())).findFirst();
          if(existingDialobRepo.isPresent()) {
            final var dialob = config.getDialob().repo().repoName(namings.getRepoDialob()).headName(HEAD_NAME).build();
            return Uni.createFrom().item(builder.dialob(dialob));
          }
          return config.getDialob().repo().repoName(namings.getRepoDialob()).headName(HEAD_NAME).create()
              .onItem().transform(dialob -> builder.dialob(dialob));
        })
        
        // wrench config
        .onItem().transformToUni(builder -> {
          final var existingHdesRepo = repos.stream().filter(e -> e.getName().equals(namings.getRepoHdes())).findFirst();
          if(existingHdesRepo.isPresent()) {
            final var hdes = config.getHdes().repo().repoName(namings.getRepoHdes()).headName(HEAD_NAME).build();
            return Uni.createFrom().item(builder.hdes(hdes));
          }
          return config.getHdes().repo().repoName(namings.getRepoHdes()).headName(HEAD_NAME).create()
              .onItem().transform(hdes -> builder.hdes(hdes));
        })
        
        .onItem().transform(builder -> new ServiceClientImpl(builder.docDb(docDb).build()));
  }
  
  protected Uni<ServiceConfigDocument> createServiceConfig(ServiceStore store, Namings namings) {
    final var body = ImmutableServiceConfigDocument.builder()
        .id(null).version(null) // FIXED ID, one config per repo
        .created(LocalDateTime.now())
        .updated(LocalDateTime.now())
        .type(ServiceDocument.DocumentType.SERVICE_CONFIG)
        .dialob(ImmutableServiceConfigValue.builder().type(ConfigType.DIALOB).id(namings.getRepoDialob()).build())
        .stencil(ImmutableServiceConfigValue.builder().type(ConfigType.STENCIL).id(namings.getRepoStencil()).build())
        .hdes(ImmutableServiceConfigValue.builder().type(ConfigType.HDES).id(namings.getRepoHdes()).build())
        .service(ImmutableServiceConfigValue.builder().type(ConfigType.SERVICE).id(namings.getRepoService()).build())
        .build();
    final var command = ImmutableCreateStoreEntity.builder()
        .id(FIXED_ID)
        .bodyType(ServiceDocument.DocumentType.SERVICE_CONFIG)
        .body(config.getMapper().toBody(body))
        .build();
    return store.create(command).onItem().transform((resp) -> ImmutableServiceConfigDocument.builder()
        .from(body)
        .id(resp.getId())
        .version(resp.getVersion())
        .build());    
  }
  
}
