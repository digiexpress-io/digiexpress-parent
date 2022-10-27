package io.digiexpress.client.api;

import java.time.LocalDateTime;

import io.digiexpress.client.api.ServiceDocument.ServiceReleaseDocument;
import io.smallrye.mutiny.Uni;


public interface ReleaseStore {
  Uni<ServiceReleaseDocument> save(ServiceReleaseDocument release);
  Uni<ServiceReleaseDocument> get(LocalDateTime targetDate); 
}
