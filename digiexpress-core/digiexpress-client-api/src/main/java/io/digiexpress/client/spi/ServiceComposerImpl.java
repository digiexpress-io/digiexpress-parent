package io.digiexpress.client.spi;

import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceComposer;
import io.digiexpress.client.spi.composer.ComposerCreateBuilderImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServiceComposerImpl implements ServiceComposer {

  private final ServiceClient client;

  @Override
  public CreateBuilder create() {
    return new ComposerCreateBuilderImpl(client);
  }

  @Override
  public ServiceComposer.QueryBuilder query() {
    // TODO Auto-generated method stub
    return null;
  }
}
