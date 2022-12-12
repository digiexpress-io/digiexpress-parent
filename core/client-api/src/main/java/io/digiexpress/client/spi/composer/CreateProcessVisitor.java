package io.digiexpress.client.spi.composer;

import io.digiexpress.client.api.Client;
import io.digiexpress.client.api.ComposerEntity.CreateServiceDescriptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateProcessVisitor {
  private final Client client;
  private final CreateServiceDescriptor init;
  
  
}
