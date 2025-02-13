package io.resys.crm.client.tests.config;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.resys.crm.client.api.model.ImmutableArchiveCustomer;
import io.resys.crm.client.api.model.ImmutableChangeCustomerAddress;
import io.resys.crm.client.api.model.ImmutableChangeCustomerEmail;
import io.resys.crm.client.api.model.ImmutableChangeCustomerFirstName;
import io.resys.crm.client.api.model.ImmutableChangeCustomerLastName;
import io.resys.crm.client.api.model.ImmutableChangeCustomerSsn;
import io.resys.crm.client.api.model.ImmutableCreateCustomer;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.api.model.ImmutableUpsertSuomiFiPerson;
import io.resys.crm.client.api.model.ImmutableUpsertSuomiFiRep;
/*-
 * #%L
 * thena-quarkus-dev-app
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
import jakarta.enterprise.context.Dependent;

@Dependent
@RegisterForReflection(targets = {
    ImmutableCustomer.class,
    ImmutableCreateCustomer.class,
    ImmutableChangeCustomerFirstName.class,
    ImmutableChangeCustomerLastName.class,
    ImmutableUpsertSuomiFiPerson.class,
    ImmutableUpsertSuomiFiRep.class,
    ImmutableChangeCustomerSsn.class,
    ImmutableChangeCustomerEmail.class,
    ImmutableChangeCustomerAddress.class,
    ImmutableArchiveCustomer.class
})
public class BeanFactory {


}
