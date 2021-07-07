package io.resys.wrench.assets.context.config;

/*-
 * #%L
 * wrench-component-context
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix="wrench.assets")
@Component
public class AssetConfigBean {

  @Value("${enabled:true}")
  private boolean enabled;

  @Value("${rest:true}")
  private boolean rest;
  
  @Value("${tag-format:}")
  private String tagFormat;
  
  public String getTagFormat() {
    return tagFormat;
  }

  public void setTagFormat(String tagFormat) {
    this.tagFormat = tagFormat;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isRest() {
    return rest;
  }

  public void setRest(boolean rest) {
    this.rest = rest;
  }
}
