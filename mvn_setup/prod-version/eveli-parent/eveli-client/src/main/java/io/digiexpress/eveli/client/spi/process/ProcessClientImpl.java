package io.digiexpress.eveli.client.spi.process;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.function.Supplier;

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.process.CreateProcessExecutorImpl.TransactionWrapper;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class ProcessClientImpl implements ProcessClient {
  private final ProcessRepository processJPA;
  private final HdesClient hdesClient;
  private final Supplier<ProgramEnvir> programEnvir;
  private final TransactionWrapper ts;
  private final EveliAssetClient workflowCommands;
  
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
    return new ProcessAuthorizationQueryImpl(hdesClient, programEnvir);
  }
  @Override
  public CreateProcessInstance createInstance() {
    return new CreateProcessInstanceImpl(processJPA);
  }
  @Override
  public CreateProcessExecutor createExecutor() {
    return new CreateProcessExecutorImpl(() -> queryInstances(), hdesClient, programEnvir, ts, workflowCommands);
  }
  @Override
  public ProcessInstanceBodyBuilder createBodyBuilder() {
    return new ProcessInstanceBodyBuilderImpl(processJPA);
  }  

}
