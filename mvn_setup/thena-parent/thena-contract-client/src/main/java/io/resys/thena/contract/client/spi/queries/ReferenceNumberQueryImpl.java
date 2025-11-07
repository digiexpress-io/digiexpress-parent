package io.resys.thena.contract.client.spi.queries;

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

import java.text.SimpleDateFormat;
import java.util.Date;

import io.resys.thena.contract.client.api.ContractQueryActions.ReferenceNumberQuery;
import io.resys.thena.contract.client.tables.ContractDb;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReferenceNumberQueryImpl implements ReferenceNumberQuery {
  private final Uni<ContractDb> startingState;
  private static final SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMM");
  private static final String DATE_NUMBER_SEPARATOR_DEFAULT = "-";
  
  @Override
  public Multi<String> findNext(int howMany) {
    if(howMany == 1) {
      return startingState
          .onItem().transformToUni(state -> state.query().queryContractSeq().getNext())
          .onItem().transformToMulti(nextVal -> Multi.createFrom().item(formatSequence(nextVal)));
    }
    
    return startingState
        .onItem().transformToMulti(state -> state.query().queryContractSeq().findNext(howMany))
        .onItem().transform(nextVal -> formatSequence(nextVal));
  }
  
  private String formatSequence(long nextVal) {
    final Date now = new Date();
    return dataFormat.format(now) + DATE_NUMBER_SEPARATOR_DEFAULT + nextVal;
  }
}
