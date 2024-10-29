package io.digiexpress.eveli.client.web.resources;

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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.api.AuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/gamut/iam")
@RequiredArgsConstructor
@Slf4j
public class GamutIamController {
  private final AuthClient authClient;
  

  @GetMapping(path = "/")
  public AuthClient.Customer getUser() {
    return authClient.getCustomer();
  }
  
  @GetMapping(path = "/roles")
  public AuthClient.CustomerRoles getRoles(@RequestHeader("cookie") String id) {
    return authClient.getCustomerRoles();
  }

  @GetMapping(path = "/liveness")
  public AuthClient.Liveness getLiveness() {
    return authClient.getLiveness();
  }
}
