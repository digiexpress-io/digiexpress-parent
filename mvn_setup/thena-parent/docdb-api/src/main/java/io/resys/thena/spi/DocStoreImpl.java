package io.resys.thena.spi;

import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.thena.api.ThenaClient;
import io.resys.thena.api.actions.TenantActions.CommitStatus;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.storesql.DbStateSqlImpl;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.PoolOptions;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;



@RequiredArgsConstructor
public class DocStoreImpl<T extends DocStore<T>> implements DocStore<T> {
  protected final ThenaDocConfig config;
  protected final DocStoreFactory<T> factory;
  protected final StructureType defaultRepoType = getRepoType();
  
  
  protected StructureType getRepoType() {
    return null;
  }
  
  
  @Override
  public T withTenantId(String repoId) {
    final var next = ImmutableThenaDocConfig.builder().from(config).repoId(repoId).build();
    return factory.getInstance(next, factory);
  }
  public T withRepo(String repoName, String headName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    final var next = ImmutableThenaDocConfig.builder().from(config).repoId(repoName).headId(headName).build();
    return factory.getInstance(next, factory);
  }
  @Override
  public Uni<Tenant> getTenant() {
    final var client = config.getClient();
    return client.tenants().find().id(config.getRepoId()).get();
  }

  
  @Override public ThenaDocConfig getConfig() { return config; }
  @Override public StoreTenantQuery<T> query() {
    return new StoreTenantQuery<T>() {
      protected StructureType repoType = defaultRepoType;
      protected String repoName, headName, externalId;
      @Override public StoreTenantQuery<T> externalId(String externalId) { this.externalId = externalId; return this; }
      @Override public StoreTenantQuery<T> repoType(StructureType repoType) { this.repoType = repoType; return this; }
      @Override public StoreTenantQuery<T> repoName(String repoName) { this.repoName = repoName; return this; }
      @Override public StoreTenantQuery<T> headName(String headName) { this.headName = headName; return this; }
      @Override public Uni<T> create() { return createRepo(repoName, headName, externalId, repoType); }
      @Override public T build() { return withRepo(repoName, headName); }
      @Override public Uni<T> createIfNot() { return createRepoOrGetRepo(repoName, headName, externalId, repoType); }
      @Override public Uni<T> delete() { return deleteRepo(repoName, headName); }
      @Override public Uni<Void> deleteAll() { return deleteRepos(); }
    };
  }
  
  protected Uni<T> createRepoOrGetRepo(String repoName, String headName, String externalId, StructureType type) {
    final var client = config.getClient();
    
    return client.tenants().find().id(repoName).get()
        .onItem().transformToUni(repo -> {        
          if(repo == null) {
            return createRepo(repoName, headName, externalId, type); 
          }
          return Uni.createFrom().item(withRepo(repoName, headName));
    });
  }
  
  protected Uni<Void> deleteRepos() {
    final var client = config.getClient();    
    return client.tenants().delete().onItem().transformToUni((junk) -> Uni.createFrom().voidItem());
  }
  
