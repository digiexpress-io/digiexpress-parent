package io.digiexpress.client.spi.composer.visitors;

import io.digiexpress.client.api.ServiceClient;
import io.digiexpress.client.api.ServiceComposerCommand.CreateProcess;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateProcessVisitor {
  private final ServiceClient client;
  private final CreateProcess init;
  
  
}
