package io.digiexpress.eveli.client.web.resources.gamut;

import java.time.OffsetDateTime;
import java.util.Optional;

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

import io.digiexpress.eveli.client.api.FeedbackClient;
import io.digiexpress.eveli.client.api.FeedbackClient.CustomerFeedback;
import io.digiexpress.eveli.client.api.GamutAuthClient;
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.program.ArticleProgram.LocalizedSite;
import io.resys.limaone.program.ImmutableLocalizedSite;
import io.resys.limaone.spi.program.input.DefaultArticleProgramInput;
import io.resys.thena.api.entities.Alias;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/portal/site")
@RequiredArgsConstructor
public class GamutSiteController {

  private final io.resys.limaone.program.Runtime runtime;
  private final Optional<Authoring> authoring;
  private final FeedbackClient feedback;
  private final GamutAuthClient auth;
 
  @GetMapping
  public Uni<LocalizedSite> getOneSiteByLocale(
      @RequestParam(name = "locale") String locale, 
      @RequestParam(name = "cockpitId", required = false) String cockpitId) {
    
    final LocalizedSite failsafe = ImmutableLocalizedSite.builder()
        .id("under-construction")
        .images("images")
        .locale(locale)
        .build();

    try {
      final var runtime = this.runtime.withTenant(Optional.ofNullable(cockpitId));
      final var bundle = runtime.getBundle();
      final var article = bundle.queryArticles().findOne();
      
      if(article.isEmpty()) {
        return Uni.createFrom().item(failsafe);
      }
      
      final var input = DefaultArticleProgramInput.builder()
          .runtime(runtime)
          .locale(locale)
          .targetDate(OffsetDateTime.now())
          .user(auth.getParticipant())
          .build();
      
      final var sites = article.get().run(input).getSites();
      final var site =  Optional.ofNullable(sites.get(locale));
      if(site.isEmpty()) {
        return Uni.createFrom().item(failsafe);
      }
      
      return Uni.createFrom().item(site.get());
    } catch(Exception error) {
      log.error("Failed to resolve site for locale: {}, because of error: {}", locale, error.getMessage(), error);
      return Uni.createFrom().item(failsafe);
    }
  }
  
  @GetMapping(path = "feedback")
  public Multi<CustomerFeedback> findAllFeedback() {
    return feedback.queryCustomerFeedbacks().findAll();
  }
  
  @GetMapping(path = "cockpits")
  public Multi<Alias> findAllCockpits() {
    if(authoring.isEmpty()) {
      return Multi.createFrom().empty();
    }
    return authoring.get().tid().aliasQuery().findAll();
  }
}