package io.resys.thena.tasks.dev.app.security;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class DigiexpressSecurityIdentityAugmentor implements SecurityIdentityAugmentor {
  @Override
  public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
    log.debug("Augment identity of {}", identity.getPrincipal().getName());
    // TODO placeholder for group mapping
    return Uni.createFrom().item(identity);
  }
}
