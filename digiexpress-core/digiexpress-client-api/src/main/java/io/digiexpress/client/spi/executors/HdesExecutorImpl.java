package io.digiexpress.client.spi.executors;

import io.digiexpress.client.api.ProcessState;
import io.digiexpress.client.api.ServiceClient.HdesExecutor;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceEnvir;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HdesExecutorImpl implements HdesExecutor {
  private final ServiceClientConfig config;
  private final ProcessState state;
  private final ServiceEnvir envir;
  
  
}
