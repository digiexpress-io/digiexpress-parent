package io.digiexpress.eveli.mig.v6;

import java.time.Duration;
import java.util.List;

import io.digiexpress.eveli.mig.v6.assets.AssetEvent;
import io.digiexpress.eveli.mig.v6.assets.AssetEvent.AssetEventMigration;
import io.digiexpress.eveli.mig.v6.assets.AssetMerger;
import io.digiexpress.eveli.mig.v6.baseline.OldEnvir;
import io.digiexpress.eveli.mig.v6.baseline.OldGit;
import io.digiexpress.eveli.mig.v6.baseline.impl.OldEnvir_impl;
import io.digiexpress.eveli.mig.v6.baseline.impl.OldGit_Impl;
import io.digiexpress.eveli.mig.v6.envir.MigrateEnvirEvent;
import io.digiexpress.eveli.mig.v6.stencil.MigrateStencilEvent;
import io.digiexpress.eveli.mig.v6.wrench.MigrateWrenchEvent;
import io.resys.thena.datasource.ThenaSqlDataSourceImpl;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.spi.FileSystem_ThenaImpl;
import io.resys.thena.fs.spi.commit.CommitBuilderImpl.EmptyCommitException;
import io.resys.thena.fs.tables.spi.FsTableNames;
import io.resys.thena.storesql.PgErrors;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.vertx.mutiny.sqlclient.Pool;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class V6Migration {
  private final OldGit oldGit;
  private final OldEnvir oldEnvir;
  private final Pool pool_old;
  private final Pool pool_new;
  private String stencil;
  private String wrench;
  private String envir;
  private String fs;
  
  public V6Migration(Pool pool_old, Pool pool_new) {
    super();
    this.oldGit = new OldGit_Impl(pool_old);
    this.oldEnvir = new OldEnvir_impl(pool_old);
    this.pool_old = pool_old;
    this.pool_new = pool_new;
  }
  public V6Migration stencil(String stencil) {
    this.stencil = RepoAssert.notEmpty(stencil, () -> "stencil repo name must be deifned");
    return this;
  }
  public V6Migration wrench(String wrench) {
    this.wrench = RepoAssert.notEmpty(wrench, () -> "wrench repo name must be deifned");
    return this;
  }
  public V6Migration envir(String envir) {
    this.envir = RepoAssert.notEmpty(envir, () -> "envir repo name must be deifned");
    return this;
  }
  public V6Migration fs(String fs) {
    this.fs = RepoAssert.notEmpty(fs, () -> "fs repo name must be deifned");
    return this;
  }
  public Uni<Void> execute() {
    RepoAssert.notEmpty(this.stencil, () -> "stencil repo name must be deifned");
    RepoAssert.notEmpty(this.wrench, () -> "wrench repo name must be deifned");
    RepoAssert.notEmpty(this.envir, () -> "envir repo name must be deifned");
    RepoAssert.notEmpty(this.fs, () -> "fs repo name must be deifned");
    
    return merge()
      .onItem().call(items -> 
        Uni.createFrom()
          .item(() -> commit(items))
          .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
      )
      .onItem().transformToUni(ignore -> Uni.createFrom().voidItem());
  }

  private Uni<List<AssetEvent>> merge() {
    return Uni.combine().all().unis(
        oldGit.findAll(stencil),
        oldGit.findAll(wrench),
        oldEnvir.findAll(envir)
      )
      .asTuple()
      .onItem().transform(tuple -> 
        new AssetMerger()
          .stencil(tuple.getItem1())
          .wrench(tuple.getItem2())
          .envir(tuple.getItem3())
          .build()
      );
  }
  
  private Object commit(List<AssetEvent> events) {
    final var fs = createFs();

    for(final var event : events) {
      if(event.getOperations().isEmpty()) {
        continue;
      }
      
      try {
        getMigration(fs, event).execute()
          .await().atMost(Duration.ofMinutes(1));
      } catch(EmptyCommitException e) {
        log.error("empty commit, skipping!");
      }
    }
    return 0;
  }
  
  private AssetEventMigration getMigration(FileSystem fs, AssetEvent event) {
    switch (event.getSourceType()) {
      case STENCIL: return new MigrateStencilEvent(fs, event);
      case WRENCH: return new MigrateWrenchEvent(fs, event);
      case ENVIR: return new MigrateEnvirEvent(fs, event);
      default: throw new IllegalArgumentException("Unexpected value: " + event.getSourceType());
    }
  }
  
  private FileSystem_ThenaImpl createFs() {
    final var fs = FileSystem_ThenaImpl.createInstance()
        .client(pool_new)
        .errorHandler(new PgErrors())
        .tenantName(this.fs)
        .build();
    
    final var tenant = fs.tenants().createOneTenant()
      .buildOnlyIfNotCreated()
      .onItem().transform(e -> e.getItem2().getRepo())
      .await().atMost(Duration.ofMinutes(1));
    
    final var datasource = (ThenaSqlDataSourceImpl) fs.getStartingState().getDataSource();
    final var names = FsTableNames.defaults().toRepo(tenant);
    final var isWipe = true;
    
    if(isWipe) {
      datasource.getClient().query("delete from " + names.getTreeIndex())
        .execute().await().atMost(Duration.ofMillis(1000));
      
      datasource.getClient().query("delete from " + names.getTag())
        .execute().await().atMost(Duration.ofMillis(1000));
      
      datasource.getClient().query("delete from " + names.getRef())
        .execute().await().atMost(Duration.ofMinutes(5));
      
      datasource.getClient().query("delete from " + names.getCommit())
        .execute().await().atMost(Duration.ofMillis(1000));
      
      datasource.getClient().query("delete from " + names.getTree())
        .execute().await().atMost(Duration.ofMillis(1000));
      
      datasource.getClient().query("delete from " + names.getProps())
        .execute().await().atMost(Duration.ofMillis(1000));
      
      datasource.getClient().query("delete from " + names.getBlob())
        .execute().await().atMost(Duration.ofMillis(1000));
    }
    return fs;
  }
}
