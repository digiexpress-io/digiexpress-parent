package io.digiexpress.eveli.client.spi.crm;

/*-
 * #%L
 * eveli-client
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

import io.digiexpress.eveli.client.api.CustomerAccountClient;
import io.digiexpress.eveli.client.api.ImmutableCustomerAccount;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerAccountClientImpl implements CustomerAccountClient {  
  private final ProcessClient processClient;

  @Override
  public CustomerAccountQuery accountQuery() {
    return new CustomerAccountQuery() {
      
      @Override
      public Uni<CustomerAccount> getOneByAnyId(String id) {
        var proc = processClient.queryInstances().findOneByQuestionnaireId(id).orElse(null);
        if(proc == null) {
          proc = processClient.queryInstances().findOneByTaskId(id).orElse(null);
        }
        if(proc == null) {
          proc = processClient.queryInstances().findOneById(id).orElse(null);
        }
        
        if(proc == null) {
          final var notFound = ImmutableCustomerAccount.builder()
              .customerId("")
              .id(id)
              .type(CrmAccountType.ANON)
              .build();
          return Uni.createFrom().item(notFound);  
        }
        
        final var found = ImmutableCustomerAccount.builder()
          .customerId(proc.getUserId() == null ? "" : proc.getUserId())
          .id(proc.getId().toString())
          .type(Boolean.TRUE.equals(proc.getAnon()) ? CrmAccountType.ANON : CrmAccountType.AUTH)
          .build();
        
        return Uni.createFrom().item(found);
        
      }
    };
  }
}
