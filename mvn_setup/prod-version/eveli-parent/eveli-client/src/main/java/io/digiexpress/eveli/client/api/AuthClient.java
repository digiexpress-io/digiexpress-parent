package io.digiexpress.eveli.client.api;

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

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;



public interface AuthClient {
  Liveness getLiveness();
  
  
  Worker getWorker();
  Customer getCustomer();
  CustomerRoles getCustomerRoles();
  
  
  interface AnyPrincipal {
    String getUsername(); // get the subject name
  }
  
  
  @Value.Immutable
  interface Worker {
    UserType getType();
    WorkerPrincipal getPrincipal();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCustomerRoles.class) @JsonDeserialize(as = ImmutableCustomerRoles.class)
  interface CustomerRoles {
    UserType getType();
    CustomerRolesPrincipal getPrincipal();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomer.class) @JsonDeserialize(as = ImmutableCustomer.class)
  interface Customer {
    UserType getType();
    CustomerPrincipal getPrincipal();
  }

  @Value.Immutable
  interface WorkerPrincipal extends AnyPrincipal {
    String getUsername(); // get the subject name
    String getEmail();
    List<String> getRoles();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomerRolesPrincipal.class) @JsonDeserialize(as = ImmutableCustomerRolesPrincipal.class)
  interface CustomerRolesPrincipal extends AnyPrincipal {
    String getIdentifier();
    String getUserName();
    List<String> getRoles();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomerPrincipal.class) @JsonDeserialize(as = ImmutableCustomerPrincipal.class)
  interface CustomerPrincipal extends AnyPrincipal  {
    String getId();
    String getSsn();
    String getUsername();
    String getFirstName();
    String getLastName();
    CustomerContact getContact();
    Boolean getProtectionOrder();

    @Nullable String getRepresentedId();
    
    @Nullable
    CustomerRepresentedPerson getRepresentedPerson();
    @Nullable
    CustomerRepresentedCompany getRepresentedCompany();  
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCustomerContact.class) @JsonDeserialize(as = ImmutableCustomerContact.class)
  interface CustomerContact {
    String getEmail();
    @Nullable
    CustomerAddress getAddress();
    @Nullable
    String getAddressValue();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableCustomerAddress.class) @JsonDeserialize(as = ImmutableCustomerAddress.class)
  interface CustomerAddress {
    String getLocality();
    String getStreet();
    String getPostalCode();
    String getCountry();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomerRepresentedPerson.class) @JsonDeserialize(as = ImmutableCustomerRepresentedPerson.class)
  interface CustomerRepresentedPerson {
    String getPersonId();
    String getName();
    String[] getRepresentativeName();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomerRepresentedCompany.class) @JsonDeserialize(as = ImmutableCustomerRepresentedCompany.class)
  interface CustomerRepresentedCompany {
    String getCompanyId();
    String getName();
  }
  
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableLiveness.class) @JsonDeserialize(as = ImmutableLiveness.class)
  interface Liveness {
    // Issuance in seconds
    long getIssuedAtTime();
    
    // Expiration in seconds
    long getExpiresIn();
  }
  
  
  enum UserType {
    ANON, AUTH
  }
}
