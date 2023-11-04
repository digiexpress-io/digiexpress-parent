package io.digiexpress.client.spi;


import static io.digiexpress.client.spi.ClientQueryImpl.FIXED_ID;
import static io.digiexpress.client.spi.support.MainBranch.HEAD_NAME;

import java.time.LocalDateTime;
import java.util.List;

import io.dialob.client.api.DialobClient;
import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.Client.ClientConfig;
import io.digiexpress.client.api.Client.TenantBuilder;
import io.digiexpress.client.api.ClientEntity;
import io.digiexpress.client.api.ClientEntity.Project;
import io.digiexpress.client.api.ClientStore;
import io.digiexpress.client.api.ImmutableClientConfig;
import io.digiexpress.client.api.ImmutableCreateStoreEntity;
import io.digiexpress.client.api.ImmutableProject;
import io.digiexpress.client.api.ImmutableProjectConfig;
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
import lombok.experimental.Accessors;;

@Data
@Getter(AccessLevel.NONE)
@Accessors(fluent = true, chain = true)
public class TenantBuilderImpl implements TenantBuilder {
  protected final ClientConfig config;  
  protected final DocDB docDb;
  protected Namings namings;
  
  public TenantBuilderImpl(ClientConfig config, DocDB docDb) {
    this.config = config;
    this.docDb = docDb;
    this.namings = Namings.builder().repoProject(config.getStore().getRepoName()).build();
  }
  
  @Override
  public Uni<Client> load() {
    ServiceAssert.notNull(namings.repoProject(), () -> "repoProject: string must be defined!");
    ServiceAssert.isNull(namings.repoStencil(), () -> "repoStencil: string must be undefined!");
    ServiceAssert.isNull(namings.repoDialob(), () -> "repoDialob: string must be undefined!");
    ServiceAssert.isNull(namings.repoHdes(), () -> "repoHdes: string must be undefined!");    

    return docDb.repo().projectsQuery().findAll().collect().asList()
    .onItem().transformToUni(repos -> {
      
      final var existingServiceRepo = repos.stream().filter(e -> e.getName().equals(namings.repoProject())).findFirst();
      final var newStore = config.getStore().repo().repoName(existingServiceRepo.get().getName()).headName(HEAD_NAME).build();

      final Uni<Project> doc = newStore.query().get().onItem().transform(state -> {
        final var result = state.getProjects().values().stream().filter(p -> p.getId().equals(FIXED_ID)).findFirst().map(entity -> config.getParser().toProject(entity));
        ServiceAssert.isTrue(result.isPresent(), () -> "repoProject: string does not exists by name: '" + namings.repoProject() + "'!");
        return result.get();
      });
  
      
      final var newCache = config.getCache().withName(newStore.getRepoName());
      final var newConfig = ImmutableClientConfig.builder().from(config).cache(newCache).store(newStore);
      return doc.onItem().transform(body -> new ClientImpl(newConfig
          .stencil(config.getStencil().repo().repoName(body.getConfig().getStencil()).headName(HEAD_NAME).build())
          .dialob(config.getDialob().repo().repoName(body.getConfig().getDialob()).headName(HEAD_NAME).build())
          .hdes(config.getHdes().repo().repoName(body.getConfig().getHdes()).headName(HEAD_NAME).build())
          .build()));
    });
  }

  @Override
  public Uni<Client> create() {
    final var namings = this.namings.withDefaults();
    return docDb.repo().projectsQuery().findAll().collect().asList()
    .onItem().transformToUni(repos -> {
      final var doc = getOrCreateDoc(repos, namings);
      return doc.onItem().transformToUni((_doc) -> create(repos, namings));
    });
  }
  
