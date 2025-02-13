package io.resys.avatar.client.spi;

import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.spi.DocStoreImpl;
import io.resys.thena.spi.ThenaDocConfig;

public class AvatarStore extends DocStoreImpl<AvatarStore> {

  public AvatarStore(ThenaDocConfig config, DocStoreFactory<AvatarStore> factory) {
    super(config, factory);
  }

  public static Builder<AvatarStore> builder() {
    final DocStoreFactory<AvatarStore> factory = (config, delegate) -> new AvatarStore(config, delegate);
    return new Builder<AvatarStore>(factory);
  }
  
  @Override
  public StoreTenantQuery<AvatarStore> query() {
    return super.query().repoType(StructureType.doc);
  }
  
  public static String DOC_TYPE_AVATAR = "AVATAR";
}

