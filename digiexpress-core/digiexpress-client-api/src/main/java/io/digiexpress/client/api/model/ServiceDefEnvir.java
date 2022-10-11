package io.digiexpress.client.api.model;

import java.util.List;
import java.util.Optional;

public interface ServiceDefEnvir {
  List<ServiceDef> findAll();
  ServiceDef get(String id);
  ServiceDef get(String name, String version);
  Optional<ServiceDef> findFirst();
}
