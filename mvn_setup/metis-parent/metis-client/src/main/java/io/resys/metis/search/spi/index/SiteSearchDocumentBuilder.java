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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.resys.limaone.program.ArticleProgram.LocalizedSite;
import io.resys.limaone.program.ArticleProgram.Topic;
import io.resys.limaone.program.ArticleProgram.TopicLink;
import io.resys.metis.api.MetisConfig;
import io.resys.metis.search.api.MetisSearchIndexingConfig;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SiteSearchDocumentBuilder {

  private final MetisConfig platform;
  private final MetisSearchIndexingConfig config;

  public List<SiteSearchDocument> buildDocuments(LocalizedSite site) {
    final var locale = site.getLocale();
    final var topics = List.copyOf(site.getTopics().values());
    final var genericTopicIds = findGenericTopicIds(site, topics);

    final Map<String, List<Topic>> topicsByLink = new HashMap<>();
    for (final var topic : topics) {
      for (final var linkId : topic.getLinks()) {
        topicsByLink.computeIfAbsent(linkId, id -> new ArrayList<>()).add(topic);
      }
    }

    final List<SiteSearchDocument> documents = new ArrayList<>();
    for (final var link : site.getLinks().values()) {
      if (!Boolean.TRUE.equals(link.getWorkflow())) {
        continue;
      }
      final var owners = topicsByLink.getOrDefault(link.getId(), List.of());
      if (owners.isEmpty()) {
        continue;
      }
      documents.add(buildDocument(site, link, owners, genericTopicIds, locale));
    }
    return documents;
  }

  private SiteSearchDocument buildDocument(
      LocalizedSite site,
      TopicLink workflow,
      List<Topic> owners,
      Set<String> genericTopicIds,
      String locale) {

    final var topicNames = owners.stream()
        .map(Topic::getName)
        .filter(name -> name != null && !name.isBlank())
        .distinct()
        .toList();
    final var category = topicNames.isEmpty() ? null : topicNames.get(0);

    final List<String> pageTexts = new ArrayList<>();
    final List<String> headings = new ArrayList<>();
    final List<String> siblingLinks = new ArrayList<>();
    for (final var topic : owners) {
      if (genericTopicIds.contains(topic.getId())) {
        continue;
      }
      final var blobId = topic.getBlob();
      if (blobId != null) {
        final var blob = site.getBlobs().get(blobId);
        if (blob != null) {
          final var stripped = MarkdownStripper.strip(blob.getValue());
          if (!stripped.isBlank()) {
            pageTexts.add(truncate(stripped, config.getMaxPageChars()));
          }
        }
      }
      topic.getHeadings().stream()
          .map(heading -> heading.getName())
          .filter(name -> name != null && !name.isBlank())
          .forEach(headings::add);
      for (final var linkId : topic.getLinks()) {
        final var sibling = site.getLinks().get(linkId);
        if (sibling == null || sibling.getId().equals(workflow.getId())) {
          continue;
        }
        if (sibling.getName() != null && !sibling.getName().isBlank()) {
          siblingLinks.add(sibling.getName());
        }
      }
    }

    final var description = describe(workflow);
    final var supplementalText = buildSupplemental(topicNames, headings, pageTexts, siblingLinks);
    final var searchText = buildSearchText(workflow.getName(), description, supplementalText);
    final var llmContext = buildLlmContext(workflow.getName(), description, topicNames);
    final var contentHash = hash(String.join("|",
        locale,
        owners.get(0).getId(),
        workflow.getName(),
        searchText,
        platform.getEmbeddingModelId(),
        config.getMetadataPromptVersion()));

    return new SiteSearchDocument(
        workflow.getId(),
        owners.get(0).getId(),
        locale,
        workflow.getName(),
        category,
        description,
        searchText,
        supplementalText,
        llmContext,
        contentHash);
  }

  private Set<String> findGenericTopicIds(LocalizedSite site, List<Topic> topics) {
    final Set<String> generic = new LinkedHashSet<>();
    for (final var topic : topics) {
      final var workflowCount = topic.getLinks().stream()
          .map(linkId -> site.getLinks().get(linkId))
          .filter(link -> link != null && Boolean.TRUE.equals(link.getWorkflow()))
          .count();
      if (workflowCount > config.getGenericTopicThreshold()) {
        generic.add(topic.getId());
      }
    }
    return generic;
  }

  private String describe(TopicLink workflow) {
    final var parts = new ArrayList<String>();
    if (workflow.getFormName() != null && !workflow.getFormName().isBlank()) {
      parts.add(workflow.getFormName());
    }
    if (workflow.getFlowName() != null && !workflow.getFlowName().isBlank()) {
      parts.add(workflow.getFlowName());
    }
    return String.join(" ", parts);
  }

  private String buildSupplemental(
      List<String> topicNames, List<String> headings, List<String> pageTexts, List<String> linkLabels) {
    final var sb = new StringBuilder();
    appendSection(sb, "Topics", topicNames);
    appendSection(sb, "Headings", headings);
    appendSection(sb, "Pages", pageTexts);
    appendSection(sb, "Links", linkLabels);
    return sb.toString().trim();
  }

  private void appendSection(StringBuilder sb, String label, List<String> items) {
    if (items.isEmpty()) {
      return;
    }
    final Set<String> unique = new LinkedHashSet<>(items);
    sb.append(label).append(": ").append(String.join("; ", unique)).append('\n');
  }

  private String buildSearchText(String title, String description, String supplemental) {
    final var sb = new StringBuilder();
    sb.append("Title: ").append(title).append('\n');
    if (description != null && !description.isBlank()) {
      sb.append("Description: ").append(description).append('\n');
    }
    if (supplemental != null && !supplemental.isBlank()) {
      sb.append(supplemental);
    }
    return sb.toString().trim();
  }

  private String buildLlmContext(String title, String description, List<String> topicNames) {
    final var sb = new StringBuilder();
    sb.append("Title: ").append(title).append('\n');
    if (description != null && !description.isBlank()) {
      sb.append("Description: ").append(description).append('\n');
    }
    if (!topicNames.isEmpty()) {
      sb.append("Categories: ").append(String.join(", ", topicNames));
    }
    return truncate(sb.toString().trim(), config.getMaxLlmContextChars());
  }

  private String truncate(String text, int maxChars) {
    if (text.length() <= maxChars) {
      return text;
    }
    return text.substring(0, maxChars).trim();
  }

  private String hash(String input) {
    try {
      final var digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }
}
