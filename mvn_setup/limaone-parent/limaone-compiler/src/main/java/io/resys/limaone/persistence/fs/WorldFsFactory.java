package io.resys.limaone.persistence.fs;

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
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model.Body;
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
  
  private final Map<String, NodePathAndName> nodePathAndName_by_object_id = new HashMap<>();
  private final Map<String, NodeAndBody> nodes_by_object_id = new HashMap<>();
  private final Map<String, ImmutableDirentBase> dirents_by_fullpath = new HashMap<>();
  private final Map<String, List<ImmutableDirentBase>> dirents_grouped_by_path = new HashMap<>();
  
  
  
  public WorldFs create() {
    final List<NodeAndBody> parseLater = new ArrayList<>();
    final var nodes = ref.getTransitives().getTree().getTreeNodes();
    for(final var node : nodes) {
      
      final var optionalNode = createNodeAndBodyType(node);
      if(optionalNode.isEmpty()) {
        continue;
      }
      
      final var nodeType = optionalNode.get();
      // Root types to load before anything else
      if(nodeType.getBodyType() == BodyType.LOCALE || nodeType.getBodyType() == BodyType.FOLDER) {
        createAnyDirent(nodeType);    
      } else {
        parseLater.add(nodeType);
      }
    }

    for(final var later : parseLater) {
      createAnyDirent(later);
    }
    
    // create folders ...
    //for() {
      
    //}
    
    return world.addAllDirents(dirents_by_fullpath.values()).build();
  }
  
  

  
  private NodePathAndName getPathAndName(NodeAndBody node) {
    if(nodePathAndName_by_object_id.containsKey(node.getObjectId())) {
      return nodePathAndName_by_object_id.get(node.getObjectId());
    }
    final var next = parsePathAndName(node);
    nodePathAndName_by_object_id.put(node.getObjectId(), next);
    return next;
  }


  private NodePathAndName parsePathAndName(NodeAndBody node) {
    final var path = node.getValue().getNodePath();
    
    switch (node.getBodyType()) {
    case ARTICLE: {
      final var articleHierarchy = getArticleHierarchy(node);
      
      final var parents = articleHierarchy.stream()
          .limit(articleHierarchy.size() - 1)
          .map(e -> e.getBodyOfType(Article.class))
          .map(e -> e.getName())
          .toList();
      
      final var articleHierarchyPath = String.join("/", parents);
      final var articlePath = articleHierarchy.get(0).getValue().getNodePath().orElse("articles");
      final var name = node.getBodyOfType(Article.class).getName();
      return NodePathAndName.of(articlePath + "/" + articleHierarchyPath, name);
    }
    
    case ARTICLE_PAGE: {
      final var blob = node.getValue().getTransitives().getBlob();
      final var page = blob.getBlobValue().mapTo(ArticlePage.class);
      final var localeId = page.getLocale();
      final var locale = getLocale(localeId);
      final var name = locale.getValue();
      
      final var articleNode = nodes_by_object_id.get(page.getArticle());
      final var articlePath = getPathAndName(articleNode);

      return NodePathAndName.of(articlePath.getPath() + "/" + articlePath.getName() + "/pages", name);

      //return "articles/" + article.getName() + "/pages";
    }
    case ARTICLE_LINK: {
      final ArticleLink link = getBodyOfType(node);
      final var name = link.getValue();
      return NodePathAndName.of(path.orElse("links"), name);
    }
    case ARTICLE_TEMPLATE: {
      final ArticleTemplate template = getBodyOfType(node);
      final var name = template.getName();
      return NodePathAndName.of(path.orElse("templates"), name);
    }
    case ARTICLE_WORKFLOW: {
      final ArticleWorkflow workflow = getBodyOfType(node);
      final var name = workflow.getValue();
      return NodePathAndName.of(path.orElse("workflows"), name);
    }
    case LOCALE: {
      final Locale locale = getBodyOfType(node);
      final var name = locale.getValue();
      return NodePathAndName.of(path.orElse("locales"), name);
    }
    case PRINTOUT: {
      final Printout printout = getBodyOfType(node);
      final var name = printout.getServiceName();
      return NodePathAndName.of(path.orElse("printouts"), name);
    }
    case PRINTOUT_PAGE: {
      final Printout printout = getBodyOfType(node);
      final var name = printout.getServiceName();
      return NodePathAndName.of(path.orElse("printout-templates"), name);
    }
    case DECISION_TABLE: {
      final DecisionTable decisionTable = getBodyOfType(node);    
      final var name = decisionTable.getName();
      return NodePathAndName.of(path.orElse("decision-table"), name);
    }
    default: throw new IllegalArgumentException("Not implemented: " + node);
    }
  }
  private ImmutableDirentBase createAnyDirent(NodeAndBody node) {
    final NodePathAndName pathAndName = getPathAndName(node);
    
    final var dirent = ImmutableDirentBase.builder()
      .id(node.getValue().getObjectId())
      .fullPath(pathAndName.getPath() + "/" + pathAndName.getName())
      .name(pathAndName.getName())
      .type(node.getBodyType())
      .build();
    
    final var path = pathAndName.getPath();
    if(!dirents_grouped_by_path.containsKey(path)) {
      dirents_grouped_by_path.put(path, new ArrayList<>());
    }
    dirents_grouped_by_path.get(path).add(dirent);
    dirents_by_fullpath.put(dirent.getFullPath(), dirent);
    
    return dirent;
  }
  
  private Optional<NodeAndBody> createNodeAndBodyType(Node node) {
    final var nodeType = NodeAndBody.of(node);
    if(nodeType.isEmpty()) {
      return Optional.empty();
    }
    nodes_by_object_id.put(nodeType.get().getValue().getObjectId(), nodeType.get());
    return nodeType;
  } 
  
  private Locale getLocale(String localeId) {
    return (Locale) nodes_by_object_id.get(localeId).getBody().get(); 
  }
  
  @SuppressWarnings("unchecked")
  private <T extends Body> T getBodyOfType(NodeAndBody node) {
    return (T) nodes_by_object_id.get(node.getObjectId()).getBody().get(); 
  }
  
  private List<NodeAndBody> getArticleHierarchy(NodeAndBody node) {
    Article article = node.getBodyOfType(); 
    final List<NodeAndBody> result = new ArrayList<>(); 
    
    // add self
    result.add(node);
    
    if(article.getParentId() == null) {
      return result;
    }
    
    // add parents
    while(article != null) {
      if(article.getParentId() == null) {
        break;
      }
      final var nextNode = nodes_by_object_id.get(article.getParentId());
      result.add(nextNode);
      article = nextNode.getBodyOfType();
      
    }
    return result.reversed();
  }
}
