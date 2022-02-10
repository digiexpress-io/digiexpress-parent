package io.resys.hdes.spring.composer.controllers;

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

import io.resys.hdes.client.spi.util.FileUtils;
import io.resys.hdes.spring.composer.ComposerAutoConfiguration.IdeToken;
import io.resys.hdes.spring.composer.ComposerAutoConfiguration.SpringIdeTokenSupplier;
import io.resys.hdes.spring.composer.ComposerConfigBean;
import io.resys.hdes.spring.composer.controllers.util.ControllerUtil;
import io.resys.hdes.spring.composer.controllers.util.IdeOnClasspath;
import io.resys.hdes.spring.composer.controllers.util.ThymeleafConfig;

@Controller
public class IdeController {
  @Value("${server.servlet.context-path}")
  private String contextPath;
  
  private final ComposerConfigBean composerConfig;
  private final IdeOnClasspath ideOnClasspath;
  private final Optional<SpringIdeTokenSupplier> token;
  
  public IdeController(ComposerConfigBean composerConfig, IdeOnClasspath ideOnClasspath, Optional<SpringIdeTokenSupplier> token) {
    this.composerConfig = composerConfig;
    this.ideOnClasspath = ideOnClasspath;
    this.token = token;
  }

  @RequestMapping(value = ComposerConfigBean.IDE_CTX_PATH, produces = MediaType.TEXT_HTML_VALUE)
  public String wrench(
      HttpServletRequest request,
      Model model,
      @RequestHeader(value = "Host", required = false) String host,
      @RequestHeader(value = "X-Forwarded-Proto", required = false, defaultValue = "") String proto) {

    Optional<IdeToken> token = this.token.map(t -> t.get(request)).orElse(Optional.empty());
    
    String restUrl = ControllerUtil.getRestUrl(proto, host, composerConfig.getRestContextPath(), contextPath);
    if(composerConfig.isIdeHttps() && !restUrl.startsWith("https")) {
      restUrl = restUrl.replaceFirst("http", "https");
    }
    
    ThymeleafConfig config = new ThymeleafConfig()
      .setContextPath("/" + FileUtils.cleanPath(composerConfig.getIdeContextPath()))
      .setUrl(restUrl)
      .setManifest(ideOnClasspath.getManifest())
      .setCss(ideOnClasspath.getCss())
      .setMainJs(ideOnClasspath.getMainJs())
      .setHash(ideOnClasspath.getHash())
      .setStatus(composerConfig.getStatus())
      .setOidc(composerConfig.getOidc())
      .setCsrf(token.orElse(null));

    model.addAttribute("config", config);
    return "wrench-ide";
  }
}
