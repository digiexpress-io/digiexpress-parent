package io.thestencil.iam.spi.noname;

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

import java.security.Principal;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.smallrye.jwt.auth.cdi.NullJsonWebToken;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.IAMClient;
import io.thestencil.iam.api.ImmutableUserLiveness;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class IAMClientDefault implements IAMClient {
  private final Principal idToken;

  @Override
  public UserQuery userQuery() {
    if(idToken instanceof JsonWebToken) {
      return new JsonWebTokenUserQuery((JsonWebToken) idToken);
    } else if(idToken instanceof NullJsonWebToken) {
      
    }
    return null;
  }
  @Override
  public IAMClientConfig getConfig() {
    return null;
  }
  @Override
  public UserRolesQuery personRolesQuery() {
    return null;
  }
  @Override
  public UserRolesQuery companyRolesQuery() {
    return null;
  }
  @Override
  public LivenessQuery livenessQuery() {
    return new LivenessQuery() {
      @Override
      public Uni<UserLiveness> get() {
        return Uni.createFrom().item(ImmutableUserLiveness.builder()
            .expiresIn(6000)
            .issuedAtTime(0)
            .build());
      }
    };
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private Principal idToken;
    
    public Builder idToken(Principal idToken) {
      this.idToken = idToken;
      return this;
    }
    public IAMClientDefault builder() {
      return new IAMClientDefault(idToken);
    }
  }
}
