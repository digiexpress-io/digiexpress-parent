package io.digiexpress.eveli.client.spi.process;

import java.time.Duration;
import java.util.Optional;

import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.asserts.ProcessAssert;
import io.digiexpress.eveli.client.spi.process.CreateProcessExecutorImpl.TransactionWrapper;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class ProcessClientImpl implements ProcessClient {
  private final ProcessRepository processJPA;
  private final TransactionWrapper ts;
  private final EveliEnvirClient envir;
  public static final Duration asset_setup_duration = Duration.ofMinutes(5);
  
  @Override
  public QueryProcessInstances queryInstances() {
    return new QueryProcessInstancesImpl(processJPA);
  }
  @Override
  public ProcessInstanceStatusBuilder changeInstanceStatus() {
    return new ProcessInstanceStatusBuilderImpl(processJPA);
  }
  @Override
  public PaginateProcessInstances paginateInstances() {
    return new PaginateProcessInstancesImpl(processJPA);
  }
  @Override
  public ProcessAuthorizationQuery queryAuthorization() {
    return new ProcessAuthorizationQueryImpl(envir);
  }
  @Override
  public CreateProcessInstance createInstance() {
    return new CreateProcessInstanceImpl(processJPA);
  }
  @Override
  public CreateProcessExecutor createExecutor() {
    return new CreateProcessExecutorImpl(() -> queryInstances(), ts, envir);
  }
  @Override
  public ProcessInstanceBodyBuilder createBodyBuilder() {
    return new ProcessInstanceBodyBuilderImpl(processJPA);
  }
  @Override
  public ProcessQuestionnaireQuery queryProcessQuestionnaire() {
    return new ProcessQuestionnaireQuery() {
      @Override
      public Optional<JsonObject> findOneByTaskId(String taskId) {
        ProcessAssert.notNull(taskId, () -> "taskId must be defined!");    
        return processJPA.findQuestionnaireByTaskId(taskId).map(json -> new JsonObject(json));
      }
    };
  }  
}
