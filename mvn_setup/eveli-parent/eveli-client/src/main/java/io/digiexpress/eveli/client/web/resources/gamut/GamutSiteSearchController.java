package io.digiexpress.eveli.client.web.resources.gamut;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.immutables.value.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.digiexpress.eveli.client.config.EveliPropsMetisSearch;
import io.resys.metis.search.api.MetisSearchClient;
import io.resys.metis.search.api.MetisSearchResult;
import io.smallrye.mutiny.Uni;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/portal/site/search")
public class GamutSiteSearchController {

  private final MetisSearchClient search;
  private final EveliPropsMetisSearch props;

  /** Per-IP, in-memory, keyed by {@code remoteAddr} (not {@code X-Forwarded-For}). */
  private final Cache<String, AtomicInteger> callsPerAddress;

  public GamutSiteSearchController(MetisSearchClient search, EveliPropsMetisSearch props) {
    this.search = search;
    this.props = props;
    this.callsPerAddress = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(props.getQuery().getRateLimitWindowSeconds()))
        .maximumSize(50_000)
        .build();
  }

  @GetMapping
  public Uni<SiteSearchResponse> findByText(
      @RequestParam(name = "q") String query,
      @RequestParam(name = "locale") String locale,
      @RequestParam(name = "limit", required = false) Integer limit,
      HttpServletRequest request) {

    if (!isIndexReadyForPortal()) {
      return Uni.createFrom().item(fallbackResponse(query, locale));
    }
    if (isOverRateLimit(request)) {
      return Uni.createFrom().item(fallbackResponse(query, locale));
    }

    final var search = this.search.query()
        .locale(locale)
        .limit(limit == null ? props.getQuery().getDefaultLimit() : limit);

    return search.findByText(query)
        .onItem().transform(results -> (SiteSearchResponse) ImmutableSiteSearchResponse.builder()
            .query(query)
            .locale(locale)
            .addAllResults(results)
            .build())
        .onFailure().recoverWithItem(error -> {
          if (error instanceof io.smallrye.mutiny.TimeoutException) {
            log.warn("Metis search timed out for locale: {}, query length: {}, query hash: {}, because of: {}",
                locale, query == null ? 0 : query.length(), queryHash(query), error.toString());
          } else {
            log.error("Metis search failed for locale: {}, query length: {}, query hash: {}, because of: {}",
                locale, query == null ? 0 : query.length(), queryHash(query), error.toString(), error);
          }
          return fallbackResponse(query, locale);
        });
  }

  private boolean isIndexReadyForPortal() {
    try {
      return search.isIndexReadyForPortal();
    } catch (RuntimeException error) {
      log.error("Metis reindex status check failed, serving the keyword fallback, because of: {}",
          error.toString(), error);
      return false;
    }
  }

  private SiteSearchResponse fallbackResponse(String query, String locale) {
    return ImmutableSiteSearchResponse.builder()
        .query(query == null ? "" : query)
        .locale(locale)
        .fallback(true)
        .build();
  }

  private boolean isOverRateLimit(HttpServletRequest request) {
    final var max = props.getQuery().getRateLimitRequests();
    if (max <= 0) {
      return false;
    }
    final var address = request == null ? null : request.getRemoteAddr();
    if (address == null) {
      return false;
    }
    final var calls = callsPerAddress.get(address, key -> new AtomicInteger()).incrementAndGet();
    if (calls <= max) {
      return false;
    }
    log.warn("Metis search rate limit reached for a client, {} calls in {}s, serving the keyword fallback",
        calls, props.getQuery().getRateLimitWindowSeconds());
    return true;
  }

  private String queryHash(String query) {
    return query == null ? "-" : Integer.toHexString(query.hashCode());
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableSiteSearchResponse.class)
  @JsonDeserialize(as = ImmutableSiteSearchResponse.class)
  public interface SiteSearchResponse {
    String getQuery();

    String getLocale();

    @Value.Default
    default boolean getFallback() {
      return false;
    }

    List<MetisSearchResult> getResults();
  }
}
