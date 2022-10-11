package io.digiexpress.client.api;

import org.immutables.value.Value;

@Value.Immutable
public interface DigiexpressClientConfig {
  DigiexpressStore getStore();
  DigiexpressCache getCache();
  DigiexpressTypesMapper getMapper();
}