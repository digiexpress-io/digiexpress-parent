package io.digiexpress.eveli.mig.v6.baseline.impl;

import io.digiexpress.eveli.mig.v6.baseline.OldEnvir;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OldEnvir_impl implements OldEnvir {
  private final io.vertx.mutiny.sqlclient.Pool pool;
  
  @Override
  public Uni<OldEnvirObjects> findAll(String tenanPrefix) {
    return new OldEnvirQuery(pool).findAll(tenanPrefix);
  }

}
