package io.digiexpress.spring.composer.config;

import org.springframework.beans.factory.annotation.Value;

/*-
 * #%L
 * wrench-component-assets-persistence
 * %%
 * Copyright (C) 2016 - 2017 Copyright 2016 ReSys OÜ
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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;



@Component
@Data
@ConfigurationProperties(prefix="dialob.formdb.file")
public class FileConfigBean {
  
  @Value("${enabled:false}")
  private boolean enabled;
  @Value("${path:src/main/resources}")
  private String path;
  @Value("${email:asset.manager@resys.io}")
  private String email;
}
