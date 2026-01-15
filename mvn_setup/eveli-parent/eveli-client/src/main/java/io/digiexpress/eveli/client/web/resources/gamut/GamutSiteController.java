package io.digiexpress.eveli.client.web.resources.gamut;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
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
import io.digiexpress.eveli.client.api.GamutAuthClient.CustomerType;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitIdSupplier;
import io.digiexpress.thena.cockpit.client.api.CockpitClient;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitConfig;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableLocalizedSite;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/portal/site")
@RequiredArgsConstructor
public class GamutSiteController {
  
  private final EveliEnvirClient envir;
  private final FeedbackClient feedback;
  private final GamutAuthClient auth;
  private final Optional<CockpitClient> cockpitClient;
  
  @GetMapping
  public Uni<LocalizedSite> getOneSiteByLocale(
      @RequestParam(name = "locale") String locale, 
      @RequestParam(name = "cockpitId", required = false) String cockpitId) {
    
    final CockpitIdSupplier cockpitIdSupplier = () -> Uni.createFrom().item(Optional.ofNullable(cockpitId));
    final var isAuth = auth.getCustomer().getType() != CustomerType.ANON; 
    
    return envir.withCockpitIdSupplier(cockpitIdSupplier).runtimeQuery().getOne().onItem().transform(runtime -> {
      final var data = runtime.getStencil(OffsetDateTime.now(), isAuth).getSites().get(locale);
      if(data == null) {
        final LocalizedSite failsafe = ImmutableLocalizedSite.builder().id("not-found")
            .images("images")
            .locale(locale)
            .build();
        return failsafe;
      }
      return ImmutableLocalizedSite.builder().from(data).id(data.getId()).build();
    }).onFailure().recoverWithItem(error -> {
      log.error("Failed to resolve site for locale: {}, because of error: {}", locale, error.getMessage(), error);
      final LocalizedSite failsafe = ImmutableLocalizedSite.builder().id("under-construction")
          .images("images")
          .locale(locale)
          .build();
          
      return failsafe;
    });
  }
  
  @GetMapping(path = "feedback")
  public List<CustomerFeedback> findAllFeedback() {
    return feedback.queryCustomerFeedbacks().findAll();
  }
  @GetMapping(path = "cockpits")
  public Multi<CockpitConfig> findAllCockpits() {
    return cockpitClient
        .map(client -> client.queries().cockpitQuery().findAll().onItem().transform(e -> e.getConfig()))
        .orElse(Multi.createFrom().items(Collections.<CockpitConfig>emptyList().stream()));
  } 
}