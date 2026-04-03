package io.digiexpress.thena.cockpit.client.api;

import java.util.Optional;

import jakarta.annotation.Nullable;

public interface CockpitTenantName {
  Optional<String> getTenantName(@Nullable String cockpitId);
  Optional<String> getUserTenantName();
}
