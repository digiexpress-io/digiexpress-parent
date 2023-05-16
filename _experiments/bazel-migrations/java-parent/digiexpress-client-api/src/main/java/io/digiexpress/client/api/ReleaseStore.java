package io.digiexpress.client.api;

import java.time.LocalDateTime;

import io.digiexpress.client.api.ClientEntity.ServiceRelease;
import io.smallrye.mutiny.Uni;


public interface ReleaseStore {
  Uni<ServiceRelease> save(ServiceRelease release);
  Uni<ServiceRelease> get(LocalDateTime targetDate); 
}
