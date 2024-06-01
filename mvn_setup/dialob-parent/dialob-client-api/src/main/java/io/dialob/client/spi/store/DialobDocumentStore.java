package io.dialob.client.spi.store;

import io.resys.thena.spi.DocStoreImpl;
import io.resys.thena.spi.ThenaDocConfig;



public class DialobDocumentStore extends DocStoreImpl<DialobDocumentStore> {
  public DialobDocumentStore(ThenaDocConfig config, DocStoreFactory<DialobDocumentStore> factory) {
    super(config, factory);
  }
  public static Builder<DialobDocumentStore> builder() {
    final DocStoreFactory<DialobDocumentStore> factory = (config, delegate) -> new DialobDocumentStore(config, delegate);
    return new Builder<DialobDocumentStore>(factory);
  }

  public static enum DialobDocumentStoreTypes {
    FORM
  }
}
