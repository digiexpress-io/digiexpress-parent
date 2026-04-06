package io.digiexpress.eveli.client.api;

import java.nio.charset.StandardCharsets;

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
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.hash.Hashing;

import io.digiexpress.eveli.client.api.WorkerAuthClient.Liveness;
import io.resys.limaone.program.ImmutableParticipant;
import io.resys.limaone.program.ImmutableParticipantId;
import io.resys.limaone.program.ProgramInput.Participant;
import io.resys.limaone.program.ProgramInput.ParticipantId;
import jakarta.annotation.Nullable;



// currently logged in customer
public interface GamutAuthClient {
  @Nullable Liveness getLiveness();
  Customer getCustomer();
  CustomerRoles getCustomerRoles();
  
  
  default Participant getParticipant() {
    
    final var user = getCustomer().getPrincipal();
    final var person = user.getRepresentedPerson();
    final var company = user.getRepresentedCompany();
    
    final var init = ImmutableParticipant.builder()
      .anon(getCustomer().getType() == CustomerType.ANON)
      .partId(getCustomer().getCustomerId())
      .addAllIdentityRoles(getCustomerRoles().getRoles())
      .protectionOrder(getCustomer().getPrincipal().getProtectionOrder())
      .username(getCustomer().getPrincipal().getUsername());
    
    if(user.getRepresentedPerson() != null) {
      final var personNames = user.getRepresentedPerson().getRepresentativeName();
      init.representativeUsername(personNames[1] + " " + personNames[0]);
    } else if(user.getRepresentedCompany() != null) {
      init.representativeUsername(user.getRepresentedCompany().getName());
    }
    
    if(person != null) {
      final var representativeName = person.getRepresentativeName();
      final var representativeFirstName = representativeName[1];  
      final var representativeLastName = representativeName[0];
      return init      
        .firstName(representativeFirstName)
        .lastName(representativeLastName)
        .identity(person.getPersonId())
        .representativeFirstName(user.getFirstName())
        .representativeLastName(user.getLastName())
        .representativeIdentity(user.getSsn())
        .build();
    } else if(company != null) {
      return init
        .companyName(company.getName())
        .lastName(company.getName())
        .identity(company.getCompanyId())
        .representativeFirstName(user.getFirstName())
        .representativeLastName(user.getLastName())
        .representativeIdentity(user.getSsn())
        .build();
    }
    
    return init
      .firstName(user.getFirstName())
      .lastName(user.getLastName())
      .identity(user.getSsn())
      .email(user.getContact().getEmail())
      .address(user.getContact().getAddressValue())
      .build();
  }
  
  enum CustomerType {
    ANON, // anonymous 
    REP_COMPANY,  // logged in as a customer who has selected to represent company
    REP_PERSON, // logged in as a customer who has selected to represent other person
    AUTH_CUSTOMER // normal logged in customer
  }


  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomerRoles.class) @JsonDeserialize(as = ImmutableCustomerRoles.class)
  interface CustomerRoles {
    List<String> getRoles();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableCustomer.class) @JsonDeserialize(as = ImmutableCustomer.class)
  interface Customer {
    CustomerType getType();
    CustomerPrincipal getPrincipal();
    
    default ParticipantId getCustomerId() {
      final var principle = getPrincipal();
      final var holderId = Optional.ofNullable(principle.getRepresentedId()).orElse(principle.getSsn());
      return ImmutableParticipantId.builder()
          .realId(holderId)
          .hashId(Hashing
            .murmur3_128()
            .hashString(holderId, StandardCharsets.UTF_8)
            .toString())
          .build();
    }
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
