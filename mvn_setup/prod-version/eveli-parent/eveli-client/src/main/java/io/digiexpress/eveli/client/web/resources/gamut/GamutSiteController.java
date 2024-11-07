package io.digiexpress.eveli.client.web.resources.gamut;

import java.util.function.Supplier;

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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.thestencil.client.api.MigrationBuilder.LocalizedSite;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.spi.beans.LocalizedSiteBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/portal/site")
@RequiredArgsConstructor
public class GamutSiteController {
  
  private final Supplier<Sites> siteEnvir;

  @GetMapping
  public LocalizedSite getOneSiteByLocale(@RequestParam(name = "locale") String locale) {
   
    final var sites = siteEnvir.get();
    final var data = sites.getSites().get(locale);
    
    if(data == null) {
      return LocalizedSiteBean.builder().id("not-found")
          .images("images")
          .locale(locale)
          .build();
    }
    return LocalizedSiteBean.builder().from(data).id(data.getId()).build();
    
  }
}
