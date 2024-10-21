package io.digiexpress.eveli.client.api;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


public interface ProcessAuthorizationCommands {
  ProcessAuthorizationQuery query();
  

  interface ProcessAuthorizationQuery {
    ProcessAuthorization get(InitProcessAuthorization init);
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableProcessAuthorization.class)
  @JsonDeserialize(as = ImmutableProcessAuthorization.class)
  interface ProcessAuthorization {
    List<String> getUserRoles();
    List<String> getAllowedProcessNames();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableInitProcessAuthorization.class)
  @JsonDeserialize(as = ImmutableInitProcessAuthorization.class)
  interface InitProcessAuthorization {
    List<String> getUserRoles();
  }
}
