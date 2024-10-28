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

import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.IAMClient;
import io.thestencil.iam.api.IAMClient.UserLiveness;
import io.thestencil.iam.api.IAMClient.UserQueryResult;
import io.thestencil.iam.api.IAMClient.UserRolesResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/gamut/iam")
@RequiredArgsConstructor
@Slf4j
public class GamutIamController {
  private final IAMClient iamClient;
  

  @GetMapping(path = "/")
  public Uni<UserQueryResult> getUser() {
    return iamClient.userQuery().get();
  }
  

  @GetMapping(path = "/roles")
  public Uni<UserRolesResult> getRoles(@RequestHeader("cookie") String id) {
    
    return iamClient.userQuery().get().onItem()
      .transformToUni(user -> {
        if(user.getUser().getRepresentedCompany() != null) {
          return iamClient.companyRolesQuery().id(id).get();
        } else if(user.getUser().getRepresentedPerson() != null) {
          return iamClient.personRolesQuery().id(id).get();
        }
        throw new RuntimeException("Represented person/company could not be resolved!");
      });
  }

  @GetMapping(path = "/liveness")
  public Uni<UserLiveness> getLiveness() {
    return iamClient.livenessQuery().get();
  }
}
