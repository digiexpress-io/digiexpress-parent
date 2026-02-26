package io.resys.limaone.spi.dependency;

import io.resys.limaone.model.Model;

public interface Resolution {
  
  Artifact getOneArtifact(String id);
  
  interface ResolutionBuilder {
    NewArtifact newArtifact();
    Resolution build();
  }
  
  interface NewArtifact {
    NewArtifact id(String id);
    NewArtifact name(String name);
    RequireDependency requireDependnecy();
    void build();
  }
  
  interface RequireDependency {
    RequireDependency id(String idOrName); 
    RequireDependency bodyType(Model.BodyType bodyType); 
    RequireDependency validator(Validator validator);
    void build();
  }
}