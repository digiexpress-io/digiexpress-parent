package io.digiexpress.eveli.client.spi.org;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.digiexpress.eveli.client.api.OrgClient;
import io.digiexpress.eveli.client.spi.asserts.IntegrationAssert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class OrgClientImpl implements OrgClient {
  private final RestTemplate client;
  private final String membershipUrl;
  private final Optional<String> groupsUrl;

  @Override
  public GroupEmailQuery queryGroupEmails() {
    return new GroupEmailQueryImpl(client, membershipUrl);
  }

  @Override
  public GroupQuery queryGroups() {
    return new GroupQuery() {
      @Override
      public List<Group> findAll() {
        try {
          if(groupsUrl.isEmpty()) {
            return new ArrayList<>();
          }
          
          final var requestEntity = createRequest();
          final var serviceUrl = UriComponentsBuilder.fromHttpUrl(groupsUrl.get()).toUriString();
          final var response = client.exchange(serviceUrl, HttpMethod.GET, requestEntity, Group[].class);
          final var result = createResult(response);
          
          return result;
        } catch (Exception e) {
          log.error(e.getMessage(), e);
          throw IntegrationAssert.fail(e);
        }
      }
      
      private List<Group> createResult(ResponseEntity<Group[]> response) {
        IntegrationAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "Response status was: " + response.getStatusCode() + " but expecting 200!");
        final var emails = new HashSet<Group>(Arrays.asList(response.getBody()));
        return emails.stream().toList();
      }
      private HttpEntity<?> createRequest() {
        final var headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        final HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        return requestEntity;
      }
    };
  }
}
