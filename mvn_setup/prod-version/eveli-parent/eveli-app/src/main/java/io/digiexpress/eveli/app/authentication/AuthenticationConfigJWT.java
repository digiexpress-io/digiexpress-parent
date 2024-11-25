package io.digiexpress.eveli.app.authentication;

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

import io.digiexpress.eveli.client.spi.auth.SpringJwtAuthClient;
import io.digiexpress.eveli.client.spi.auth.SpringJwtCrmClient;




@Configuration
public class AuthenticationConfigJWT  {
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
