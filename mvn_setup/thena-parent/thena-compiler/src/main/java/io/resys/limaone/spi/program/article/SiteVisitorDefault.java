package io.resys.limaone.spi.program.article;

import java.nio.charset.StandardCharsets;

/*-
 * #%L
 * stencil-static-content
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.hash.Hashing;

import io.resys.limaone.program.ArticleProgram.LocalizedSite;
import io.resys.limaone.program.ArticleProgram.Topic;
import io.resys.limaone.program.ArticleProgram.TopicBlob;
import io.resys.limaone.program.ArticleProgram.TopicHeading;
import io.resys.limaone.program.ArticleProgram.TopicLink;
import io.resys.limaone.program.ImmutableLocalizedSite;
import io.resys.limaone.program.ImmutableTopic;
import io.resys.limaone.program.ImmutableTopicBlob;
import io.resys.limaone.program.ImmutableTopicHeading;
import io.resys.limaone.program.ImmutableTopicLink;
import io.thestencil.client.spi.staticontent.support.Sha2;
import io.vertx.core.json.JsonObject;

public class SiteVisitorDefault implements SiteVisitor {
  private final List<Message> messages = new ArrayList<>();
  private final Map<String, List<TopicData>> localeTopicData = new HashMap<>();
  private final Map<String, List<LinkData>> pathLinkData = new HashMap<>();
  private final Map<String, TopicNameData> pathTopicNamesData = new HashMap<>();
  private final Map<String, TopicBlob> blobs = new HashMap<>();
  private final Map<String, TopicLink> links = new HashMap<>();
  private String imageUrl;

  @Override
  public void visitTopicData(TopicData topic) {
    var topics = localeTopicData.get(topic.getLocale());
    if(topics == null) {
      topics = new ArrayList<>();
      localeTopicData.put(topic.getLocale(), topics);
    }
    topics.add(topic);
  }
  @Override
  public void visitLinkData(LinkData link) {
    var links = pathLinkData.get(link.getPath());
    if(links == null) {
      links = new ArrayList<>();
      pathLinkData.put(link.getPath(), links);
    }
    links.add(link); 
  }
  @Override
  public void visitTopicNameData(TopicNameData names) {
    pathTopicNamesData.put(names.getPath(), names);
  }
  @Override
  public SiteVisitorOutput visit(String imageUrl) {
    this.imageUrl = imageUrl;
    final var builder = ImmutableSiteVisitorOutput.builder();
    final var initSites = this.localeTopicData.entrySet().stream()
        .map(this::visitLocale)
        .collect(Collectors.toList());
    
    
    final var allWkLinks = initSites.stream()
      .collect(Collectors.toMap(site -> site.getLocale(), site -> site.getLinks().values().stream()
          .filter(e -> Boolean.TRUE.equals(e.getWorkflow()))
          .toList()));
    
    final var sites = initSites.stream().map(site -> ImmutableLocalizedSite.builder()
        .from(site)
        .putAllWorkflowsInOtherLocales(allWkLinks)
        .build())
        .sorted((s1, s2) -> s1.getId().compareTo(s2.getId()))
        .toList();
    
    return builder.sites(sites).addAllMessage(messages).build();
  }

  private LocalizedSite visitLocale(Map.Entry<String, List<TopicData>> localization) {
    final var siteTopics = new LinkedHashMap<String, Topic>();
    final var siteBlobs = new LinkedHashMap<String, TopicBlob>();
    final var siteLinks = new LinkedHashMap<String, TopicLink>();
    final var visitedTopics = new ArrayList<String>();
    final var parents = new ArrayList<String>();
    final var locale = localization.getKey();

    
    for(final var src : localization.getValue()) {
      final var topicId = src.getPath();
      final var parent = visitTopicParent(src);
      final var name = visitTopicName(src);
      final var blob = visitTopicBlob(src);
      final var topicLinks = visitTopicLinks(topicId, locale);
      final var topicHeadings = visitTopicHeadings(src);
      
      final var topic = ImmutableTopic.builder()
          .id(topicId)
          .name(name)
          .auth(src.getAuth())
          .links(topicLinks)
          .parent(parent)
          .blob(blob)
          .headings(topicHeadings)
          .build(); 
      
      if(parent != null) {
        parents.add(parent);
      }
      
      visitedTopics.add(topicId);
      siteTopics.put(topic.getId(), topic);
      topic.getLinks().forEach(link -> siteLinks.put(link, this.links.get(link)));
      siteBlobs.put(topic.getBlob(), this.blobs.get(topic.getBlob()));
    }
    
    // add assignable links
    
    
    visitTopicLinks("_", locale).stream()
      .filter(id -> Boolean.TRUE.equals(this.links.get(id).getAssignable()))
      .forEach(id -> {
        siteLinks.put(id, this.links.get(id));
      });
    
    
    // Add missing levels
    for(String parent : parents) {
      if(visitedTopics.contains(parent)) {
        continue;
      }
      final var id = parent;
      final var name = visitTopicName(parent, locale);
      final var topicLinks = visitTopicLinks(id, locale);
      final var topic = ImmutableTopic.builder()
          .id(id)
          .name(name)
          .links(topicLinks)
          .build(); 
      
      topic.getLinks().forEach(link -> siteLinks.put(link, this.links.get(link)));
      siteTopics.put(topic.getId(), topic);
    }

    final var result = ImmutableLocalizedSite.builder()
        .id("")
        .images(imageUrl)
        .locale(locale)
        .topics(sort(siteTopics))
        .blobs(sort(siteBlobs))
        .links(sort(siteLinks))
        .build();
    final var id = Sha2.blobId(JsonObject.mapFrom(result).encode());
    return ImmutableLocalizedSite.builder().from(result).id(id).build();
  }
  
  private <K> Map<String, K> sort(Map<String, K> input) {

    final var values = new ArrayList<>(input.entrySet());
    values.sort((e1, e2) -> e1.getKey().compareTo(e2.getKey()));

    Map<String, K> result = new LinkedHashMap<>();
    for(final var entry : values) {
      result.put(entry.getKey(), entry.getValue());
    }
    
    return result;
  }
  
  private String visitTopicParent(TopicData topic) {
    String[] path = topic.getPath().split("\\/");
    if(path.length > 1) {
      return path[0];
    }
    return null;
  }
  private String visitTopicName(String path, String locale) {
    final var locales = pathTopicNamesData.get(path);
    if(locales == null) {
      this.messages.add(ImmutableMessage.builder().text("Missing localized: '" + locale + "' name for path: '" + path + "'!").build());
      return path;
    }
    final var name = locales.getLocale().get(locale);
    if(name == null || name.isBlank()) {
      this.messages.add(ImmutableMessage.builder().text("Missing localized: '" + locale + "' name for path: '" + path + "'!").build());
      return path;
    }
    return path;
  } 
  private String visitTopicName(TopicData topic) {
    for(var heading : topic.getHeadings()) {
      if(heading.getLevel() == 1 && heading.getName().length() > 1) {
        return heading.getName().substring(1).trim();
      }
    }
    
    final var locales = pathTopicNamesData.get(topic.getPath());
    if(locales == null) {
      this.messages.add(ImmutableMessage.builder().text("Missing localized: '" + topic.getLocale() + "' name for path: '" + topic.getPath() + "'!").object(topic).build());
      return topic.getPath();
    }
    final var name = locales.getLocale().get(topic.getLocale());
    if(name == null || name.isBlank()) {
      this.messages.add(ImmutableMessage.builder().text("Missing localized: '" + topic.getLocale() + "' name for path: '" + topic.getPath() + "'!").object(topic).build());
      return topic.getPath();
    }
    return name;
  } 
  private String visitTopicBlob(TopicData topic) {
    String blob = topic.getValue();
    final var id = Hashing.murmur3_128().hashString(blob, StandardCharsets.UTF_8).toString();
    this.blobs.put(id, ImmutableTopicBlob.builder().id(id).value(blob).build());
    return id;
  }   
  private List<TopicHeading> visitTopicHeadings(TopicData topic) {
    List<TopicHeading> result = new ArrayList<>();
    int index = 1;
    for(final var heading : topic.getHeadings()) {      
      result.add(ImmutableTopicHeading.builder()
        .id(String.valueOf(index++))
        .name(heading.getName())
        .order(heading.getOrder())
        .level(heading.getLevel())
        .build());
    }
    return result;
  }   
  private List<String> visitTopicLinks(String path, String locale) {
    var src = pathLinkData.get(path);
    if(src == null) {
      final var prefix = path.indexOf("_");
      if(prefix > - 1) {
        src = pathLinkData.get(path.substring(prefix + 1));
      }
    }
    
    final var links = new ArrayList<>(src == null ? Collections.emptyList() : src);
    links.addAll(pathLinkData.getOrDefault("", Collections.emptyList()));
    
    final List<String> result = new ArrayList<>();
    for(var link : links) {
      final var allLocales = link.getLocale() == null || link.getLocale().isBlank();
      final var topicLocale = link.getLocale() != null && link.getLocale().indexOf(locale) > -1;
      if(allLocales || topicLocale) {
        
        final var template = ImmutableTopicLink.builder()
          .id("template")
          .path(link.getPath())
          .global(link.getGlobal())
          .type(link.getType())
          .name(link.getName())
          .value(link.getValue())
          .assignable(link.getAssignable())
          .anon(link.getAnon())
          .workflow(link.getWorkflow())
          .startDate(link.getStartDate())
          .endDate(link.getEndDate())
          .formId(link.getFormId())
          .formName(link.getFormName())
          .formTag(link.getFormTag())
          .flowName(link.getFlowName())
          .build();
        
        final var id = hash(link);
        this.links.put(id, ImmutableTopicLink.builder().from(template).id(id).build());
        result.add(id);
      }
    }
    return result;    
  }
  
  
  private String hash(LinkData link) {
    final var hashString = new StringBuilder()
      .append(link.getPath() != null ? link.getPath() : "")
      .append("|")
      .append(link.getType() != null ? link.getType() : "")
      .append("|")
      .append(link.getName() != null ? link.getName() : "")
      .append("|")
      .append(link.getValue() != null ? link.getValue() : "")
      .append("|")
      .append(link.getGlobal() != null ? link.getGlobal() : false)
      .append("|")
      .append(link.getAssignable() != null ? link.getAssignable() : false)
      .append("|")
      .append(link.getAnon() != null ? link.getAnon() : false)
      .append("|")
      .append(link.getWorkflow() != null ? link.getWorkflow() : false)
      .append("|")
      .append(link.getFormId() != null ? link.getFormId() : "")
      .append("|")
      .append(link.getFormName() != null ? link.getFormName() : "")
      .append("|")
      .append(link.getFormTag() != null ? link.getFormTag() : "")
      .append("|")
      .append(link.getFlowName() != null ? link.getFlowName() : "");
    return Hashing.murmur3_128().hashString(hashString.toString(), StandardCharsets.UTF_8).toString();
  }
}
