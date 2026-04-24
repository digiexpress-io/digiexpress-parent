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
import java.util.List;
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
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Printout;
import io.resys.limaone.model.PrintoutPage;
import io.resys.limaone.model.PrintoutResource;
import io.resys.thena.fs.entities.Entity;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Ref;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class WorldFsFactory {
  private final Ref ref;
  private final WorldFsState worldState = new WorldFsState();
  
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
    for(final var folderName : worldState.getFolderNames()) {
      if(worldState.isFolderDirentCreated(folderName)) {
        continue;
      } else {
        createMissingFolders(folderName);  
      }
    }
    
    // compact everything together
    final ImmutableWorldFs.Builder world = ImmutableWorldFs.builder();
    for(final var folderName : worldState.getFolderNames().stream().sorted().toList().reversed()) {
      
      final var pathAndName = NodePathAndName.of(folderName);
      final var children = worldState.getChildDirents(folderName);
      
      if(folderName.isEmpty()) {
        world.addAllDirents(children);
        continue;
      }
      
      final ImmutableDirentBase folder = worldState.getFolderDirent(folderName).withChildren(children);
      worldState.putDirent(pathAndName.getPath(), folder);
    }
    return world.build();
  }
  

  private void createMissingFolders(String missing) {
    for(final var pathAndName : NodePathAndName.explode(missing)) {
      
      final var fullPath = pathAndName.getFullPath();
      final var dirent = ImmutableDirentBase.builder()
        .id(Entity.uuid().append(fullPath).build().toString())
        .fullPath(fullPath)
        .name(pathAndName.getName())
        .type(BodyType.FOLDER)
        .build();
      
      worldState.putDirent(pathAndName.getPath(), dirent);
      
    }
  }
  
  private NodePathAndName getPathAndName(NodeAndBody node) {
    return worldState.getPathAndName(node, this::parsePathAndName);
  }

  private NodePathAndName parsePathAndName(NodeAndBody node) {
    final var path = node.getValue().getNodePath();
    
    switch (node.getBodyType()) {
    case ARTICLE: {
      final var articleHierarchy = worldState.getArticleHierarchy(node);
      
      final var parents = articleHierarchy.stream()
          .limit(articleHierarchy.size() - 1)
          .map(e -> e.getBodyOfType(Article.class))
          .map(e -> e.getName())
          .toList();
      
      final var articleHierarchyPath = Optional.of(String.join("/", parents))
          .map(e -> e.isEmpty() ? "" : "/" + e)
          .get();
      
      final var articlePath = articleHierarchy.get(0).getValue().getNodePath().orElse("articles");
      final var name = node.getBodyOfType(Article.class).getName();
      
      worldState.putProps(node, n -> Props_ArticleBuilder.of(worldState, n));
      return NodePathAndName.of(articlePath + articleHierarchyPath, name);
    }
    
    case ARTICLE_PAGE: {
      final var blob = node.getValue().getTransitives().getBlob();
      final var page = blob.getBlobValue().mapTo(ArticlePage.class);
      final var localeId = page.getLocale();
      final var locale = worldState.getLocale(localeId);
      final var name = locale.getValue();
      
      final var articleNode = worldState.getNodeAndBody(page.getArticle());
      final var articlePath = getPathAndName(articleNode);

      worldState.putProps(node, n -> Props_ArticlePageBuilder.of(worldState, n));
      return NodePathAndName.of(articlePath.getPath() + "/" + articlePath.getName() + "/pages", name);
    }
    
    case ARTICLE_LINK: {
      final ArticleLink link = worldState.getBodyOfType(node);
      final var name = link.getValue();
      worldState.putProps(node, n -> Props_LinkBuilder.of(worldState, n));
      return NodePathAndName.of(path.orElse("links"), name);
    }
    
    case ARTICLE_WORKFLOW: {
      final ArticleWorkflow workflow = worldState.getBodyOfType(node);
      final var name = workflow.getValue();
      worldState.putProps(node, n -> Props_ArticleWorkflowBuilder.of(worldState, n));
      return NodePathAndName.of(path.orElse("workflows"), name);
    }
    
    case ARTICLE_TEMPLATE: {
      final ArticleTemplate template = worldState.getBodyOfType(node);
      final var name = template.getName();
      return NodePathAndName.of(path.orElse("templates"), name);
    }
    
    case LOCALE: {
      final Locale locale = worldState.getBodyOfType(node);
      final var name = locale.getValue();
      worldState.putProps(node, n -> Props_LocaleBuilder.of(worldState, n));
      return NodePathAndName.of(path.orElse("locales"), name);
    }
    
    case PRINTOUT: {
      final Printout printout = worldState.getBodyOfType(node);
      final var name = printout.getServiceName();
      worldState.putProps(node, n -> Props_PrintoutBuilder.of(worldState, n));
      return NodePathAndName.of(path.orElse("printouts"), name);
    }
    
    case PRINTOUT_PAGE: {
      final PrintoutPage printout = worldState.getBodyOfType(node);
      final var name = printout.getLocaleId();
      worldState.putProps(node, n -> Props_PrintoutPageBuilder.of(worldState, n));
      return NodePathAndName.of(path.orElse("printout-templates"), name);
    }
    
    case PRINTOUT_RESOURCE: {
      final PrintoutResource printoutResource = worldState.getBodyOfType(node);
      final var name = printoutResource.getResourceName();
      return NodePathAndName.of(path.orElse("printout-resources"), name);
    }
    
    case DECISION_TABLE: {
      final DecisionTable decisionTable = worldState.getBodyOfType(node);    
      final var name = decisionTable.getName();
      return NodePathAndName.of(path.orElse("decision-table"), name);
    }
    
    case FLOW_TASK: {
      final FlowTask flowTask = worldState.getBodyOfType(node); 
      final var name = flowTask.getTaskName();
      return NodePathAndName.of(path.orElse("flow-tasks"), name);
    }
    
    case FLOW: {
      final Flow flow = worldState.getBodyOfType(node);
      final var name = flow.getFlowName();
      worldState.putProps(node, n -> Props_FlowBuilder.of(worldState, n));
      return NodePathAndName.of(path.orElse("flows"), name);
    }

    case FOLDER: {
      return NodePathAndName.of(path.orElse(""), node.getValue().getNodeName());
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
      .props(worldState.getProps(node.getObjectId()))
      .build();

    worldState.putDirent(pathAndName.getPath(), dirent);
    
    return dirent;
  }
  
  private Optional<NodeAndBody> createNodeAndBodyType(Node node) {
    final var nodeType = NodeAndBody.of(node);
    if(nodeType.isEmpty()) {
      return Optional.empty();
    }
    
    final var bodyType = nodeType.get().getBodyType();
    if( bodyType == BodyType.DEPLOYMENT || 
        bodyType == BodyType.DIALOB_FORM || 
        bodyType == BodyType.UNKNOWN) {
      return Optional.empty();
    }
    worldState.putNodeAndBody(nodeType.get());
    return nodeType;
  }
}
