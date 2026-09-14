package io.resys.metis.tests.search;

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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.resys.limaone.program.ArticleProgram;
import io.resys.limaone.program.ArticleProgram.ArticleProgramInput;
import io.resys.limaone.program.ArticleProgram.ArticleProgramResult;
import io.resys.limaone.program.ArticleProgram.LocalizedSite;
import io.resys.limaone.program.Compiler.Bundle;
import io.resys.limaone.program.ImmutableLocalizedSite;
import io.resys.limaone.program.ImmutableTopic;
import io.resys.limaone.program.ImmutableTopicBlob;
import io.resys.limaone.program.ImmutableTopicLink;
import io.resys.limaone.program.Runtime;
import io.resys.metis.api.ImmutableMetisConfig;
import io.resys.metis.api.MetisConfig;
import io.resys.metis.search.api.ImmutableMetisSearchConfig;
import io.resys.metis.search.api.ImmutableMetisSearchIndexingConfig;
import io.resys.metis.search.api.MetisSearchConfig;
import io.resys.metis.search.api.MetisSearchIndexingConfig;
import io.resys.metis.search.spi.index.SiteContentReader;
import io.resys.metis.search.spi.index.SiteContentReader.ContentUnavailableException;
import io.resys.metis.search.spi.index.SiteSearchDocumentBuilder;

public class SiteContentReaderTest {

  private static final MetisConfig PLATFORM = ImmutableMetisConfig.builder().build();
  private static final MetisSearchConfig CONFIG =
      ImmutableMetisSearchConfig.builder().locales(List.of("fi")).build();
  private static final MetisSearchIndexingConfig INDEXING =
      ImmutableMetisSearchIndexingConfig.builder().build();

  @Test
  public void aMissingBundleFailsInsteadOfReportingAnEmptySite() {
    final var runtime = Mockito.mock(Runtime.class);
    Mockito.when(runtime.getCachelessBundle()).thenReturn(null);

    Assertions.assertThrows(ContentUnavailableException.class, () -> reader(runtime).readDocuments());
  }

  @Test
  public void aMissingArticleProgramFailsInsteadOfReportingAnEmptySite() {
    final var runtime = runtimeOf(null, Map.of());

    Assertions.assertThrows(ContentUnavailableException.class, () -> reader(runtime).readDocuments());
  }

  @Test
  public void noConfiguredLocaleResolvingToASiteFails() {
    final var runtime = runtimeOf(Mockito.mock(ArticleProgram.class), Map.of("sv", site()));

    Assertions.assertThrows(ContentUnavailableException.class, () -> reader(runtime).readDocuments());
  }

  @Test
  public void aResolvedSiteWithoutWorkflowsReadsAsZeroDocuments() {
    final var empty = ImmutableLocalizedSite.builder().id("site-1").images("images").locale("fi").build();
    final var runtime = runtimeOf(Mockito.mock(ArticleProgram.class), Map.of("fi", empty));

    Assertions.assertEquals(List.of(), reader(runtime).readDocuments());
  }

  @Test
  public void readsTheCachelessBundleSoThatADeploymentIsNotIndexedFromTheOldSite() {
    final var runtime = runtimeOf(Mockito.mock(ArticleProgram.class), Map.of("fi", site()));

    final var documents = reader(runtime).readDocuments();

    Assertions.assertEquals(1, documents.size());
    Mockito.verify(runtime).getCachelessBundle();
    Mockito.verify(runtime.getCachelessBundle()).withCacheless();
    Mockito.verify(runtime, Mockito.never()).getBundle();
  }

  private SiteContentReader reader(Runtime runtime) {
    return new SiteContentReader(runtime, CONFIG, new SiteSearchDocumentBuilder(PLATFORM, INDEXING));
  }

  private Runtime runtimeOf(ArticleProgram article, Map<String, LocalizedSite> sites) {
    final var runtime = Mockito.mock(Runtime.class);
    final var bundle = Mockito.mock(Bundle.class, Mockito.RETURNS_DEEP_STUBS);
    Mockito.when(runtime.getCachelessBundle()).thenReturn(bundle);
    Mockito.when(bundle.withCacheless()).thenReturn(bundle);
    Mockito.when(bundle.queryArticles().findOne()).thenReturn(java.util.Optional.ofNullable(article));

    if (article != null) {
      final var result = Mockito.mock(ArticleProgramResult.class);
      Mockito.when(result.getSites()).thenReturn(sites);
      Mockito.when(article.run(Mockito.any(ArticleProgramInput.class))).thenReturn(result);
    }
    return runtime;
  }

  private LocalizedSite site() {
    return ImmutableLocalizedSite.builder()
        .id("site-1").images("images").locale("fi")
        .putLinks("wf-library", ImmutableTopicLink.builder()
            .id("wf-library").type("workflow").name("Varaa kirja kirjastosta").value("/library")
            .global(false).workflow(true).build())
        .putBlobs("blob-library", ImmutableTopicBlob.builder()
            .id("blob-library").value("Kirjaston kirja varataan verkossa.").build())
        .putTopics("topic-library", ImmutableTopic.builder()
            .id("topic-library").name("Kirjasto").blob("blob-library").addLinks("wf-library").build())
        .build();
  }
}
