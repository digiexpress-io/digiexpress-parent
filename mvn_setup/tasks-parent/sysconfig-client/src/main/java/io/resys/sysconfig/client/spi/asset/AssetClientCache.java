package io.resys.sysconfig.client.spi.asset;

import java.io.Serializable;
import java.util.Optional;

import io.resys.sysconfig.client.api.AssetClient.Asset;

public interface AssetClientCache {
  AssetClientCache withName(String name);
  void flush(String id);

  CacheEntry save(Asset src);
  <T> Optional<Asset> get(String id);
  

  interface CacheEntry extends Serializable {
    String getId();
    Asset getAsset();
  }
}
