package io.resys.crm.client.spi;

import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.spi.DocStoreImpl;
import io.resys.thena.spi.ThenaDocConfig;



public class CrmStore extends DocStoreImpl<CrmStore> {

  public static String DOC_TYPE_CUSTOMER = "CUSTOMER";
  
  public CrmStore(ThenaDocConfig config, DocStoreFactory<CrmStore> factory) {
    super(config, factory);
  }

  public static Builder<CrmStore> builder() {
    final DocStoreFactory<CrmStore> factory = (config, delegate) -> new CrmStore(config, delegate);
    return new Builder<CrmStore>(factory);
  }
  
  @Override
  public StoreTenantQuery<CrmStore> query() {
    return super.query().repoType(StructureType.doc);
  }

}
