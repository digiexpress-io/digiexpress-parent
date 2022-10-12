package io.digiexpress.client.api;

import org.immutables.value.Value;

import io.dialob.client.api.DialobClient;
import io.resys.hdes.client.api.HdesClient;
import io.thestencil.client.api.StencilClient;

@Value.Immutable
public interface DigiexpressConfig {
  DigiexpressStore getStore();
  DigiexpressCache getCache();
  DigiexpressTypesMapper getMapper();

  DialobClient getDialob();
  HdesClient getHdes();
  StencilClient getStencil();
}