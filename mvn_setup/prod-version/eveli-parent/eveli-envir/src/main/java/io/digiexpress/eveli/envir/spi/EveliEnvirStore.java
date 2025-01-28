package io.digiexpress.eveli.envir.spi;

import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.spi.DocStoreImpl;
import io.resys.thena.spi.ThenaDocConfig;



public class EveliEnvirStore extends DocStoreImpl<EveliEnvirStore> {

  public static String DOC_TYPE_DEPLOYMENT = "deployment";
  public static String DOC_TYPE_DEPLOYMENT_ASSETS = "assets";
  
  public EveliEnvirStore(ThenaDocConfig config, DocStoreFactory<EveliEnvirStore> factory) {
    super(config, factory);
  }

  public static Builder<EveliEnvirStore> builder() {
    final DocStoreFactory<EveliEnvirStore> factory = (config, delegate) -> new EveliEnvirStore(config, delegate);
    return new Builder<EveliEnvirStore>(factory);
  }
  
  @Override
  public StoreTenantQuery<EveliEnvirStore> query() {
    return super.query().repoType(StructureType.doc);
  }
  

}
