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

import io.digiexpress.eveli.client.api.AuthClient.Liveness;
import jakarta.annotation.Nullable;

public interface CrmClient {

  Liveness getLiveness();
  Customer getCustomer();
  CustomerRoles getCustomerRoles();
  
  enum CustomerType {
    ANON, // anonymous 
    REP_COMPANY,  // logged in as a customer who has selected to represent company
    REP_PERSON, // logged in as a customer who has selected to represent other person
    AUTH_CUSTOMER // normal logged in customer
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomerRoles.class) @JsonDeserialize(as = ImmutableCustomerRoles.class)
  interface CustomerRoles {
    String getIdentifier();
    String getUsername();
    List<String> getRoles();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomer.class) @JsonDeserialize(as = ImmutableCustomer.class)
  interface Customer {
    CustomerType getType();
    CustomerPrincipal getPrincipal();
  }

  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomerPrincipal.class) @JsonDeserialize(as = ImmutableCustomerPrincipal.class)
  interface CustomerPrincipal  {
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
}
