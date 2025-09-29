package io.digiexpress.tagomi.api;

import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiWorld;
import io.smallrye.mutiny.Uni;

public interface TagomiClient {

  WorldBuilder createWorld();
  
  
  interface WorldBuilder {
    WorldBuilder container(TagomiContainer container);
    Uni<TagomiWorld> build();
  }
  
}
