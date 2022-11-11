package io.digiexpress.client.spi.executors;

import io.dialob.api.proto.Actions;
import io.digiexpress.client.api.ProcessState;
import io.digiexpress.client.api.ServiceClient.DialobExecutor;
import io.digiexpress.client.api.ServiceClient.Execution;
import io.digiexpress.client.api.ServiceClient.ExecutionDialobBody;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceEnvir;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DialobExecutorImpl implements DialobExecutor {
  private final ServiceClientConfig config;
  private final ProcessState state;
  private final ServiceEnvir envir;
  
  @Override
  public DialobExecutor actions(Actions userActions) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public Execution<ExecutionDialobBody> build() {
    // TODO Auto-generated method stub
    return null;
  }
}
