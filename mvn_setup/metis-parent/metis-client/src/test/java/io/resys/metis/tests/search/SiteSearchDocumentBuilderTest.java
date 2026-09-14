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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.limaone.program.ArticleProgram.LocalizedSite;
import io.resys.limaone.program.ArticleProgram.TopicLink;
import io.resys.limaone.program.ImmutableLocalizedSite;
import io.resys.limaone.program.ImmutableTopic;
import io.resys.limaone.program.ImmutableTopicBlob;
import io.resys.limaone.program.ImmutableTopicHeading;
import io.resys.limaone.program.ImmutableTopicLink;
import io.resys.metis.api.ImmutableMetisConfig;
import io.resys.metis.api.MetisConfig;
import io.resys.metis.search.api.ImmutableMetisSearchIndexingConfig;
import io.resys.metis.search.api.MetisSearchIndexingConfig;
import io.resys.metis.search.spi.index.SiteSearchDocument;
import io.resys.metis.search.spi.index.SiteSearchDocumentBuilder;

public class SiteSearchDocumentBuilderTest {

  private static final MetisConfig PLATFORM = ImmutableMetisConfig.builder().build();
  private static final MetisSearchIndexingConfig CONFIG =
      ImmutableMetisSearchIndexingConfig.builder().build();

  @Test
  public void oneDocumentPerWorkflowLinkWithTopicContent() {
    final var site = siteBuilder("fi")
        .putLinks("wf-1", workflow("wf-1", "Anna palautetta"))
        .putLinks("plain-1", plainLink("plain-1", "Lue lisää"))
        .putBlobs("blob-1", ImmutableTopicBlob.builder()
            .id("blob-1")
            .value("# Palaute\n\nVoit **antaa** palautetta [lomakkeella](/form).")
            .build())
        .putTopics("topic-1", ImmutableTopic.builder()
            .id("topic-1")
            .name("Palvelut")
            .blob("blob-1")
            .addLinks("wf-1", "plain-1")
            .addHeadings(ImmutableTopicHeading.builder().id("h1").name("Palaute").order(1).level(1).build())
            .build())
        .build();

    final var documents = build(site);

    Assertions.assertEquals(1, documents.size());
    final var doc = documents.get(0);
    Assertions.assertEquals("wf-1", doc.workflowId());
    Assertions.assertEquals("topic-1", doc.topicId());
    Assertions.assertEquals("fi", doc.locale());
    Assertions.assertEquals("Anna palautetta", doc.title());
    Assertions.assertEquals("Palvelut", doc.category());

    Assertions.assertTrue(doc.searchText().contains("Voit antaa palautetta lomakkeella."), doc.searchText());
    Assertions.assertFalse(doc.searchText().contains("**"), doc.searchText());
    Assertions.assertTrue(doc.searchText().contains("Headings: Palaute"), doc.searchText());
    Assertions.assertTrue(doc.searchText().contains("Links: Lue lisää"), doc.searchText());
  }

  @Test
  public void topicsBehindAuthenticationAreIndexed() {
    final var site = siteBuilder("en")
        .putLinks("wf-1", workflow("wf-1", "Secret service"))
        .putTopics("topic-1", ImmutableTopic.builder()
            .id("topic-1")
            .name("Secured")
            .auth(true)
            .addLinks("wf-1")
            .build())
        .build();

    final var documents = build(site);
    Assertions.assertEquals(1, documents.size());
    final var doc = documents.get(0);
    Assertions.assertEquals("wf-1", doc.workflowId());
    Assertions.assertEquals("topic-1", doc.topicId());
    Assertions.assertEquals("Secret service", doc.title());
  }

  @Test
  public void textOfTopicsSharedByManyWorkflowsIsLeftOut() {
    final var threshold = CONFIG.getGenericTopicThreshold();
    final var generic = ImmutableTopic.builder()
        .id("topic-generic")
        .name("Kaikki palvelut")
        .blob("blob-generic");
    final var site = siteBuilder("fi")
        .putBlobs("blob-generic", ImmutableTopicBlob.builder()
            .id("blob-generic")
            .value("Yleinen sivupohja jota ei pidä indeksoida")
            .build());

    for (int i = 0; i <= threshold; i++) {
      final var id = "wf-" + i;
      site.putLinks(id, workflow(id, "Palvelu " + i));
      generic.addLinks(id);
    }
    site.putTopics("topic-generic", generic.build());

    final var documents = build(site.build());

    Assertions.assertEquals(threshold + 1, documents.size());
    documents.forEach(doc -> Assertions.assertFalse(
        doc.searchText().contains("Yleinen sivupohja"), doc.searchText()));
  }

