package io.dialob.spring.composer.controllers;

/*-
 * #%L
 * wrench-assets-application
 * %%
 * Copyright (C) 2016 - 2021 Copyright 2020 ReSys OÜ
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import io.dialob.client.spi.support.FileUtils;
import io.dialob.spring.composer.config.UiConfigBean;

@Controller
public class DialobComposerUiRedirectController {

  private final UiConfigBean configBean;

  public DialobComposerUiRedirectController(UiConfigBean configBean) {
    super();
    this.configBean = configBean;
  }

  @RequestMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
  public String index() {
    return "redirect:/" + FileUtils.cleanPath(configBean.getServicePath()) + "/";
  }
}
