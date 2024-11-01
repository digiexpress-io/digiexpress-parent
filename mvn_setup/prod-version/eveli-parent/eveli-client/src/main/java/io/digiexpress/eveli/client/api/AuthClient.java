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



public interface AuthClient {
  Liveness getLiveness();
  User getUser();

  
  @Value.Immutable @JsonSerialize(as = ImmutableUser.class) @JsonDeserialize(as = ImmutableUser.class)
  interface User {
    UserPrincipal getPrincipal();
    boolean isAuthenticated();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableLiveness.class) @JsonDeserialize(as = ImmutableLiveness.class)
  interface Liveness {
    // Issuance in seconds
    long getIssuedAtTime();
    
    // Expiration in seconds
    long getExpiresIn();
  }

  @Value.Immutable
  interface UserPrincipal {
    String getUsername(); // get the subject name
    String getEmail();
    List<String> getRoles();
  }
}
