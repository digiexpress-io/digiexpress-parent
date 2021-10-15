package io.resys.wrench.assets.flow.spi.executors;

/*-
 * #%L
 * wrench-component-assets-flow
 * %%
 * Copyright (C) 2016 - 2017 Copyright 2016 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.function.Function;

import io.resys.wrench.assets.bundle.spi.flowtask.FlowTaskExecutorException;
import io.resys.wrench.assets.flow.api.FlowExecutorRepository.FlowExecutor;
import io.resys.wrench.assets.flow.api.FlowExecutorRepository.FlowTaskExecutor;
import io.resys.hdes.client.api.programs.FlowResult;
import io.resys.hdes.client.api.programs.FlowProgram.FlowProgramStepType;
import io.resys.hdes.client.api.programs.FlowProgram.FlowProgramStep;
import io.resys.hdes.client.api.programs.FlowResult.FlowExecutionStatus;
import io.resys.hdes.client.api.programs.FlowResult.FlowTask;
import io.resys.wrench.assets.flow.spi.FlowException;

public class GenericFlowExecutor implements FlowExecutor {

  private final Function<FlowProgramStepType, FlowTaskExecutor> executor;

  public GenericFlowExecutor(
      Function<FlowProgramStepType, FlowTaskExecutor> executor) {
    super();
    this.executor = executor;
  }

  @Override
  public void execute(FlowResult flow) {
    flow.getContext().setStatus(FlowExecutionStatus.RUNNING);
    FlowProgramStep node = flow.getModel().getStep().get(flow.getContext().getPointer());
    run(flow, node);
  }

  protected void run(FlowResult flow, FlowProgramStep node) {

    try {
      FlowTask executable = flow.start(node);
      FlowProgramStep next = executor.apply(node.getType()).execute(flow, executable);

      if(flow.getContext().getStatus() == FlowExecutionStatus.ENDED ||
          flow.getContext().getStatus() == FlowExecutionStatus.SUSPENDED) {

        return;
      }

      run(flow, next);
    } catch(FlowException e) {
      throw e;
    } catch(FlowTaskExecutorException e) {
      final String msg = "Flow with id: " + flow.getModel().getId() + ", failed to execute task: " + node.getId() + " because: " + e.getMessage();
      throw new FlowException(msg, flow, node);      
    } catch(Exception e) {
      final String msg = "Flow with id: " + flow.getModel().getId() + ", failed to execute task: " + node.getId() + " because: " + e.getMessage();
      throw new FlowException(msg, flow, node);
    }
  }
}
