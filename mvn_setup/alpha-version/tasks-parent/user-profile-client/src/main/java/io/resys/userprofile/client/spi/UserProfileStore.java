package io.resys.userprofile.client.spi;

import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.spi.DocStoreImpl;
import io.resys.thena.spi.ThenaDocConfig;



public class UserProfileStore extends DocStoreImpl<UserProfileStore> {

  public UserProfileStore(ThenaDocConfig config, DocStoreFactory<UserProfileStore> factory) {
    super(config, factory);
  }

  public static Builder<UserProfileStore> builder() {
    final DocStoreFactory<UserProfileStore> factory = (config, delegate) -> new UserProfileStore(config, delegate);
    return new Builder<UserProfileStore>(factory);
  }
  
  @Override
  public StoreTenantQuery<UserProfileStore> query() {
    return super.query().repoType(StructureType.doc);
  }
}
