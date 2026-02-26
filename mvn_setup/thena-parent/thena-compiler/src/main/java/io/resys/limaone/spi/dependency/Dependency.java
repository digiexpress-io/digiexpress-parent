package io.resys.limaone.spi.dependency;

import io.resys.limaone.model.Model;

public interface Dependency {
  String getArtifactId();
  String getArtifactName();
  Model.BodyType getArtifactType();
  
}
