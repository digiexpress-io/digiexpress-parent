package io.digiexpress.eveli.app;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*-
 * #%L
 * eveli-app
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

import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.client.api.AuthClient.Liveness;
import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.ImmutableCustomer;
import io.digiexpress.eveli.client.api.ImmutableCustomerContact;
import io.digiexpress.eveli.client.api.ImmutableCustomerPrincipal;
import io.digiexpress.eveli.client.api.ImmutableCustomerRoles;
import io.digiexpress.eveli.client.api.ImmutableUser;
import io.digiexpress.eveli.client.api.ImmutableUserPrincipal;
import io.digiexpress.eveli.client.spi.auth.SpringJwtAuthClient;
import io.digiexpress.eveli.client.spi.crm.SpringJwtCrmClient;




@Configuration
public class SecurityProvider  {
  
  @Bean
  @Profile("fake-user")
  public AuthClient authClientFakeUser() {
    return new AuthClient() {
      @Override
      public User getUser() {
        return ImmutableUser.builder()
            .isAuthenticated(true)
            .principal(ImmutableUserPrincipal.builder()
                .isAdmin(true)
                .username("tester")
                .email("tester@resys.io")
                .roles(Arrays.asList())
                .build())
            .build();
      }


      @Override
      public Liveness getLiveness() {
        // TODO Auto-generated method stub
        return null;
      }
    };
  }
  
  @Bean
  @Profile("fake-user")
  public CrmClient crm() {
    return new CrmClient() {
      @Override
      public Liveness getLiveness() {
        return null;
      }
      @Override
      public Customer getCustomer() {
        return ImmutableCustomer.builder()
            .principal(ImmutableCustomerPrincipal.builder()
                .id(UUID.randomUUID().toString())
                .ssn("my-ssn")
                .username("same vimes")
                .firstName("same")
                .lastName("vimes")
                .protectionOrder(false)
                .contact(ImmutableCustomerContact.builder()
                    .email("same.vimes@resys.io")
                    .addressValue("test-street")
                    .build())
                .build())
            .type(CustomerType.AUTH_CUSTOMER)
            .build();
      }

      @Override
      public CustomerRoles getCustomerRoles() {
        final var customer = getCustomer();
        
        return ImmutableCustomerRoles.builder()
            .identifier(customer.getPrincipal().getSsn())
            .username(customer.getPrincipal().getUsername())
            .build();
      }
    };
  }
  
  
  @Bean
  @Profile("jwt")
  public SpringJwtAuthClient authClientJwt() {
    return new SpringJwtAuthClient();
  }
  @Bean
  @Profile("jwt")
  public SpringJwtCrmClient crmClientJwt() {
    return new SpringJwtCrmClient(new RestTemplate(), "");
  }
}
