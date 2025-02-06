package io.digiexpress.eveli.client.config;

import io.resys.hdes.client.api.HdesClient;
import io.thestencil.client.api.StencilClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EveliEditEnvir {
  public static final String BEAN_NAME = "eveliEditEnvir";
  
  private final StencilClient stencil;
  private final HdesClient wrench;
  private final EveliPropsAssets assetProps;
}
