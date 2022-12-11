package io.digiexpress.client.spi.executors;

import java.time.LocalDateTime;
import java.util.UUID;

import io.dialob.client.spi.support.OidUtils;
import io.digiexpress.client.api.ImmutableExecution;
import io.digiexpress.client.api.ImmutableExecutionHdesBody;
import io.digiexpress.client.api.ImmutableFlowCompleted;
import io.digiexpress.client.api.ImmutableProcessState;
import io.digiexpress.client.api.ImmutableStep;
import io.digiexpress.client.api.ProcessState;
import io.digiexpress.client.api.ProcessState.FlowCompleted;
import io.digiexpress.client.api.Client.Execution;
import io.digiexpress.client.api.Client.ExecutionHdesBody;
import io.digiexpress.client.api.Client.HdesExecutor;
import io.digiexpress.client.api.Client.QuestionnaireStore;
import io.digiexpress.client.api.Client.ClientConfig;
import io.digiexpress.client.api.ServiceEnvir;
import io.digiexpress.client.api.ServiceEnvir.ServiceProgramHdes;
import io.digiexpress.client.spi.support.ServiceAssert;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HdesExecutorImpl implements HdesExecutor {
  private final ClientConfig config;
  private final ProcessState state;
  private final ServiceEnvir envir;
  private QuestionnaireStore store;
  private LocalDateTime targetDate;
  
  @Override
  public HdesExecutor targetDate(LocalDateTime targetDate) {
    ServiceAssert.notNull(targetDate, () -> "targetDate must be defined!");
    this.targetDate = targetDate;
    return this;
  }
  @Override
  public HdesExecutor store(QuestionnaireStore store) {
    this.store = store;
    return this;
  }
  @Override
  public Execution<ExecutionHdesBody> build() {
    final var start = LocalDateTime.now();
    final var targetDate = this.targetDate == null ? LocalDateTime.now() : this.targetDate;
    final var def = envir.getDef(targetDate).getDelegate(config);
    final var program = (ServiceProgramHdes) envir.getByRefId(def.getHdes());
    final var envir = program.getCompiled(config).orElse(null);
    ServiceAssert.notNull(envir, () -> "hdes envir must be defined!");
    
    final var created = state.getStepProcessCreated();
    final var flowId = created.getBody().getFlowId();
    final var flowName = program.getFlowName(flowId, config);
    final var flow = envir.getFlowsByName().get(flowName);
    ServiceAssert.isTrue(flow.getProgram().isPresent(), () -> "can't compile hdes flow: '" +  flowName + "'!");
    
    final var flowResult = config.getHdes().executor(envir)
        .inputMap(created.getBody().getParams())
        .flow(flowName)
        .andGetBody();
    
    final var nextState = ImmutableProcessState.builder()
        .from(state)
        .steps(state.getSteps())
        .addSteps(ImmutableStep.<FlowCompleted>builder()
            .id(UUID.randomUUID().toString())
            .version(1)
            .start(start)
            .end(LocalDateTime.now())
            .body(ImmutableFlowCompleted.builder()
                .accepts(flowResult.getAccepts())
                .returns(flowResult.getReturns())
                .build())
            .build())
        .build();
    
    return ImmutableExecution.<ExecutionHdesBody>builder()
        .body(ImmutableExecutionHdesBody.builder()
            .flow(flowResult)
            .state(nextState)
            .build())
        .build();
  } 
  
  final String genGid() {
    return OidUtils.gen();
  }

}
