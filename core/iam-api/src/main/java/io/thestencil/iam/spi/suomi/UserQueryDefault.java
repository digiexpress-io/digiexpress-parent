package io.thestencil.iam.spi.suomi;

import java.util.Map;

/*-
 * #%L
 * iam-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.IAMClient.Address;
import io.thestencil.iam.api.IAMClient.RepresentedPerson;
import io.thestencil.iam.api.IAMClient.ResultType;
import io.thestencil.iam.api.IAMClient.User;
import io.thestencil.iam.api.IAMClient.UserQuery;
import io.thestencil.iam.api.IAMClient.UserQueryResult;
import io.thestencil.iam.api.ImmutableAddress;
import io.thestencil.iam.api.ImmutableContact;
import io.thestencil.iam.api.ImmutableRepresentedPerson;
import io.thestencil.iam.api.ImmutableUser;
import io.thestencil.iam.api.ImmutableUserQueryResult;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UserQueryDefault implements UserQuery {
  private final JsonWebToken idToken;
  
  @Override
  public Uni<UserQueryResult> get() {
    final var sub = (String) idToken.getClaim("sub");
    if(sub == null) {
      return Uni.createFrom().item(ImmutableUserQueryResult.builder().type(ResultType.ANONYMOUS).build());
    }
    return Uni.createFrom().item(ImmutableUserQueryResult.builder()
        .type(ResultType.OK)
        .user(toUser(sub))
        .build());
  }
  
  private User toUser(String sub) {
    final var firstName = (String) idToken.getClaim("firstNames");
    final var lastName = (String) idToken.getClaim("lastName");
    final var ssn = (String) idToken.getClaim("personalIdentityCode");
    final var email = (String) idToken.getClaim("email");
    
    final var address = toAddress();
    final var protectionOrder = "true".equals(idToken.getClaim("protectionOrder"));
    
    return ImmutableUser.builder()
        .firstName(orEmpty(firstName))
        .lastName(orEmpty(lastName))
        .ssn(orEmpty(ssn))
        .id(sub)
        .protectionOrder(protectionOrder)
        .representedPerson(toRepresentedPerson())
        .contact(ImmutableContact.builder()
            .email(orEmpty(email))
            .address(address)
            .addressValue(toAddressValue(address))
            .build())
        .build();
  }
  private String toAddressValue(Address src) {
    if(src == null) {
      return null;
    }
    return orEmpty(src.getStreet()) + ", " + orEmpty(src.getPostalCode()) + " " + orEmpty(src.getLocality());
  }
  
  private Address toAddress() {
    return ImmutableAddress.builder()
        .postalCode(orEmpty(idToken.getClaim("postalCode")))
        .locality(orEmpty(idToken.getClaim("locality")))
        .street(orEmpty(idToken.getClaim("streetAddress")))
        .country(orEmpty(idToken.getClaim("country")))
        .build();
  }
  
  @SuppressWarnings({ "unchecked" })
  private RepresentedPerson toRepresentedPerson() {
    final var value = (Map<String, String>) idToken.getClaim("representedPerson");
    if(value == null) {
      return null;
    }
    
    return ImmutableRepresentedPerson.builder()
        .name(value.get("name"))
        .personId(value.get("personId"))
        .build();
  }
  
  private static String orEmpty(String value) {
    return value == null ? "" : value; 
  }
}
