package io.digiexpress.spring.composer.controllers;

/*-
 * #%L
 * hdes-spring-composer
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import io.digiexpress.spring.composer.ComposerAutoConfiguration.IdeToken;
import io.digiexpress.spring.composer.ComposerAutoConfiguration.SpringIdeTokenSupplier;
import io.digiexpress.spring.composer.config.UiConfigBean;
import io.digiexpress.spring.composer.controllers.util.ControllerUtil;
import io.digiexpress.spring.composer.controllers.util.FileUtils;
import io.digiexpress.spring.composer.controllers.util.IdeOnClasspath;
import io.digiexpress.spring.composer.controllers.util.ThymeleafConfig;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DigiexpressComposerUiController {
  @Value("${server.servlet.context-path}")
  private String contextPath;
  @Value(UiConfigBean.REST_SPRING_CTX_PATH_EXP)
  private String servicePath;
  
  private final UiConfigBean config;
  private final IdeOnClasspath ideOnClasspath;
  private final Optional<SpringIdeTokenSupplier> token;

  
  @RequestMapping(value = UiConfigBean.UI_SPRING_CTX_PATH_EXP, produces = MediaType.TEXT_HTML_VALUE)
  public String ui(
      HttpServletRequest request,
      Model model,
      @RequestHeader(value = "Host", required = false) String host,
      @RequestHeader(value = "X-Forwarded-Proto", required = false, defaultValue = "") String proto) {

    Optional<IdeToken> token = this.token.map(t -> t.get(request)).orElse(Optional.empty());
    
    String restUrl = ControllerUtil.getRestUrl(proto, host, servicePath, contextPath);
    if(config.isHttps() && !restUrl.startsWith("https")) {
      restUrl = restUrl.replaceFirst("http", "https");
    }
    
    ThymeleafConfig thymeleaf = new ThymeleafConfig()
      .setContextPath("/" + FileUtils.cleanPath(config.getServicePath()))
      .setUrl(restUrl)
      .setManifest(ideOnClasspath.getManifest())
      .setCss(ideOnClasspath.getCss())
      .setMainJs(ideOnClasspath.getMainJs())
      .setHash(ideOnClasspath.getHash())
      .setStatus(config.getStatus())
      .setOidc(config.getOidc())
      .setCsrf(token.orElse(null));

    model.addAttribute("config", thymeleaf);
    return "digiexpress-ui";
  }
}
