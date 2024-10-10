package io.resys.crm.client.spi.actions;

import java.util.ArrayList;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.Arrays;
import java.util.List;

import io.resys.crm.client.api.CrmClient.CreateCustomerAction;
import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand.CreateCustomer;
import io.resys.crm.client.api.model.CustomerCommand.UpsertSuomiFiPerson;
import io.resys.crm.client.spi.CrmStore;
import io.resys.crm.client.spi.visitors.CreateCustomersVisitor;
import io.resys.crm.client.spi.visitors.UpdateCustomerVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateCustomerActionImpl implements CreateCustomerAction {
  private final CrmStore ctx;
  
  @Override
  public Uni<Customer> createOne(CreateCustomer command) {
    return this.createMany(Arrays.asList(command))
       .onItem().transform(tasks -> tasks.get(0)) ;
  }
  
  @Override
  public Uni<List<Customer>> createMany(List<? extends CreateCustomer> commands) {
    return ctx.getConfig().accept(new CreateCustomersVisitor(commands));
  }

  @Override
  public Uni<Customer> createOne(UpsertSuomiFiPerson command) {
    return ctx.getConfig().accept(new UpdateCustomerVisitor(Arrays.asList(command), ctx)).onItem()
        .transformToUni(item -> item).onItem().transform(items -> items.get(0));
  }

  @Override
  public Uni<List<Customer>> upsertMany(List<? extends UpsertSuomiFiPerson> commands) {
    return ctx.getConfig().accept(new UpdateCustomerVisitor(new ArrayList<>(commands), ctx)).onItem()
        .transformToUni(item -> item).onItem().transform(items -> items);
  }

}