  @Override
  public Client build() {
    final var namings = this.namings.withDefaults();
    
    final var newStore = config.getStore().repo().repoName(namings.repoProject()).headName(HEAD_NAME).build();
    final var newCache = config.getCache().withName(namings.repoProject());
    final var newConfig = ImmutableClientConfig.builder()
        .from(config)
        .stencil(config.getStencil().repo().repoName(namings.repoStencil()).headName(HEAD_NAME).build())
        .dialob(config.getDialob().repo().repoName(namings.repoDialob()).headName(HEAD_NAME).build())
        .hdes(config.getHdes().repo().repoName(namings.repoHdes()).headName(HEAD_NAME).build())
        .cache(newCache)
        .store(newStore)
        .build();
    return new ClientImpl(newConfig);
  }

  protected Uni<Project> getOrCreateDoc(final List<Repo> repos, final Namings namings) {
    final var existingServiceRepo = repos.stream().filter(e -> e.getName().equals(this.namings.repoProject())).findFirst();
    
    if(existingServiceRepo.isEmpty()) {
      return config.getStore().repo().repoName(namings.repoProject()).headName(HEAD_NAME).create()
          .onItem().transformToUni(newStore -> createServiceConfig(newStore, namings)); 
    }
    
    final var serviceStore = config.getStore().repo().repoName(existingServiceRepo.get().getName()).headName(HEAD_NAME).build();
    return serviceStore.query().get()
        .onItem().transformToUni(state -> state.getProjects().values().stream().filter(p -> p.getId().equals(FIXED_ID)).findFirst()
          .map(entity -> Uni.createFrom().item(config.getParser().toProject(entity)))
          .orElseGet(() -> createServiceConfig(serviceStore, namings)))
          .onItem().transform(prj -> {
            final var newConfig = prj.getConfig();
            
            ServiceAssert.isTrue(
                this.namings.repoStencil() == null || newConfig.getStencil().equals(this.namings.repoStencil()), 
                () -> "Incorrect user configuration.repoStencil expected = '" + newConfig.getStencil() + "', actual = '" + this.namings.repoStencil() + "'");

            ServiceAssert.isTrue(
                this.namings.repoDialob() == null || newConfig.getDialob().equals(this.namings.repoDialob()), 
                () -> "Incorrect user configuration.repoDialob expected = '" + newConfig.getDialob() + "', actual = '" + this.namings.repoDialob() + "'");
            
            ServiceAssert.isTrue(
                this.namings.repoHdes() == null || newConfig.getHdes().equals(this.namings.repoHdes()), 
                () -> "Incorrect user configuration.repoHdes expected = '" + newConfig.getHdes() + "', actual = '" + this.namings.repoHdes() + "'");
            
            ServiceAssert.isTrue(
                this.namings.repoProject() == null || newConfig.getProject().equals(this.namings.repoProject()), 
                () -> "Incorrect user configuration.repoProject expected = '" + newConfig.getProject() + "', actual = '" + this.namings.repoProject() + "'");
            
            return prj;
          });
  }
  
  
  protected Uni<Client> create(final List<Repo> repos, final Namings namings) {
    final var serviceStore = config.getStore().repo().repoName(namings.repoProject()).headName(HEAD_NAME).build();
    return Uni.createFrom().item(ImmutableClientConfig.builder()
          .from(config)
          .cache(config.getCache().withName(namings.repoProject()))
          .store(serviceStore))
        // Stencil config
        .onItem().transformToUni(builder -> {
          final var existingStencilRepo = repos.stream().filter(e -> e.getName().equals(namings.repoStencil())).findFirst();
          if(existingStencilRepo.isPresent()) {
            final var stencil = config.getStencil().repo().repoName(namings.repoStencil()).headName(HEAD_NAME).build();
            return Uni.createFrom().item(builder.stencil(stencil));
          }
          return config.getStencil().repo().repoName(namings.repoStencil()).headName(HEAD_NAME).create()
              .onItem().transform(stencil -> builder.stencil(stencil));
        })
        
        // dialob config
        .onItem().transformToUni(builder -> {
          final var existingDialobRepo = repos.stream().filter(e -> e.getName().equals(namings.repoDialob())).findFirst();
          if(existingDialobRepo.isPresent()) {
            final var dialob = config.getDialob().repo().repoName(namings.repoDialob()).headName(HEAD_NAME).build();
            return Uni.createFrom().item(builder.dialob(dialob));
          }
          return config.getDialob().repo().repoName(namings.repoDialob()).headName(HEAD_NAME).create()
              .onItem().transform(dialob -> builder.dialob(dialob));
        })
        
        // wrench config
        .onItem().transformToUni(builder -> {
          final var existingHdesRepo = repos.stream().filter(e -> e.getName().equals(namings.repoHdes())).findFirst();
          if(existingHdesRepo.isPresent()) {
            final var hdes = config.getHdes().repo().repoName(namings.repoHdes()).headName(HEAD_NAME).build();
            return Uni.createFrom().item(builder.hdes(hdes));
          }
          return config.getHdes().repo().repoName(namings.repoHdes()).headName(HEAD_NAME).create()
              .onItem().transform(hdes -> builder.hdes(hdes));
        })
        
        .onItem().transform(builder -> new ClientImpl(builder.docDb(docDb).build()));
  }
  
