package io.resys.limaone.spi.compiler.article;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ComparisonChain;
import com.google.common.hash.Hashing;

import io.resys.limaone.ast.Article_AST.Link;
import io.resys.limaone.program.ArticleProgram.Topic;
import io.resys.limaone.program.ArticleProgram.TopicBlob;
import io.resys.limaone.program.ArticleProgram.TopicLink;
import io.resys.limaone.program.ImmutableLocalizedSite;
import io.resys.limaone.program.ImmutableTopic;
import io.resys.limaone.program.ImmutableTopicLink;
import io.resys.limaone.spi.compiler.article.Deltas.TopicData;

public class LocalizedSiteBuilder {
  private final Map<String, List<Link>> pathLinkData;
  private final Map<String, Topic> siteTopics = new LinkedHashMap<String, Topic>();
  private final Map<String, TopicBlob> siteBlobs = new LinkedHashMap<String, TopicBlob>();
  private final Map<String, TopicLink> siteLinks = new LinkedHashMap<String, TopicLink>();
  private final List<String> parents = new ArrayList<String>();
  private final String locale;
  private final String imageUrl = "{imageUrl}";
  private final StringBuilder hashBuilder = new StringBuilder();
  
  public LocalizedSiteBuilder(String locale, Map<String, List<Link>> pathLinkData) {
    this.locale = locale;
    this.pathLinkData = pathLinkData;
    // Initialize hash with locale
    this.hashBuilder.append("locale:").append(locale).append("|");
  }

  public void addTopic(TopicData src) {
    final var topicId = src.getPath();
    final var parent = src.getParentTopic();
    final var name = src.getTopicName();
    
    final var blob = src.getTopicBlob();
    final var topicLinks = createTopicLinks(topicId);
    final var topicHeadings = src.getTopicHeadings();
    final var links = topicLinks.stream().map(e -> e.getId()).sorted().toList();
    
    final var topic = ImmutableTopic.builder()
        .id(topicId)
        .name(name)
        .auth(src.getAuth())
        .links(links)
        .parent(parent)
        .blob(blob.getId())
        .headings(topicHeadings)
        .build(); 
    
    // Add to hash incrementally
    this.hashBuilder.append("topic:")
        .append(topicId).append("|")
        .append(name != null ? name : "").append("|")
        .append(src.getAuth() != null ? src.getAuth() : false).append("|")
        .append(parent != null ? parent : "").append("|")
        .append(blob.getId()).append("|");
    
    // Add sorted link IDs to hash
    links.forEach(linkId -> hashBuilder.append("link:").append(linkId).append(","));
    this.hashBuilder.append("|");
    
    // Add headings in order
    topicHeadings.forEach(heading -> 
        hashBuilder.append("heading:").append(heading.getId())
            .append("-").append(heading.getOrder()).append(","));
    hashBuilder.append("||");
    
    if(parent != null) {
      parents.add(parent);
    }
    
    siteTopics.put(topic.getId(), topic);
    siteBlobs.put(topic.getBlob(), blob);
    topicLinks.forEach(link -> siteLinks.put(link.getId(), link));
  }
  
  public ImmutableLocalizedSite build() {
    // add assignable links
    hashBuilder.append("assignable:");
    createTopicLinks("_").stream()
      .filter(link -> Boolean.TRUE.equals(link.getAssignable()))
      .forEach(link -> {
        siteLinks.put(link.getId(), link);
        hashBuilder.append(link.getId()).append(",");
      });
    hashBuilder.append("|");
    
    // Add missing levels
    hashBuilder.append("missing:");
    for(final String parent : parents) {
      if(siteTopics.containsKey(parent)) {
        continue;
      }
      final var id = parent;
      final var name = parent;
      final var topicLinks = createTopicLinks(id);
      final var topic = ImmutableTopic.builder()
          .id(id)
          .name(name)
          .links(topicLinks.stream().map(e -> e.getId()).toList())
          .build(); 
      
      // Add missing parent to hash
      hashBuilder.append("parent:").append(id).append("-").append(name).append(",");
      
      topicLinks.forEach(link -> siteLinks.put(link.getId(), link));
      siteTopics.put(topic.getId(), topic);
    }
    hashBuilder.append("|");
    
    // Calculate final hash
    String calculatedId = Hashing.murmur3_128()
        .hashString(hashBuilder.toString(), StandardCharsets.UTF_8)
        .toString();
    
    return ImmutableLocalizedSite.builder()
        .id(calculatedId)
        .images(imageUrl)
        .locale(locale)
        .topics(siteTopics)
        .blobs(siteBlobs)
        .links(siteLinks)
        .build();
  }
  
  
  private List<TopicLink> createTopicLinks(String path) {
    var src = pathLinkData.get(path);
    if(src == null) {
      final var prefix = path.indexOf("_");
      if(prefix > - 1) {
        src = pathLinkData.get(path.substring(prefix + 1));
      }
    }
    
    final var links = new ArrayList<>(src == null ? Collections.emptyList() : src);
    links.addAll(pathLinkData.getOrDefault("", Collections.emptyList()));
    links.sort((a, b) -> ComparisonChain.start()
        .compare(a.getPath(), b.getPath())
        .compare(a.getDesc() != null ? a.getDesc() : "", b.getDesc() != null ? b.getDesc() : "")
        .result());
    
    final List<TopicLink> result = new ArrayList<>();
    for(final Link link : links) {
      final var topicLocale = link.getLocale().indexOf(locale) > -1;
      
      if(topicLocale) {
        final var template = ImmutableTopicLink.builder()
          .id(link.getId())
          .path(link.getPath())
          .global(link.getGlobal())
          .type(link.getType())
          .name(link.getDesc())
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

        // Add link details to hash
        hashBuilder.append("topicLink:")
            .append(template.getId()).append("|")
            .append(template.getPath() != null ? template.getPath() : "").append("|")
            .append(template.getType() != null ? template.getType() : "").append("|")
            .append(template.getName() != null ? template.getName() : "").append("|")
            .append(template.getValue() != null ? template.getValue() : "").append("|")
            .append(template.getGlobal() != null ? template.getGlobal() : false).append("|")
            .append(template.getWorkflow() != null ? template.getWorkflow() : false).append("||");

        result.add(template);
      }
    }
    return result;    
  }
}