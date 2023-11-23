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

import io.resys.crm.client.api.model.Customer.CustomerAddress;
import io.resys.crm.client.api.model.Customer.CustomerBody;
import io.resys.crm.client.api.model.Customer.CustomerBodyType;
import io.resys.crm.client.api.model.Customer.CustomerContact;



@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "commandType")
@JsonSubTypes({
  @Type(value = ImmutableCreateCustomer.class, name = "CreateCustomer"),  
  @Type(value = ImmutableUpsertSuomiFiPerson.class, name = "UpsertSuomiFiPerson"),  
  @Type(value = ImmutableUpsertSuomiFiRep.class, name = "UpsertSuomiFiRep"),  
  @Type(value = ImmutableChangeCustomerFirstName.class, name = "ChangeCustomerFirstName"),  
  @Type(value = ImmutableChangeCustomerLastName.class, name = "ChangeCustomerLastName"),  
  @Type(value = ImmutableChangeCustomerSsn.class, name = "ChangeCustomerSsn"),  
  @Type(value = ImmutableChangeCustomerEmail.class, name = "ChangeCustomerEmail"),  
  @Type(value = ImmutableChangeCustomerAddress.class, name = "ChangeCustomerAddress"),  
  @Type(value = ImmutableArchiveCustomer.class, name = "ArchiveCustomer")
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
    ChangeCustomerFirstName,
    ChangeCustomerLastName,
    ChangeCustomerSsn,
    ChangeCustomerEmail,
    ChangeCustomerAddress,
    ArchiveCustomer
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCreateCustomer.class) @JsonDeserialize(as = ImmutableCreateCustomer.class)
  interface CreateCustomer extends CustomerCommand {
    String getExternalId();
    CustomerBody getBody();
    
    @Value.Default
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.CreateCustomer; }
  }
  
  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.PROPERTY,
      property = "commandType")
  @JsonSubTypes({
    @Type(value = ImmutableUpsertSuomiFiPerson.class, name = "UpsertSuomiFiPerson"),  
    @Type(value = ImmutableUpsertSuomiFiRep.class, name = "UpsertSuomiFiRep"),  
    @Type(value = ImmutableChangeCustomerFirstName.class, name = "ChangeCustomerFirstName"),  
    @Type(value = ImmutableChangeCustomerLastName.class, name = "ChangeCustomerLastName"),  
    @Type(value = ImmutableChangeCustomerSsn.class, name = "ChangeCustomerSsn"),  
    @Type(value = ImmutableChangeCustomerEmail.class, name = "ChangeCustomerEmail"),  
    @Type(value = ImmutableChangeCustomerAddress.class, name = "ChangeCustomerAddress"),  
    @Type(value = ImmutableArchiveCustomer.class, name = "ArchiveCustomer")
  })
  interface CustomerUpdateCommand extends CustomerCommand {
    String getCustomerId(); // SSN or Business ID or Internal ID
    CustomerUpdateCommand withUserId(String userId);
    CustomerUpdateCommand withTargetDate(Instant targetDate);
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUpsertSuomiFiPerson.class) @JsonDeserialize(as = ImmutableUpsertSuomiFiPerson.class)
  interface UpsertSuomiFiPerson extends CustomerUpdateCommand {
    String getUserName();
    String getFirstName();
    String getLastName();
    Optional<Boolean> getProtectionOrder();
    Optional<CustomerContact> getContact();
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.UpsertSuomiFiPerson; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableUpsertSuomiFiRep.class) @JsonDeserialize(as = ImmutableUpsertSuomiFiRep.class)
  interface UpsertSuomiFiRep extends CustomerUpdateCommand {
    String getName();
    CustomerBodyType getCustomerType();
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.UpsertSuomiFiRep; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeCustomerFirstName.class) @JsonDeserialize(as = ImmutableChangeCustomerFirstName.class)
  interface ChangeCustomerFirstName extends CustomerUpdateCommand {
    String getFirstName();
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.ChangeCustomerFirstName; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeCustomerLastName.class) @JsonDeserialize(as = ImmutableChangeCustomerLastName.class)
  interface ChangeCustomerLastName extends CustomerUpdateCommand {
    String getLastName();
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.ChangeCustomerLastName; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeCustomerSsn.class) @JsonDeserialize(as = ImmutableChangeCustomerSsn.class)
  interface ChangeCustomerSsn extends CustomerUpdateCommand {
    String getNewSsn();
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.ChangeCustomerSsn; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeCustomerEmail.class) @JsonDeserialize(as = ImmutableChangeCustomerEmail.class)
  interface ChangeCustomerEmail extends CustomerUpdateCommand {
    String getEmail();
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.ChangeCustomerEmail; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableChangeCustomerAddress.class) @JsonDeserialize(as = ImmutableChangeCustomerAddress.class)
  interface ChangeCustomerAddress extends CustomerUpdateCommand {
    CustomerAddress getAddress();
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.ChangeCustomerAddress; }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableArchiveCustomer.class) @JsonDeserialize(as = ImmutableArchiveCustomer.class)
  interface ArchiveCustomer extends CustomerUpdateCommand {
    @Override default CustomerCommandType getCommandType() { return CustomerCommandType.ArchiveCustomer; }
  }

}
