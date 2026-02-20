package io.digiexpress.eveli.mig.v6.baseline.impl;

import io.digiexpress.eveli.mig.v6.baseline.OldGit;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OldGit_Impl implements OldGit {
  private final io.vertx.mutiny.sqlclient.Pool pool;

  @Override
  public Uni<OldGitObjects> findAll(String tenanPrefix) {
    return new OldGitQuery(pool).findAll(tenanPrefix);
  }

}
