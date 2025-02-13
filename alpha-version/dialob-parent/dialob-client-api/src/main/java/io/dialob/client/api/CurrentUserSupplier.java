package io.dialob.client.api;

import java.util.function.Supplier;

import org.immutables.value.Value;

import io.dialob.client.api.CurrentUserSupplier.CurrentUser;

@FunctionalInterface
public interface CurrentUserSupplier extends Supplier<CurrentUser> {
  
  
  @Value.Immutable
  interface CurrentUser {
    String getUser();
    String getEmail();
  }
}


