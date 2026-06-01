package io.digiexpress.eveli.client.web.resources.assets;

/*-
 * #%L
 * eveli-client
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

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.authoring.DebugAny.DebugAnyProps;
import io.resys.limaone.authoring.DebugAny.DebugResult;
import io.resys.limaone.authoring.ModifyArticle.ModifyArticleProps;
import io.resys.limaone.authoring.ModifyArticleLink.ModifyArticleLinkProps;
import io.resys.limaone.authoring.ModifyArticlePage.ModifyArticlePageProps;
import io.resys.limaone.authoring.ModifyArticleWorkflow.ModifyArticleWorkflowProps;
import io.resys.limaone.authoring.ModifyDecisionTable.ModifyDecisionTableProps;
import io.resys.limaone.authoring.ModifyPrintout.ModifyPrintoutProps;
import io.resys.limaone.authoring.ModifyPrintoutResource.ModifyPrintoutResourceProps;
import io.resys.limaone.authoring.NewPrintoutPage.NewPrintoutPageProps;
import io.resys.limaone.authoring.NewPrintoutResource.NewPrintoutResourceProps;
import io.resys.limaone.model.PrintoutPage;
import io.resys.limaone.model.PrintoutResource;
import io.resys.limaone.authoring.ModifyFlow.ModifyFlowProps;
import io.resys.limaone.authoring.ModifyFlowTask.ModifyFlowTaskProps;
import io.resys.limaone.authoring.ModifyLocale.ModifyLocaleProps;
import io.resys.limaone.authoring.NewArticle.NewArticleProps;
import io.resys.limaone.authoring.NewArticleLink.NewArticleLinkProps;
import io.resys.limaone.authoring.NewArticlePage.NewArticlePageProps;
import io.resys.limaone.authoring.NewArticleWorkflow.NewArticleWorkflowProps;
import io.resys.limaone.authoring.NewDecisionTable.NewDecisionTableProps;
import io.resys.limaone.authoring.NewFlow.NewFlowProps;
import io.resys.limaone.authoring.NewFlowTask.NewFlowTaskProps;
import io.resys.limaone.authoring.NewLocale.NewLocaleProps;
import io.resys.limaone.authoring.NewPrintout.NewPrintoutProps;
import io.resys.limaone.fs.WorldFs;
import io.resys.limaone.fs.WorldFsBody;
import io.resys.limaone.fs.WorldFsBody.WrenchAstBodyChange;
import io.resys.limaone.model.Article;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Printout;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/worker/rest/api/assets/fs")
@Slf4j
@RequiredArgsConstructor
public class AssetsFsController {
  
  private final Authoring authoring; 

  
  @GetMapping("dirents")
  public Uni<WorldFs> findAllDirents() {
    return authoring.worldFsQuery().findAll();
  }
  
  @GetMapping("dirents/{id}/bodies/{bodyType}")
  public Uni<WorldFsBody> getDirentBody(
      @PathVariable("id") String id, 
      @PathVariable("bodyType") BodyType bodyType) {
    
    return authoring.worldFsBodyQuery().id(id).bodyType(bodyType).getOne();
  }
  
  
  @PostMapping(path = "/debugs", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<DebugResult> debug(@RequestBody DebugAnyProps debug) {
    return authoring.debugModel().debugAny().props(debug).build();
  }


  @PostMapping("dirents/{id}/bodies/{bodyType}/transient-changes")
  public Uni<WorldFsBody> getDirentBody(
      @RequestBody WrenchAstBodyChange transientChanges,
      @PathVariable("id") String id, 
      @PathVariable("bodyType") BodyType bodyType
  ) {
    return authoring.worldFsBodyQuery().id(id).bodyType(bodyType).withTransientChanges(transientChanges).getOne();
  }
  
  @PutMapping("dirents/articles/{id}")
  public Uni<Model<Article>> updateArticle(
      @PathVariable("id") String id,
      @RequestBody ModifyArticleProps body)
  {
    return authoring.modifyModel().modifyArticle().props(body).build();
  }

  @PutMapping("dirents/links/{id}")
  public Uni<Model<ArticleLink>> udpateLink(
      @PathVariable("id") String id,
      @RequestBody ModifyArticleLinkProps body)
  {
    return authoring.modifyModel().modifyArticleLink().props(body).build();
  }

  @PutMapping("dirents/article-page/{id}")
  public Uni<Model<ArticlePage>> updatePage(
      @PathVariable("id") String id,
      @RequestBody ModifyArticlePageProps body)
  {
    return authoring.modifyModel().modifyArticlePage().props(body).build();
  }

  @PutMapping("dirents/article-workflows/{id}")
  public Uni<Model<ArticleWorkflow>> updateArticleWorkflow(
      @PathVariable("id") String id,
      @RequestBody ModifyArticleWorkflowProps body)
  {
    return authoring.modifyModel().modifyArticleWorkflow().props(body).build();
  }

  @PutMapping("dirents/decision-tables/{id}")
  public Uni<Model<DecisionTable>> updateDecisionTable(
      @PathVariable("id") String id,
      @RequestBody ModifyDecisionTableProps body)
  {
    return authoring.modifyModel().modifyDecisionTable().props(body).build();
  }

  @PutMapping("dirents/flows/{id}")
  public Uni<Model<Flow>> updateFlow(
      @PathVariable("id") String id,
      @RequestBody ModifyFlowProps body)
  {
    return authoring.modifyModel().modifyFlow().props(body).build();
  }

  @PutMapping("dirents/flow-tasks/{id}")
  public Uni<Model<FlowTask>> updateFlowTask(
      @PathVariable("id") String id,
      @RequestBody ModifyFlowTaskProps body)
  {
    return authoring.modifyModel().modifyFlowTask().props(body).build();
  }

  @PutMapping("dirents/locales/{id}")
  public Uni<Model<Locale>> updateLocale(
      @PathVariable("id") String id,
      @RequestBody ModifyLocaleProps body)
  {
    return authoring.modifyModel().modifyLocale().props(body).build();
  }
  
  @PostMapping("dirents/articles")
  public Uni<Model<Article>> createArticle(@RequestBody NewArticleProps body) {
    return authoring.newModel().newArticle().props(body).build();
  }

  @PostMapping("dirents/article-page")
  public Uni<Model<ArticlePage>> createArticlePage(@RequestBody NewArticlePageProps body) {
    return authoring.newModel().newArticlePage().props(body).build();
  }

  @PostMapping("dirents/links")
  public Uni<Model<ArticleLink>> createLink(@RequestBody NewArticleLinkProps body) {
    return authoring.newModel().newArticleLink().props(body).build();
  }
  
  @PostMapping("dirents/locales")
   public Uni<Model<Locale>> createLocale(@RequestBody NewLocaleProps body) {
   return authoring.newModel().newLocale().props(body).build();
  }
  
  @PostMapping("dirents/article-workflows")
  public Uni<Model<ArticleWorkflow>> createArticleWorkflow(@RequestBody NewArticleWorkflowProps body) {
    return authoring.newModel().newArticleWorkflow().props(body).build();
  }
  
  @PostMapping("dirents/flows")
  public Uni<Model<Flow>> createFlow(@RequestBody NewFlowProps body) {
    return authoring.newModel().newFlow().props(body).build();
  }
  
  @PostMapping("dirents/flow-tasks")
  public Uni<Model<FlowTask>> createFlowTask(@RequestBody NewFlowTaskProps body) {
    return authoring.newModel().newFlowTask().props(body).build();
  }
  
  @PostMapping("dirents/decision-tables")
  public Uni<Model<DecisionTable>> createDecisionTable(@RequestBody NewDecisionTableProps body) {
    return authoring.newModel().newDecisionTable().props(body).build();
  }
  
  @PostMapping("dirents/printouts")
  public Uni<Model<Printout>> createPrintout(@RequestBody NewPrintoutProps body) {
    return authoring.newModel().newPrintout().props(body).build();
  }

  @PutMapping("dirents/printouts/{id}")
  public Uni<Model<Printout>> updatePrintout(
      @PathVariable("id") String id,
      @RequestBody ModifyPrintoutProps body)
  {
    return authoring.modifyModel().modifyPrintout().props(body).build();
  }

  @PostMapping("dirents/printout-resources")
  public Uni<Model<PrintoutResource>> createPrintoutResource(@RequestBody NewPrintoutResourceProps body) {
    return authoring.newModel().newPrintoutResource().props(body).build();
  }

  @PutMapping("dirents/printout-resources/{id}")
  public Uni<Model<PrintoutResource>> updatePrintoutResource(
      @PathVariable("id") String id,
      @RequestBody ModifyPrintoutResourceProps body)
  {
    return authoring.modifyModel().modifyPrintoutResource().props(body).build();
  }

  @PostMapping("dirents/printout-pages")
  public Uni<Model<PrintoutPage>> createPrintoutPage(@RequestBody NewPrintoutPageProps body) {
    return authoring.newModel().newPrintoutPage().props(body).build();
  }

}
