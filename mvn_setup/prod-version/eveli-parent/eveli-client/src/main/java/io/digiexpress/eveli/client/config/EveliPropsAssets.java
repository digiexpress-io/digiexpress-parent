package io.digiexpress.eveli.client.config;

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

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;


@Data
@ConfigurationProperties(prefix = "eveli.assets")
public class EveliPropsAssets {
  /**
   * Enable asset management via live DB
   */
  private Boolean enabled;

  private Integer timezoneOffset = 2;
  
  
  /**
   * json of type Deployment, used for importing WRENCH, STENCIL, DIALOB, WORKFLOWS into empty DB
   */
  private String importDeployment;
  
  /**
   * If true uses form Id of tagged version to find correct form in process. Used in cases of shared database or 
   * forms transferred between enironments in manner where it retains it's id (e.g. file based form storage).
   * If false then form id to use is found by form name and tag name from form repository. Used in case when forms 
   * are stored in database or transferred with release in manner where id is not retained.
   */
  private Boolean useFormId;
  
}
