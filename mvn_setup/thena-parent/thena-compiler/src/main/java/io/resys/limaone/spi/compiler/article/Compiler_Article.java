package io.resys.limaone.spi.compiler.article;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.Article_AST.Link;
import io.resys.limaone.ast.Article_AST.Markdown;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.program.ArticleProgram.LocalizedSite;
import io.resys.limaone.program.ArticleProgram.Topic;
import io.resys.limaone.program.ArticleProgram.TopicBlob;
import io.resys.limaone.program.ArticleProgram.TopicLink;
import io.resys.limaone.program.ImmutableLocalizedSite;
import io.resys.limaone.program.ImmutableTopic;
import io.resys.limaone.program.ImmutableTopicLink;
import io.resys.limaone.spi.compiler.CompilableUnit;
import io.resys.limaone.spi.compiler.article.Deltas.TopicData;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Compiler_Article implements CompilableUnit {

  private final AST_Parser parser;
  private final ModelWorld world;
  public static String LINK_TYPE_WORKFLOW = "workflow";

  private final Map<String, List<TopicData>> localeTopicData = new HashMap<>();
  private final Map<String, List<Link>> pathLinkData = new HashMap<>();
  private String imageUrl;
  

  @Override
  public OpenProgram compile(NewArtifact resolution) {
    final var articleAST = parser.parseArticles().world(world).parse();
    
    
    articleAST.getValues().forEach(this::visitMarkdown);
    articleAST.getLinks().forEach(this::visitLink);
    
    
    final var initSites = this.localeTopicData.keySet().stream().sorted().
      map(locale -> visitLocale(locale, this.localeTopicData.get(locale)))
      .collect(Collectors.toList());
    

    
    
    // ArticleProgram

    
    // 1. target date when running, 2. AUTH when running
    /**
     *         boolean requiredAuth = Boolean.TRUE.equals(topic.getAuth());
        boolean isUserAuthenticated = this.auth;
        if(requiredAuth) {
          return isUserAuthenticated;  
        }
     */
    
    return null;
  }

  

  private LocalizedSite visitLocale(String locale, List<TopicData> localeTopics) {
    localeTopics.sort((e1, e2) -> e1.getFullPath().compareTo(e2.getFullPath()));
    
    final var siteTopics = new LinkedHashMap<String, Topic>();
    final var siteBlobs = new LinkedHashMap<String, TopicBlob>();
    final var siteLinks = new LinkedHashMap<String, TopicLink>();
    final var visitedTopics = new ArrayList<String>();
    final var parents = new ArrayList<String>();

    
    for(final var src : localeTopics) {
      final var topicId = src.getPath();
      final var parent = src.getParentTopic();
      final var name = src.getTopicName();
      
      final var blob = src.getTopicBlob();
      final var topicLinks = getTopicLinks(topicId, locale);
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
      
      if(parent != null) {
        parents.add(parent);
      }
      
      visitedTopics.add(topicId);
      siteTopics.put(topic.getId(), topic);
      topicLinks.forEach(link -> siteLinks.put(link.getId(), link));
      siteBlobs.put(topic.getBlob(), blob);
    }
    
    // add assignable links
    getTopicLinks("_", locale).stream()
      .filter(link -> Boolean.TRUE.equals(link.getAssignable()))
      .forEach(link -> siteLinks.put(link.getId(), link));
    
    
    // Add missing levels
    for(final String parent : parents) {
      if(visitedTopics.contains(parent)) {
        continue;
      }
      final var id = parent;
      final var name = parent;
      final var topicLinks = getTopicLinks(id, locale);
      final var topic = ImmutableTopic.builder()
          .id(id)
          .name(name)
          .links(topicLinks.stream().map(e -> e.getId()).toList())
          .build(); 
      
      topicLinks.forEach(link -> siteLinks.put(link.getId(), link));
      siteTopics.put(topic.getId(), topic);
    }

    final var result = ImmutableLocalizedSite.builder()
        .id("")
        .images(imageUrl)
        .locale(locale)
        .topics(siteTopics)
        .blobs(siteBlobs)
        .links(siteLinks)
        .build();
    final var id = Sha2.blobId(JsonObject.mapFrom(result).encode());
    return ImmutableLocalizedSite.builder().from(result).id(id).build();
  }

  private void visitMarkdown(Markdown md) {
    final TopicData topic = ImmutableTopicData.builder()
        .auth(md.getAuth())
        .path(md.getPath())
        .locale(md.getLocale())
        .headings(md.getHeadings())
        .images(md.getImages())
        .value(md.getValue())
        .build();
    
    var topics = localeTopicData.get(topic.getLocale());
    if(topics == null) {
      topics = new ArrayList<>();
      localeTopicData.put(topic.getLocale(), topics);
    }
    topics.add(topic);
  }
  

  private void visitLink(Link src) {
    var links = pathLinkData.get(src.getPath());
    if(links == null) {
      links = new ArrayList<>();
      pathLinkData.put(src.getPath(), links);
    }
    links.add(src); 
  }
  
  private List<TopicLink> getTopicLinks(String path, String locale) {
    var src = pathLinkData.get(path);
    if(src == null) {
      final var prefix = path.indexOf("_");
      if(prefix > - 1) {
        src = pathLinkData.get(path.substring(prefix + 1));
      }
    }
    
    final var links = new ArrayList<>(src == null ? Collections.emptyList() : src);
    links.addAll(pathLinkData.getOrDefault("", Collections.emptyList()));
    
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

        result.add(template);
      }
    }
    return result;    
  }

}
