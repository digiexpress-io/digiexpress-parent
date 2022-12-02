package io.digiexpress.spring.composer.config;

import javax.annotation.Nullable;

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

import lombok.Data;

@ConfigurationProperties(prefix="digiexpress.composer.ui")
@Component
@Data
public class UiConfigBean {
  public static final String REST_SPRING_CTX_PATH = "digiexpress.composer.service.service-path";
  public static final String REST_SPRING_CTX_PATH_EXP = "${" + REST_SPRING_CTX_PATH + ":/assets}";
  public static final String UI_SPRING_CTX_PATH = "digiexpress.composer.ui.service-path";
  public static final String UI_SPRING_CTX_PATH_EXP = "${" + UI_SPRING_CTX_PATH + ":/ide}";
  public static final String UI_ENABLED = "digiexpress.composer.ui.enabled";
  
  public final static String API_STATE = "head";
  public final static String API_MIGRATE = "migrate";
  public final static String API_VERSION = "version";
  
  
  // is the whole component enabled
  @Value("${enabled:true}")
  private boolean enabled;
  @Value("${redirect:true}")
  private String redirect;
  @Value("${service-path:/ide}")
  private String servicePath;

  @Value("${https:false}")
  private boolean https;
  @Nullable
  @Value("${oidc-path:}")
  private String oidc;
  @Nullable
  @Value("${status-path:}")
  private String status;
}
