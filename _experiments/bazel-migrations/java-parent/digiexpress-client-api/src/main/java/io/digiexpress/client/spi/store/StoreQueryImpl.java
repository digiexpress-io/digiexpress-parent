package io.digiexpress.client.spi.store;



import io.digiexpress.client.api.ClientStore;
import io.digiexpress.client.api.ClientStore.StoreEntity;
import io.digiexpress.client.api.ClientStore.StoreState;
import io.smallrye.mutiny.Uni;

public class StoreQueryImpl extends DocDBCommandsSupport implements ClientStore.StoreQuery {

  public StoreQueryImpl(DocDBConfig config) {
    super(config);
  }
  @Override
  public Uni<StoreState> get() {
    return super.get();
  }
  @Override
  public Uni<StoreEntity> get(String id) {
    final var result = super.getState(id);
    return result.onItem().transform(entityState -> entityState.getEntity());
  }
}
