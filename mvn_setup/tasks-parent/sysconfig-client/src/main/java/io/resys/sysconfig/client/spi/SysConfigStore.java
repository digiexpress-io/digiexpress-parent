package io.resys.sysconfig.client.spi;

import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.spi.DocStoreImpl;
import io.resys.thena.spi.ThenaDocConfig;

public class SysConfigStore extends DocStoreImpl<SysConfigStore> {

  public SysConfigStore(ThenaDocConfig config, DocStoreFactory<SysConfigStore> factory) {
    super(config, factory);
  }

  public static Builder<SysConfigStore> builder() {
    final DocStoreFactory<SysConfigStore> factory = (config, delegate) -> new SysConfigStore(config, delegate);
    return new Builder<SysConfigStore>(factory);
  }
  
  @Override
  public StoreTenantQuery<SysConfigStore> query() {
    return super.query().repoType(StructureType.doc);
  }
}
