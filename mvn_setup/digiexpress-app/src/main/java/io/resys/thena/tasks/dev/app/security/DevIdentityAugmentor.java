package io.resys.thena.tasks.dev.app.security;

import java.util.HashSet;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jose4j.jwt.JwtClaims;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.resys.thena.tasks.dev.app.RandomDataProvider;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;



@IfBuildProfile("dev")
@ApplicationScoped
@Slf4j
public class DevIdentityAugmentor implements SecurityIdentityAugmentor {
  @Inject PrincipalCache cache;
  
  @Override
  public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
    log.debug("Augment identity of {}", identity.getPrincipal().getName());
    return Uni.createFrom().item(() -> create(identity))
        .onItem().transformToUni(created -> merge(created));
  }

  private Uni<SecurityIdentity> merge(SecurityIdentity src) {
    final var principal = (JsonWebToken) src.getPrincipal();
    final var sub = (String) principal.getClaim(Claims.sub.name());
    final var email = (String) principal.getClaim(Claims.email.name());
    return cache.getPrincipalPermissions(sub, email).onItem()
        .transform(permissions -> QuarkusSecurityIdentity.builder(src)
              .addRoles(new HashSet<>(permissions))
              .build());
  }
  
  private SecurityIdentity create(SecurityIdentity src) {
    final var vimes = RandomDataProvider.ASSIGNEES.get(1);
    final var claims = new JwtClaims();
    claims.setClaim(Claims.sub.name(), vimes);
    claims.setClaim(Claims.given_name.name(), "Sam");
    claims.setClaim(Claims.family_name.name(), "Vimes");
    claims.setClaim(Claims.email.name(), "sam.vimes@digiexpress.io");
    
    final JsonWebToken principal = new DefaultJWTCallerPrincipal("DEV-TOKEN", claims);
    final QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(src);
    return builder
        .setPrincipal(principal)
        .setAnonymous(false)
        .build();
    
  }
}
