package io.resys.crm.client.api.model;

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

import java.io.Serializable;
import java.time.Instant;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.crm.client.api.model.Customer.CustomerBodyType;
import io.resys.crm.client.api.model.Customer.CustomerContact;



@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableCreateCustomer.class, name = "CreateCustomer"),  
  @Type(value = ImmutableChangeCustomerInfo.class, name = "ChangeCustomerInfo"),
})
public interface CustomerCommand extends Serializable {
  @Nullable String getUserId();
  @Nullable Instant getTargetDate();
  CustomerCommandType getCommandType();
  
  
  CustomerCommand withUserId(String userId);
  CustomerCommand withTargetDate(Instant targetDate);
  
  enum CustomerCommandType {
    CreateCustomer, 
    UpsertSuomiFiPerson, 
    UpsertSuomiFiRep, 
    CreateOrUpdateCustomer, 
    ChangeCustomerInfo
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateCustomer.class) @JsonDeserialize(as = ImmutableCreateCustomer.class)
  interface CreateCustomer extends CustomerCommand {
    String getRepoId();
    String getName();
    
    @Value.Default
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.CreateCustomer; }
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    @Type(value = ImmutableChangeCustomerInfo.class, name = "ChangeCustomerInfo"),
  })
  interface CustomerUpdateCommand extends CustomerCommand {
    String getId();
    CustomerUpdateCommand withUserId(String userId);
    CustomerUpdateCommand withTargetDate(Instant targetDate);
  }
  

  @Value.Immutable @JsonSerialize(as = ImmutableChangeCustomerInfo.class) @JsonDeserialize(as = ImmutableChangeCustomerInfo.class)
  interface ChangeCustomerInfo extends CustomerUpdateCommand {
    String getName();
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.ChangeCustomerInfo; }
  }
  
  
  interface UpsertSuomiFiPerson extends CustomerUpdateCommand {
    String getUserName();
    String getSsn();
    String getFirstName();
    String getLastName();
    Optional<Boolean> getProtectionOrder();
    Optional<CustomerContact> getContact();
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.UpsertSuomiFiPerson; }
  }
  
  
  interface UpsertSuomiFiRep extends CustomerUpdateCommand {
    String getName();
    String getSsnOrBusinessId();
    CustomerBodyType getCustomerType();
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.UpsertSuomiFiRep; }
  }
  
  
  // TODO: Break into individual commands
  interface UpdateCustomerInfo extends CustomerUpdateCommand {
    CustomerBodyType getBodyType();
    Optional<String> getFirstName();
    Optional<String> getLastName();
    Optional<String> getSsn();
    Optional<String> getBusinessId();
    CustomerContact getCustomerContact();
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.CreateOrUpdateCustomer; }
  }

}
