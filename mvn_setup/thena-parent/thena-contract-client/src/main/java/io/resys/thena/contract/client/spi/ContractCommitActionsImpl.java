package io.resys.thena.contract.client.spi;

/*-
 * #%L
 * thena-contract-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.resys.thena.contract.client.api.ContractCommitActions;
import io.resys.thena.contract.client.spi.actions.CreateOneContractImpl;
import io.resys.thena.contract.client.spi.actions.ModifyOneContractImpl;
import io.resys.thena.contract.client.tables.ContractDb;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ContractCommitActionsImpl implements ContractCommitActions {
  private final ContractDb state;
  private final String tenantId;
  
  @Override
  public CreateOneContract createOneContract() {
    return new CreateOneContractImpl(state, tenantId);
  }
  @Override
  public ModifyOneContract modifyOneContract() {
    return new ModifyOneContractImpl(state, tenantId);
  }
}
