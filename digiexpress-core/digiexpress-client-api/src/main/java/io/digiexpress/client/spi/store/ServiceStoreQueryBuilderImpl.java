package io.digiexpress.client.spi.store;



import io.digiexpress.client.api.ServiceStore;
import io.digiexpress.client.api.ServiceStore.StoreEntity;
import io.digiexpress.client.api.ServiceStore.StoreState;
import io.smallrye.mutiny.Uni;

public class ServiceStoreQueryBuilderImpl extends ServiceStoreTemplate implements ServiceStore.QueryBuilder {

  public ServiceStoreQueryBuilderImpl(ServiceStoreConfig config) {
    super(config);
  }

  @Override
  public Uni<StoreState> get() {
    return super.get();
  }
  @Override
  public Uni<StoreEntity> get(String id) {
    final var result = super.getEntityState(id);
    return result.onItem().transform(entityState -> entityState.getEntity());
  }
}
