package io.resys.thena.projects.client.spi;

import io.resys.thena.api.entities.doc.ThenaDocConfig;
import io.resys.thena.spi.DocStoreImpl;



public class ProjectStore extends DocStoreImpl<ProjectStore> {
  public ProjectStore(ThenaDocConfig config, DocStoreFactory<ProjectStore> factory) {
    super(config, factory);
  }
  public static Builder<ProjectStore> builder() {
    final DocStoreFactory<ProjectStore> factory = (config, delegate) -> new ProjectStore(config, delegate);
    return new Builder<ProjectStore>(factory);
  }
  
}