  @Test
  public void pageTextIsTruncatedAndContentHashTracksTheContent() {
    final var config = ImmutableMetisSearchIndexingConfig.builder().maxPageChars(20).build();
    final var site = siteBuilder("en")
        .putLinks("wf-1", workflow("wf-1", "Apply for a permit"))
        .putBlobs("blob-1", ImmutableTopicBlob.builder()
            .id("blob-1")
            .value("a".repeat(500))
            .build())
        .putTopics("topic-1", ImmutableTopic.builder()
            .id("topic-1")
            .name("Permits")
            .blob("blob-1")
            .addLinks("wf-1")
            .build())
        .build();

    final var renamed = siteBuilder("en")
        .putLinks("wf-1", workflow("wf-1", "Apply for a building permit"))
        .putBlobs("blob-1", ImmutableTopicBlob.builder()
            .id("blob-1")
            .value("a".repeat(500))
            .build())
        .putTopics("topic-1", ImmutableTopic.builder()
            .id("topic-1")
            .name("Permits")
            .blob("blob-1")
            .addLinks("wf-1")
            .build())
        .build();

    final var doc = new SiteSearchDocumentBuilder(PLATFORM, config).buildDocuments(site).get(0);
    Assertions.assertTrue(doc.searchText().contains("Pages: " + "a".repeat(20)), doc.searchText());

    final var unchanged = new SiteSearchDocumentBuilder(PLATFORM, config).buildDocuments(site).get(0);
    Assertions.assertEquals(doc.contentHash(), unchanged.contentHash());

    final var changed = new SiteSearchDocumentBuilder(PLATFORM, config).buildDocuments(renamed).get(0);
    Assertions.assertNotEquals(doc.contentHash(), changed.contentHash());
  }

  @Test
  public void contentHashChangesWhenTheOwningTopicOrTheModelChanges() {
    final var site = siteBuilder("en")
        .putLinks("wf-1", workflow("wf-1", "Apply for a permit"))
        .putTopics("topic-1", ImmutableTopic.builder()
            .id("topic-1").name("Permits").addLinks("wf-1").build())
        .build();
    final var moved = siteBuilder("en")
        .putLinks("wf-1", workflow("wf-1", "Apply for a permit"))
        .putTopics("topic-2", ImmutableTopic.builder()
            .id("topic-2").name("Permits").addLinks("wf-1").build())
        .build();

    final var original = new SiteSearchDocumentBuilder(PLATFORM, CONFIG).buildDocuments(site).get(0);
    Assertions.assertNotEquals(original.contentHash(),
        new SiteSearchDocumentBuilder(PLATFORM, CONFIG).buildDocuments(moved).get(0).contentHash());
    Assertions.assertNotEquals(original.contentHash(),
        new SiteSearchDocumentBuilder(
            ImmutableMetisConfig.builder().from(PLATFORM).embeddingModelId("bge-m3").build(), CONFIG)
            .buildDocuments(site).get(0).contentHash());
    Assertions.assertNotEquals(original.contentHash(),
        new SiteSearchDocumentBuilder(PLATFORM,
            ImmutableMetisSearchIndexingConfig.builder().from(CONFIG).metadataPromptVersion("v2").build())
            .buildDocuments(site).get(0).contentHash());
  }

  @Test
  public void llmContextStaysWithinItsCharacterBudget() {
    final var config = ImmutableMetisSearchIndexingConfig.builder().maxLlmContextChars(30).build();
    final var site = siteBuilder("en")
        .putLinks("wf-1", workflow("wf-1", "A very long service name that keeps going and going"))
        .putTopics("topic-1", ImmutableTopic.builder()
            .id("topic-1")
            .name("Services")
            .addLinks("wf-1")
            .build())
        .build();

    final var doc = new SiteSearchDocumentBuilder(PLATFORM, config).buildDocuments(site).get(0);
    Assertions.assertTrue(doc.llmContext().length() <= 30, doc.llmContext());
  }

  private List<SiteSearchDocument> build(LocalizedSite site) {
    return new SiteSearchDocumentBuilder(PLATFORM, CONFIG).buildDocuments(site);
  }

  private ImmutableLocalizedSite.Builder siteBuilder(String locale) {
    return ImmutableLocalizedSite.builder().id("site-1").images("images").locale(locale);
  }

  private TopicLink workflow(String id, String name) {
    return ImmutableTopicLink.builder()
        .id(id).type("workflow").name(name).value("/" + id)
        .global(false).workflow(true)
        .build();
  }

  private TopicLink plainLink(String id, String name) {
    return ImmutableTopicLink.builder()
        .id(id).type("internal").name(name).value("/" + id)
        .global(false).workflow(false)
        .build();
  }
}
