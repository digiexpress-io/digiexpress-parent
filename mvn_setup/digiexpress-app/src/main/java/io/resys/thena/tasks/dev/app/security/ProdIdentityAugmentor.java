package io.resys.thena.tasks.dev.app.security;

import java.util.HashSet;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;


@ApplicationScoped
@Slf4j
public class ProdIdentityAugmentor implements SecurityIdentityAugmentor {
  @Inject PrincipalCache cache;
  
  @Override
  public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
    if(identity.isAnonymous()) {
      return Uni.createFrom().item(() -> identity);
    }
   
    log.debug("Augment identity of {}", identity.getPrincipal().getName());
    return Uni.createFrom().item(() -> identity)
        .onItem().transformToUni(created -> merge(created));
  }

  private Uni<SecurityIdentity> merge(SecurityIdentity src) {
    final var principal = (JsonWebToken) src.getPrincipal();
    final var sub = (String) principal.getClaim(Claims.sub.name());
    final var email = (String) principal.getClaim(Claims.email.name());
    
    log.debug("Augment identity merge: {}, {}", sub, email);
    return cache.getPrincipalPermissions(sub, email).onItem()
        .transform(permissions -> {
          
          log.debug("Merging: {}", permissions.getPermissions());
          return QuarkusSecurityIdentity.builder(src)
          .addRoles(new HashSet<>(permissions.getPermissions()))
          .build();
        });
  }
}