  protected Uni<T> deleteRepo(String repoName, String headName) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    final var client = config.getClient();
    final var existingRepo = client.git(repoName).tenants().get();
    
    
    return existingRepo.onItem().transformToUni((repoResult) -> {
      if(repoResult.getStatus() != QueryEnvelopeStatus.OK) {
        throw new DocStoreException("DOC_REPO_GET_FOR_DELETE_FAIL", 
            ImmutableDocumentExceptionMsg.builder()
            .id(repoResult.getStatus().toString())
            .value(repoName)
            .addAllArgs(repoResult.getMessages().stream().map(message-> message.getText()).collect(Collectors.toList()))
            .build()); 
      }
      
      final var repoId = repoResult.getRepo().getId();
      final var rev = repoResult.getRepo().getRev();
      final var docStore = withRepo(repoName, headName);
      
      return client.tenants().find().id(repoId).rev(rev).delete()
          .onItem().transform(junk -> docStore);
    });
  }
    
  protected Uni<T> createRepo(String repoName, String headName, String externalId, StructureType type) {
    RepoAssert.notNull(repoName, () -> "repoName must be defined!");
    RepoAssert.notNull(type, () -> "type must be defined!");
    
    final var client = config.getClient();
    final var newRepo = client.tenants().commit().name(repoName, type).externalId(externalId).build();
    return newRepo.onItem().transform((repoResult) -> {
      if(repoResult.getStatus() != CommitStatus.OK) {
        throw new DocStoreException("DOC_REPO_CREATE_FAIL", 
            ImmutableDocumentExceptionMsg.builder()
            .id(repoResult.getStatus().toString())
            .value(repoName)
            .addAllArgs(repoResult.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
            .build()); 
      }
      
      return withRepo(repoName, headName);
    });
  }
  
  public static <T extends DocStore<T>> Builder<T> builder(DocStoreFactory<T> factory) {
    return new Builder<T>(factory);
  }
  
  @FunctionalInterface
  public interface DocStoreFactory<T extends DocStore<T>> {
    T getInstance(ThenaDocConfig config, DocStoreFactory<T> factory);
  }
  
  @Slf4j
  @Accessors(fluent = true, chain = true)
  @Data
  @Getter(AccessLevel.NONE)
  public static class Builder<T extends DocStore<T>> {
    protected final DocStoreFactory<T> factory;
    protected String repoName;
    protected ObjectMapper objectMapper;
    protected ThenaDocConfig.AuthorProvider authorProvider;
    
    protected io.vertx.mutiny.pgclient.PgPool pgPool;
    protected String pgHost;
    protected String pgDb;
    protected Integer pgPort;
    protected String pgUser;
    protected String pgPass;
    protected Integer pgPoolSize;
    public Builder(DocStoreFactory<T> factory) {
      super();
      this.factory = factory;
    }
    
    protected ThenaDocConfig.AuthorProvider getAuthorProvider() {
      return this.authorProvider == null ? ()-> "not-configured" : this.authorProvider;
    } 
    
    public T build() {
      RepoAssert.notNull(repoName, () -> "repoName must be defined!");
      if(log.isDebugEnabled()) {
        log.debug("""
          Configuring Thena:
            repoName: {}
            objectMapper: {}
            authorProvider: {}
            pgPool: {}
            pgPoolSize: {}
            pgHost: {}
            pgPort: {}
            pgDb: {}
            pgUser: {}
            pgPass: {}
          """,
          this.repoName,
          this.objectMapper == null ? "configuring" : "provided",
          this.authorProvider == null ? "configuring" : "provided",
          this.pgPool == null ? "configuring" : "provided",
          this.pgPoolSize,
          this.pgHost,
          this.pgPort,
          this.pgDb,
          this.pgUser == null ? "null" : "***",
          this.pgPass == null ? "null" : "***");
      }
      
      final ThenaClient thena;
      if(pgPool == null) {
        RepoAssert.notNull(pgHost, () -> "pgHost must be defined!");
        RepoAssert.notNull(pgPort, () -> "pgPort must be defined!");
        RepoAssert.notNull(pgDb, () -> "pgDb must be defined!");
        RepoAssert.notNull(pgUser, () -> "pgUser must be defined!");
        RepoAssert.notNull(pgPass, () -> "pgPass must be defined!");
        RepoAssert.notNull(pgPoolSize, () -> "pgPoolSize must be defined!");
        
        final PgConnectOptions connectOptions = new PgConnectOptions()
            .setHost(pgHost)
            .setPort(pgPort)
            .setDatabase(pgDb)
            .setUser(pgUser)
            .setPassword(pgPass);
        final PoolOptions poolOptions = new PoolOptions()
            .setMaxSize(pgPoolSize);
        
        final io.vertx.mutiny.pgclient.PgPool pgPool = io.vertx.mutiny.pgclient.PgPool.pool(connectOptions, poolOptions);
        
        thena = DbStateSqlImpl.create().client(pgPool).db(repoName).build();
      } else {
        thena = DbStateSqlImpl.create().client(pgPool).db(repoName).build();
      }
      
      final ThenaDocConfig config = ImmutableThenaDocConfig.builder()
          .client(thena).repoId(repoName)
          .author(getAuthorProvider())
          .build();
      return factory.getInstance(config, factory);
    }
  }

}