  protected Uni<Project> createServiceConfig(ClientStore store, Namings namings) {
    final var body = ImmutableProject.builder()
        .id(null).version(null) // FIXED ID, one config per repo
        .name(FIXED_ID).head(HEAD_NAME)
        .created(LocalDateTime.now())
        .updated(LocalDateTime.now())
        .type(ClientEntity.ClientEntityType.PROJECT)
        .config(ImmutableProjectConfig.builder()
            .dialob(namings.repoDialob())
            .stencil(namings.repoStencil())
            .hdes(namings.repoHdes())
            .project(namings.repoProject())
            .build())
        .build();
    final var command = ImmutableCreateStoreEntity.builder()
        .id(FIXED_ID)
        .bodyType(ClientEntity.ClientEntityType.PROJECT)
        .body(config.getParser().toStore(body))
        .build();
    return store.create(command).onItem().transform((resp) -> ImmutableProject.builder()
        .from(body)
        .id(resp.getId())
        .version(resp.getVersion())
        .build());    
  }

  @Override
  public TenantBuilder repoStencil(String repoStencil) {
    this.namings = this.namings.toBuilder().repoStencil(repoStencil).build();
    return this;
  }
  @Override
  public TenantBuilder repoHdes(String repoHdes) {
    this.namings = this.namings.toBuilder().repoHdes(repoHdes).build();
    return this;
  }
  @Override
  public TenantBuilder repoDialob(String repoDialob) {
    this.namings = this.namings.toBuilder().repoDialob(repoDialob).build();
    return this;
  }
  @Override
  public TenantBuilder repoProject(String repoProject) {
    this.namings = this.namings.toBuilder().repoProject(repoProject).build();
    return this;
  }

  @Data @Builder(toBuilder = true) @Accessors(chain = true)
  public static class Namings {
    private final String repoProject;
    private final String repoStencil;
    private final String repoDialob;
    private final String repoHdes;
    
    public Namings withDefaults() {
      final var repoProject = this.repoProject == null ? "project" : this.repoProject;
      final var repoStencil = this.repoStencil == null ? this.repoProject + "-stencil" : this.repoStencil;
      final var repoDialob = this.repoDialob == null ? this.repoProject + "-dialob" : this.repoDialob;
      final var repoHdes = this.repoHdes == null ? this.repoProject + "-hdes" : this.repoHdes;
      ServiceAssert.isUnique(() -> "repo values must be unique!", repoStencil, repoHdes, repoDialob, repoProject);
      return Namings.builder()
            .repoDialob(repoDialob)
            .repoHdes(repoHdes)
            .repoProject(repoProject)
            .repoStencil(repoStencil)
          .build();
    }
  }
  
  @Data @Builder @Accessors(chain = true)
  private static class RepoTuple {
    private StencilClient stencil;
    private DialobClient dialob;
    private HdesClient hdes;
    private Client project;
  }
  
}
