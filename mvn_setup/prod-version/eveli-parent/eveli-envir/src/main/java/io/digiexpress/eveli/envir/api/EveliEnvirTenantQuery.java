package io.digiexpress.eveli.envir.api;

import java.util.Optional;

import io.smallrye.mutiny.Uni;

public interface EveliEnvirTenantQuery {
  EveliEnvirTenantQuery tenantName(String tenantName);

  Uni<EveliEnvirClient> deleteAll();
  Uni<EveliEnvirClient> delete();
  Uni<EveliEnvirClient> create();
  Uni<EveliEnvirClient> createIfNot();
  Uni<Optional<EveliEnvirClient>> get();
  EveliEnvirClient build();
}
