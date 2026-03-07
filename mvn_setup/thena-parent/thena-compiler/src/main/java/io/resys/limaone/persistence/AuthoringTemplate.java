package io.resys.limaone.persistence;

import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public abstract class AuthoringTemplate<IMPL, MODEL> {

  protected final AuthoringConfig config;
  
  abstract Uni<MODEL> build();
  
  public MODEL buildSync() {
    return build()
        .runSubscriptionOn(config.getWorkerPool())
        .await().atMost(config.getWorkerTimeout());
  }
}
