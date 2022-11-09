package io.digiexpress.client.spi.builders;

import java.util.stream.Collectors;

import io.digiexpress.client.api.ProcessState;
import io.digiexpress.client.api.ServiceClient.ArticleExecutor;
import io.digiexpress.client.api.ServiceClient.CreateProcessExecutor;
import io.digiexpress.client.api.ServiceClient.FillProcessExecutor;
import io.digiexpress.client.api.ServiceClient.FlowProcessExecutor;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceClient.ServiceExecutorBuilder;
import io.digiexpress.client.api.ServiceEnvir;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgram;
import io.digiexpress.client.spi.support.ServiceExecutorException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ServiceExecutorBuilderImpl implements ServiceExecutorBuilder {
  private final ServiceEnvir envir;
  private final ServiceClientConfig config;
  
  @Override
  public CreateProcessExecutor create(String nameOrId) {
    final ServiceProgram doc = envir.getValues().values().stream().findFirst()
        .orElseThrow(() ->
            ServiceExecutorException.serviceNotFound(
            () -> "Services available" + 
              String.join(",", envir.getValues().values().stream()
                .map(p -> p.getId())
                .collect(Collectors.toList())) + 
          "!"));
    
    
    final var process = doc.getDef().getProcesses().stream()
      .filter(p -> p.getId().equals(nameOrId) || p.getName().equals(nameOrId))
      .findFirst()
      .orElseThrow(() -> ServiceExecutorException.processNotFound(doc, nameOrId, 
          () -> "Accepted in: '" + doc.getId() + "' process id/name: " + 
              String.join(",", doc.getDef().getProcesses().stream()
                .map(p -> p.getId() + "/" + p.getName())
                .collect(Collectors.toList())) + 
          "!"));
    
    return new CreateProcessExecutorImpl(config, doc, process);
  }
  @Override
  public FillProcessExecutor fill(ProcessState state) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public FlowProcessExecutor flow(ProcessState state) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public ArticleExecutor article() {
    // TODO Auto-generated method stub
    return null;
  }
}
