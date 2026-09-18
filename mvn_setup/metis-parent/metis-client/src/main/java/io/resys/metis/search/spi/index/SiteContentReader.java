package io.resys.metis.search.spi.index;

/*-
 * #%L
 * metis-client
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.resys.limaone.authoring.Authoring.WorldRef;
import io.resys.limaone.program.ImmutableParticipant;
import io.resys.limaone.program.ImmutableParticipantId;
import io.resys.limaone.program.ProgramInput.Participant;
import io.resys.limaone.program.Runtime;
import io.resys.limaone.spi.program.input.DefaultArticleProgramInput;
import io.resys.metis.search.api.MetisSearchConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SiteContentReader {

  private static final String INDEXER_ID = "metis-indexer";

  private final Runtime runtime;
  private final MetisSearchConfig config;
  private final SiteSearchDocumentBuilder documentBuilder;

  public static class ContentUnavailableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ContentUnavailableException(String message) {
      super(message);
    }
  }

  public List<SiteSearchDocument> readDocuments() {
    final var cacheless = runtime.getCachelessBundle();
    if (cacheless == null) {
      throw new ContentUnavailableException("no content bundle is deployed");
    }
    // Fresh cacheless view: skips the 5s debounce on the long-lived Runtime.
    final var fresh = cacheless.withCacheless();
    final var bundle = fresh == null ? cacheless : fresh;

    final var article = bundle.queryArticles().findOne();
    if (article.isEmpty()) {
      throw new ContentUnavailableException("no article program is deployed");
    }

    final var locales = config.getLocales();
    if (locales.isEmpty()) {
      throw new ContentUnavailableException(
          "no locales are configured, see eveli.metis.search.locales");
    }

    final List<SiteSearchDocument> documents = new ArrayList<>();
    var resolvedLocales = 0;
    for (final var locale : locales) {
      final var input = DefaultArticleProgramInput.builder()
          .runtime(runtime)
          .locale(locale)
          .targetDate(OffsetDateTime.now())
          .user(anonymous())
          .build();

      final var sites = article.get().run(input).getSites();
      final var site = Optional.ofNullable(sites.get(locale))
          .or(() -> Optional.ofNullable(sites.get(locale.toLowerCase())));
      if (site.isEmpty()) {
        log.info("Metis found no site content for locale: {}", locale);
        continue;
      }
      resolvedLocales++;
      final var localeDocuments = documentBuilder.buildDocuments(site.get());
      log.info("Metis built {} document(s) for locale: {}", localeDocuments.size(), locale);
      documents.addAll(localeDocuments);
    }

    if (resolvedLocales == 0) {
      throw new ContentUnavailableException(
          "none of the configured locales resolved to a site: " + locales);
    }
    return documents;
  }

  public Optional<String> currentBundleHash() {
    try {
      final var properties = runtime.getProperties();
      if (properties == null || properties.getModelDb() == null) {
        return Optional.empty();
      }
      return properties.getModelDb().worldRefQuery().findOneSync()
          .map(WorldRef::getHash)
          .filter(hash -> hash != null && !hash.isBlank());
    } catch (RuntimeException error) {
      log.debug("Metis could not read the Runtime world hash, because of: {}", error.toString());
      return Optional.empty();
    }
  }

  private Participant anonymous() {
    return ImmutableParticipant.builder()
        .identity(INDEXER_ID)
        .username(INDEXER_ID)
        .partId(ImmutableParticipantId.builder().realId(INDEXER_ID).hashId(INDEXER_ID).build())
        .anon(true)
        .protectionOrder(false)
        .build();
  }
}
