package io.resys.permission.client.security;

import java.util.HashSet;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.resys.permission.client.api.PermissionClient;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SecurityMerger implements SecurityIdentityAugmentor {
  private final PermissionClient client;
  
  @Override
  public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
    if(identity.isAnonymous()) {
      return Uni.createFrom().item(() -> identity);
    }
    
    return client.principalQuery().get(identity.getPrincipal().getName())
        .onItem().transform(p -> {
          return QuarkusSecurityIdentity.builder(identity)
              .addRoles(new HashSet<>(p.getRoles()))
              .build();
        });

    
  }

}