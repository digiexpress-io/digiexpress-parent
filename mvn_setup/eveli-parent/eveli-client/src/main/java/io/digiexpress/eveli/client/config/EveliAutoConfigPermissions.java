package io.digiexpress.eveli.client.config;

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

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import io.digiexpress.eveli.client.config.factory.YamlPropertySourceFactory;
import lombok.Data;

@Configuration
@Data
@ConfigurationProperties(prefix = "eveli.authorization")
@PropertySource(value = "classpath:eveliPermissions.yaml", factory = YamlPropertySourceFactory.class)
public class EveliAutoConfigPermissions {

  private List<Access> worker;
  @Data
  public static class Access {
    private String pathPattern;
    private String method;
    private List<String> roles;

  }

}
