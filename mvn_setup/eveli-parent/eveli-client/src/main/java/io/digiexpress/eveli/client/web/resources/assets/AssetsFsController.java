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
import io.resys.limaone.fs.WorldFs;
import io.resys.limaone.fs.WorldFsBody;
import io.resys.limaone.fs.WorldFsBody.WrenchAstBodyChange;
import io.resys.limaone.model.Article;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
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
}
