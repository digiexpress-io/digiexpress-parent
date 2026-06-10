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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.authoring.ModifyArticle.ModifyArticleProps;
import io.resys.limaone.authoring.ModifyArticleLink.ModifyArticleLinkProps;
import io.resys.limaone.authoring.ModifyArticlePage.ModifyArticlePageProps;
import io.resys.limaone.authoring.ModifyArticleTemplate.ModifyArticleTemplateProps;
import io.resys.limaone.authoring.ModifyArticleWorkflow.ModifyArticleWorkflowProps;
import io.resys.limaone.authoring.ModifyLocale.ModifyLocaleProps;
import io.resys.limaone.authoring.NewArticle.NewArticleProps;
import io.resys.limaone.authoring.NewArticleLink.NewArticleLinkProps;
import io.resys.limaone.authoring.NewArticlePage.NewArticlePageProps;
import io.resys.limaone.authoring.NewArticleTemplate.NewArticleTemplateProps;
import io.resys.limaone.authoring.NewArticleWorkflow.NewArticleWorkflowProps;
import io.resys.limaone.authoring.NewLocale.NewLocaleProps;
import io.resys.limaone.model.Article;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.ArticleTemplate;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Model.ModelWorldIndex;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/worker/rest/api/assets/stencil")
@Slf4j
@RequiredArgsConstructor
public class AssetsStencilController {
  
  private final Authoring composer; 
  
  
  @GetMapping("/")
  public Uni<ModelWorld> root() {
    return getSites();
  }
  @PostMapping("/sites") 
  public Uni<ModelWorld> createSites() {
    return getSites();
  }
  @GetMapping("/sites")
  public Uni<ModelWorld> getSites() {
    return composer.worldQuery()
      .docs(
        BodyType.LOCALE,
        BodyType.ARTICLE,
        BodyType.ARTICLE_LINK,
        BodyType.ARTICLE_WORKFLOW,
        BodyType.ARTICLE_PAGE,
        BodyType.ARTICLE_TEMPLATE
      )
      .findAll();
  }
  @GetMapping(path = "/commitlogs", produces = MediaType.APPLICATION_JSON_VALUE)
  public Multi<ModelWorldIndex> commitlogs() {
    return composer.worldIndexQuery().findAll();
  }
  
  @PostMapping("/articles")
  public Uni<Model<Article>> createArticle(@RequestBody NewArticleProps body) {
    return composer.newModel().newArticle().props(body).build();
  }
  @PutMapping("/articles") 
  public Uni<Model<Article>> updateArticle(@RequestBody ModifyArticleProps body) {
    return composer.modifyModel().modifyArticle().props(body).build();
  }
  @DeleteMapping("/articles/{id}") 
  public Uni<Model<?>> deleteArticle(@PathVariable("id") String id) {
    return composer.deleteModel().deleteAny()
        .props(props -> props.bodyType(BodyType.ARTICLE).id(id)).build();
  }
  @PostMapping("/links") 
  public Uni<Model<ArticleLink>> createLink(@RequestBody NewArticleLinkProps body) {
    return composer.newModel().newArticleLink().props(body).build();
  }
  @PutMapping("/links") 
  public Uni<Model<ArticleLink>> updateLink(@RequestBody ModifyArticleLinkProps body) {
    return composer.modifyModel().modifyArticleLink().props(body).build();
  }
  @DeleteMapping("/links/{id}") 
  public Uni<Model<?>> deleteLink(@PathVariable("id") String linkId, @RequestParam(name = "articleId", required = false) String articleId) {
    if(StringUtils.isNotEmpty(articleId)) {
      return composer.deleteModel()
          .deleteArticleLink().props(props -> props.articleId(articleId).linkId(linkId)).build()
          .map(model -> model);
    } 
    return composer.deleteModel().deleteAny()
        .props(props -> props.bodyType(BodyType.ARTICLE_LINK).id(linkId)).build();

  }
  @PostMapping("/workflows") 
  public Uni<Model<ArticleWorkflow>> createWorkflow(@RequestBody NewArticleWorkflowProps body) {
    return composer.newModel().newArticleWorkflow().props(body).build();
  }
  @PutMapping("/workflows") 
  public Uni<Model<ArticleWorkflow>> updateWorkflow(@RequestBody ModifyArticleWorkflowProps body) {
    return composer.modifyModel().modifyArticleWorkflow().props(body).build();
  }
  @DeleteMapping("/workflows/{id}") 
  public Uni<Model<?>> deleteWorkflow(
      @PathVariable("id") String linkId,  
      @RequestParam(name = "articleId", required = false) String articleId) {
    
    if(StringUtils.isNotEmpty(articleId)) {
      return composer.deleteModel().deleteArticleWorkflow()
          .props(props -> props.articleId(articleId).workflowId(linkId)).build()
          .map(model -> model);
    } 
    return composer.deleteModel().deleteAny()
        .props(props -> props.bodyType(BodyType.ARTICLE_WORKFLOW).id(linkId)).build();

  }
  @PostMapping("/locales") 
  public Uni<Model<Locale>> createLocale(@RequestBody NewLocaleProps body) {
    return composer.newModel().newLocale().props(body).build();
  }
  @PutMapping("/locales") 
  public Uni<Model<Locale>> updateLocale(@RequestBody ModifyLocaleProps body) {
    return composer.modifyModel().modifyLocale().props(body).build();
  }
  @DeleteMapping("/locales/{id}") 
  public Uni<Model<?>> deleteLocale(@PathVariable("id") String id) {
    return composer.deleteModel().deleteAny()
        .props(builder -> builder.bodyType(BodyType.LOCALE).id(id)).build();
  }
  @PostMapping("/pages") 
  public Uni<Model<ArticlePage>> createPage(@RequestBody NewArticlePageProps body) {
    return composer.newModel().newArticlePage().props(body).build();
  }
  @PutMapping("/pages") 
  public Uni<List<Model<ArticlePage>>> updatePage(@RequestBody List<ModifyArticlePageProps> body) {
    return composer.modifyModel().modifyArticlePage().props(body).buildAll();
  }
  @DeleteMapping("/pages/{id}") 
  public Uni<Model<?>> deletePage(@PathVariable("id") String id) {
    return composer.deleteModel().deleteAny()
        .props(builder -> builder.bodyType(BodyType.ARTICLE_PAGE).id(id)).build();
  }
  @PostMapping("/templates") 
  public Uni<Model<ArticleTemplate>> createTemplate(@RequestBody NewArticleTemplateProps body) {
    return composer.newModel().newArticleTemplate().props(body).build();
  }
  @PutMapping("/templates") 
  public Uni<Model<ArticleTemplate>> updateTemplate(@RequestBody ModifyArticleTemplateProps body) {
    return composer.modifyModel().modifyArticleTemplate().props(body).build();
  }
  @DeleteMapping("/templates/{id}") 
  public Uni<Model<?>> deleteTemplate(@PathVariable("id") String id) {
    return composer.deleteModel().deleteAny()
        .props(builder -> builder.bodyType(BodyType.ARTICLE_TEMPLATE).id(id)).build();
  }
}
