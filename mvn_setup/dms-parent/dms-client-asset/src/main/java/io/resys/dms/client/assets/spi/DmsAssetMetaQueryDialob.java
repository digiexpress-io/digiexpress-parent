package io.resys.dms.client.assets.spi;

import io.digiexpress.eveli.dialob.api.DialobClient;
import io.resys.dms.client.assets.api.DmsAssetsClient.AnyAsset;
import io.resys.dms.client.assets.api.DmsAssetsClient.DmsAssetMetaQuery;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DmsAssetMetaQueryDialob implements DmsAssetMetaQuery {
  private final DialobClient dialobClient;
  
  @Override
  public Uni<AnyAsset> findAll() {

    
    
    
    return null;
  }  
}
