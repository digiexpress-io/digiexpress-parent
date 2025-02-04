package io.digiexpress.eveli.client.spi.notification;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.digiexpress.eveli.client.api.NotificationCommands.EmailRequest;
import io.digiexpress.eveli.client.api.NotificationCommands.GroupMembershipQuery;
import io.digiexpress.eveli.client.spi.asserts.IntegrationAssert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class GroupMembershipRestQuery implements GroupMembershipQuery{

  private final String membershipUrl;
  private final RestTemplate client;
  
  @Override
  public Set<String> queryMembership(String groupName) {
    Set<String> result = new HashSet<>();
    log.debug("Group membership query for group {}", groupName);
    if (!StringUtils.isEmpty(membershipUrl)) {
      try {
        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        final HttpEntity<EmailRequest> requestEntity = new HttpEntity<>(headers);
        String serviceUrl = UriComponentsBuilder.fromHttpUrl(membershipUrl).queryParam("groupName", groupName).toUriString();
        log.debug("Sending group membership query to url {}", serviceUrl);
        ResponseEntity<String[]> response = client.exchange(serviceUrl, 
            HttpMethod.GET, requestEntity, String[].class);
        IntegrationAssert.isTrue(response.getStatusCode().is2xxSuccessful(), () -> "Response status was: " + response.getStatusCode() + " but expecting 200!");
        String[] groupMembers = response.getBody();
        Collections.addAll(result, groupMembers);
        log.debug("Group membership query result: {}", result);
      } catch (Exception e) {
        throw IntegrationAssert.fail(e);
      }
    }
    else {
      log.info("Group membership query, url not configured, returning empty result");
    }
    return result;
  }

}
