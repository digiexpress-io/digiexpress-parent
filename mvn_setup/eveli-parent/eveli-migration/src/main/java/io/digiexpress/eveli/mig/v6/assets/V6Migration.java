package io.digiexpress.eveli.mig.v6.assets;

import io.digiexpress.eveli.mig.v6.baseline.OldEnvir;
import io.digiexpress.eveli.mig.v6.baseline.OldGit;
import io.digiexpress.eveli.mig.v6.baseline.impl.OldEnvir_impl;
import io.digiexpress.eveli.mig.v6.baseline.impl.OldGit_Impl;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;


public class V6Migration {

  private final io.vertx.mutiny.sqlclient.Pool pool;
  private final OldGit oldGit;
  private final OldEnvir oldEnvir;
  
  private String stencil;
  
  public V6Migration(Pool pool) {
    super();
    this.pool = pool;
    this.oldGit = new OldGit_Impl(pool);
    this.oldEnvir = new OldEnvir_impl(pool);
  }

  public V6Migration stencil(String stencil) {
    this.stencil = RepoAssert.notEmpty(stencil, () -> "Stencil repo name must be deifned");
    return this;
  }
  
  
  public Uni<Void> execute() {
    final var stencil = RepoAssert.notEmpty(this.stencil, () -> "Stencil repo name must be deifned");
  }
  
}
