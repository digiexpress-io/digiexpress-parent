package io.resys.limaone.persistence.world;

/*-
 * #%L
 * limaone-compiler
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.resys.limaone.fs.ImmutableDirentBase;
import io.resys.limaone.fs.ImmutableWorldFs;
import io.resys.limaone.fs.WorldFs;
import io.resys.limaone.model.Article;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.ArticleTemplate;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Printout;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Ref;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class WorldFsFactory {
  private final Ref ref;
  private final ImmutableWorldFs.Builder world = ImmutableWorldFs.builder();
  private final List<Node> parseLater = new ArrayList<>();
  private final Map<String, Article> article_cache = new HashMap<>();
  private final Map<String, Locale> locale_cache = new HashMap<>();
  private final Map<String, ArticleLink> link_cache = new HashMap<>();
  private final Map<String, ArticleTemplate> template_cache = new HashMap<>();
  private final Map<String, ArticleWorkflow> workflow_cache = new HashMap<>();
  private final Map<String, Printout> printout_cache = new HashMap<>();
  private final Map<String, ImmutableDirentBase> dirent_cache = new HashMap<>();
  private final Map<String, List<ImmutableDirentBase>> dirents_grouped_by_path = new HashMap<>();
  
  public WorldFs create() {
    final var nodes = ref.getTransitives().getTree().getTreeNodes();
    for(final var node : nodes) {
      final var bodyType = getBodyType(node);
      if(bodyType.isEmpty()) {
        continue;
      }
      
      final var dirent = visitFirstLoadNode(node, bodyType.get());
      if(dirent.isPresent()) {
        addDirent(dirent.get(), node, bodyType.get());
      } else {
        parseLater.add(node);
      }
    }
     
    for(final var later : parseLater) {
      final var bodyType = getBodyType(later).orElseThrow();
      final var dirent = visitSecondLoadNode(later, bodyType);
      addDirent(dirent, later, bodyType);
    }
    
    return world.addAllDirents(dirent_cache.values()).build();
  }
  
  private void addDirent(ImmutableDirentBase dirent, Node node, BodyType bodyType) {
    final var path = getPath(node, bodyType);
    
    if(!dirents_grouped_by_path.containsKey(path)) {
      dirents_grouped_by_path.put(path, new ArrayList<>());
    }
    
    dirents_grouped_by_path.get(path).add(dirent);
    dirent_cache.put(dirent.getFullPath(), dirent);
  }
  
  private String getPath(Node node, BodyType bodyType) {
    final var path = node.getNodePath().orElse(null);
    if(path != null) {
      return path;
    }
    
    switch (bodyType) {
    case ARTICLE: {
      final var parents = getArticleHierarchy(getArticle(node.getObjectId()))
          .stream().map(e -> e.getName())
          .toList();
      
      final var articles = String.join("/", parents);
      return "articles/" + articles;
    }
    default: return "/";
    }
  }
  
  private Optional<ImmutableDirentBase> visitFirstLoadNode(Node node, BodyType bodyType) {

    switch (bodyType) {
      case LOCALE: return Optional.of(createLocaleDirent(node, bodyType));
      case ARTICLE: return createArticleDirent(node, bodyType);
      case ARTICLE_PAGE: return Optional.empty();
      case ARTICLE_LINK: return Optional.empty();
      case ARTICLE_TEMPLATE: return Optional.empty();
      case ARTICLE_WORKFLOW: return Optional.empty();
      case PRINTOUT: return Optional.empty();
      default: return Optional.of(createAnyDirent(node, bodyType));
    }
  }
  
  private ImmutableDirentBase visitSecondLoadNode(Node node, BodyType bodyType) {

    switch (bodyType) {
      case ARTICLE: return createChildArticleDirent(node, bodyType);
      case ARTICLE_PAGE: return createArticlePageDirent(node, bodyType);
      case ARTICLE_LINK: return createArticleLinkDirent(node, bodyType);
      case ARTICLE_WORKFLOW: return createArticleWorkflowDirent(node, bodyType);
      case ARTICLE_TEMPLATE: return createArticleTemplateDirent(node, bodyType);
      case PRINTOUT: return createPrintoutDirent(node, bodyType);
      default: throw new IllegalArgumentException(bodyType + " is not recognized as valid node in second load!");
    }
  }
  

  private ImmutableDirentBase createPrintoutDirent(Node node, BodyType bodyType) {
    final var blob = node.getTransitives().getBlob();
    final var printout = blob.getBlobValue().mapTo(Printout.class);
    final var path = node.getNodePath().orElse("printouts");
    printout_cache.put(node.getObjectId(), printout);
    
    final var name = printout.getServiceName();
    final var dirent = ImmutableDirentBase.builder()
        .id(node.getObjectId())
        .fullPath(path + "/" + name)
        .name(name)
        .type(bodyType)
        .build();
      return dirent;
  }
  
  private ImmutableDirentBase createArticleWorkflowDirent(Node node, BodyType bodyType) {
    final var blob = node.getTransitives().getBlob();
    final var workflow = blob.getBlobValue().mapTo(ArticleWorkflow.class);
    final var path = node.getNodePath().orElse("workflows");
    workflow_cache.put(node.getObjectId(), workflow);
    
    final var name = workflow.getValue();
    final var dirent = ImmutableDirentBase.builder()
        .id(node.getObjectId())
        .fullPath(path + "/" + name)
        .name(name)
        .type(bodyType)
        .build();
      return dirent;
  }
  
  private ImmutableDirentBase createArticleTemplateDirent(Node node, BodyType bodyType) {
    final var blob = node.getTransitives().getBlob();
    final var template = blob.getBlobValue().mapTo(ArticleTemplate.class);
    final var path = node.getNodePath().orElse("templates");
    template_cache.put(node.getObjectId(), template);
    
    final var name = template.getName();
    final var dirent = ImmutableDirentBase.builder()
        .id(node.getObjectId())
        .fullPath(path + "/" + name)
        .name(name)
        .type(bodyType)
        .build();
      return dirent;
  }
  
  private ImmutableDirentBase createArticleLinkDirent(Node node, BodyType bodyType) {
    final var blob = node.getTransitives().getBlob();
    final var link = blob.getBlobValue().mapTo(ArticleLink.class);
    final var name = link.getValue();
    link_cache.put(node.getObjectId(), link);
    
    final var path = node.getNodePath().orElse("links");
    final var dirent = ImmutableDirentBase.builder()
      .id(node.getObjectId())
      .fullPath(path + "/" + name)
      .name(name)
      .type(bodyType)
      .build();
    return dirent;
  }
  
  private ImmutableDirentBase createArticlePageDirent(Node node, BodyType bodyType) {
    final var blob = node.getTransitives().getBlob();
    final var page = blob.getBlobValue().mapTo(ArticlePage.class);
    final var articleId = page.getArticle();
    final var localeId = page.getLocale();
    final var article = getArticle(articleId);
    final var locale = getLocale(localeId);
 
    final var name = locale.getValue();
    final var dirent = ImmutableDirentBase.builder()
        .id(node.getObjectId())
        .fullPath(article.getName() + "/" + name)
        .name(name)
        .type(bodyType)
        .build();
      return dirent;
  }
  
  private ImmutableDirentBase createLocaleDirent(Node node, BodyType bodyType) {
    final var name = node.getNodeName();
    final var dirent = ImmutableDirentBase.builder()
        .id(node.getObjectId())
        .fullPath("locales/" + name)
        .name(name)
        .type(bodyType)
        .build();
    
    final var blob = node.getTransitives().getBlob();
    final var locale = blob.getBlobValue().mapTo(Locale.class);
    locale_cache.put(node.getObjectId(), locale);
    return dirent;
  }
  
  private Optional<ImmutableDirentBase> createArticleDirent(Node node, BodyType bodyType) {
    final var blob = node.getTransitives().getBlob();
    final var article = blob.getBlobValue().mapTo(Article.class);
    article_cache.put(node.getObjectId(), article);
    
    if(article.getParentId() != null) {
      return Optional.empty();
    }
    
    final var dirent = ImmutableDirentBase.builder()
      .id(node.getObjectId())
      .fullPath(getPath(node, bodyType) + "/" + article.getName())
      .name(node.getNodeName())
      .type(bodyType)
      .build();
    return Optional.ofNullable(dirent);
  }
  
  private ImmutableDirentBase createChildArticleDirent(Node node, BodyType bodyType) {
    final var article = article_cache.get(node.getObjectId());
    
    if(article.getParentId() == null) {
      throw new IllegalArgumentException("Can only load child articles!");
    }
    
    final var dirent = ImmutableDirentBase.builder()
      .id(node.getObjectId())
      .fullPath(getPath(node, bodyType) + "/" + article.getName())
      .name(node.getNodeName())
      .type(bodyType)
      .build();
    return dirent;
  }
  
  private ImmutableDirentBase createAnyDirent(Node node, BodyType bodyType) {
    final var dirent = ImmutableDirentBase.builder()
      .id(node.getObjectId())
      .fullPath(node.getFullPath())
      .name(node.getNodeName())
      .type(bodyType)
      .build();
    return dirent;
  }
  
  /**
   * resolve dirent type based on blob or other props
   */
  private Optional<BodyType> getBodyType(Node node) {
    if(node.getBlobId().isEmpty()) {
      return Optional.of(BodyType.FOLDER);
    }
    
    final var blob = node.getTransitives().getBlob();    
    try {
      final var type = BodyType.valueOf(blob.getBlobType());
      return Optional.of(type);
    } 
    catch(Exception e) {
      log.warn("Failed to get node type from blob: {}, message: {}", node.getNodeName(), e.getMessage());
      return Optional.empty();
    }
  }
  
  private Article getArticle(String articleId) {
    return article_cache.get(articleId); 
  }
  
  private Locale getLocale(String localeId) {
    return locale_cache.get(localeId); 
  }
  
  private List<Article> getArticleHierarchy(Article article) {
    if(article.getParentId() == null) {
      return Collections.emptyList();
    }
    final List<Article> result = new ArrayList<>(); 
    
    while(article != null) {
      if(article.getParentId() == null) {
        break;
      }
      article = getArticle(article.getParentId());
      result.add(article);
    }
    
    return result.reversed();
  }
}
