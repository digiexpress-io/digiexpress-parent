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

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.IAMClient.ResultType;
import io.thestencil.iam.api.IAMClient.UserQuery;
import io.thestencil.iam.api.IAMClient.UserQueryResult;
import io.thestencil.iam.api.ImmutableContact;
import io.thestencil.iam.api.ImmutableUser;
import io.thestencil.iam.api.ImmutableUserQueryResult;

public class JsonWebTokenUserQuery implements UserQuery {
  private final JsonWebToken idToken;

  public JsonWebTokenUserQuery(JsonWebToken idToken) {
    super();
    this.idToken = idToken;
  }

  @Override
  public Uni<UserQueryResult> get() {
    this.idToken.getClaimNames();
    final String last = this.idToken.getClaim("family_name");
    final String first = this.idToken.getClaim("given_name");
    final var builder = ImmutableUser.builder()
      .id(this.idToken.getClaim("email"))
      .username(first + " " + last)
      .lastName(last)
      .firstName(first)
      .protectionOrder(false)
      .ssn("")
      .contact(ImmutableContact.builder()
          .email(this.idToken.getClaim("email"))
          .build());
    
    final var result = ImmutableUserQueryResult.builder()
        .type(ResultType.OK)
        .user(builder.build())
        .build();
    
    return Uni.createFrom().item(result);
  }
}
