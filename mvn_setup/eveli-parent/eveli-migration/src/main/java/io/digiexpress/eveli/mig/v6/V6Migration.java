package io.digiexpress.eveli.mig.v6;

import io.digiexpress.eveli.mig.v6.assets.AssetMerger;
import io.digiexpress.eveli.mig.v6.baseline.OldEnvir;
import io.digiexpress.eveli.mig.v6.baseline.OldGit;
import io.digiexpress.eveli.mig.v6.baseline.impl.OldEnvir_impl;
import io.digiexpress.eveli.mig.v6.baseline.impl.OldGit_Impl;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;


public class V6Migration {
  private final OldGit oldGit;
  private final OldEnvir oldEnvir;
  
  private String stencil;
  private String envir;
  
  public V6Migration(Pool pool) {
    super();
    this.oldGit = new OldGit_Impl(pool);
    this.oldEnvir = new OldEnvir_impl(pool);
  }
  public V6Migration stencil(String stencil) {
    this.stencil = RepoAssert.notEmpty(stencil, () -> "stencil repo name must be deifned");
    return this;
  }
  public V6Migration envir(String envir) {
    this.envir = RepoAssert.notEmpty(envir, () -> "envir repo name must be deifned");
    return this;
  }
  public Uni<Void> execute() {
    final var stencil = RepoAssert.notEmpty(this.stencil, () -> "stencil repo name must be deifned");
    final var envir = RepoAssert.notEmpty(this.envir, () -> "envir repo name must be deifned");
    
    
    return Uni.combine().all().unis(
      oldGit.findAll(stencil),
      oldEnvir.findAll(envir)
    )
    .asTuple()
    .onItem().transform(tuple -> {
      
      new AssetMerger()
        .stencil(tuple.getItem1())
        .build();
      
      
      return tuple;
    })
    .onItem().transformToUni(ignore -> Uni.createFrom().voidItem());
  }
  
}
