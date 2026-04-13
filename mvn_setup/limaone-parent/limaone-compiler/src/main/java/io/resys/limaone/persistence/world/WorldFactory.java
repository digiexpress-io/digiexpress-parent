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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.dialob.api.form.Form;
import io.resys.limaone.model.Article;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.ArticleTemplate;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.DialobForm;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.ImmutableDeployment;
import io.resys.limaone.model.ImmutableDialobForm;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.ImmutableModelWorld;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Tag;
import io.resys.thena.fs.spi.branch.BranchConstants;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class WorldFactory {

  private final Optional<Ref> ref;
  private final List<Tag> tags = new ArrayList<>();
  private final ImmutableModelWorld.Builder builder = ImmutableModelWorld.builder();
  private final List<FormToFind> formsToFind = new ArrayList<>();
  private final List<String> visitedForms = new ArrayList<>();
  
  record FormToFind(String formName, String tagName) {}
  
  public WorldFactory addTags(List<Tag> tags) {
    this.tags.addAll(Objects.requireNonNull(tags, () -> "tags must be defined!"));
    
    for(final var tag : tags) {
      final var deployment = ImmutableDeployment.builder()
          .fromCommitId(tag.getCommitId())
          .fromRefId(tag.getRefId().orElse(null))
          .name(tag.getTagName())
          .external(false)
          .externalId(tag.getExternalId().orElse(null))
          .cockpitId(null)
          .startsAt(tag.getTagStartsAt().orElse(null))
          
          .createdBy(tag.getTagAuthor())
          .createdAt(tag.getTagCreatedAt())
          .description(tag.getTagDescription().orElse(""))
          .status(Deployment.BundleStatus.UNKNOWN)
          .build();

      final Model<Deployment> model = ImmutableModel.<Deployment>builder()
          .id(tag.getId().toString())
          .bodyHash(tag.getCommitId().toString())
          .bodyType(BodyType.DEPLOYMENT)
          .body(deployment)
          .build();
      builder.putDeployments(model.getId(), model);
    }
    return this;
  }
  
  
  public ModelWorld build() {
    append(ref);
    return builder.build();
  }
  
  
  public Uni<ModelWorld> buildWithForms(FormDb formDb) {
    append(ref);
    return Multi.createFrom().items(formsToFind.stream())
        .onItem().transformToUni(form -> formDb.withTenant()
            .formQuery()
            .formTag(form.formName(), form.tagName())
            .findOne().onItem().transform(found -> found.map(f -> Tuple2.of(f, form))))
            .merge(10).collect().asList()
            .onItem().transform(forms -> append(forms).builder.build());
  }
  
  public static WorldFactory from(Optional<Ref> ref) {
    return new WorldFactory(Objects.requireNonNull(ref, () -> "Ref must be defined!"));
  }
  
  private WorldFactory append(List<Optional<Tuple2<Form, FormToFind>>> forms) {
    
    for(final var raw : forms) {
      if(raw.isEmpty()) {
        continue;
      }
      
      final var form = raw.get().getItem1();
      final String formId = form.getId();
      
      if(this.visitedForms.contains(formId)) {
        continue;
      }

      this.visitedForms.add(formId);      
      final var tag = raw.get().getItem2();
      
      final var body = ImmutableDialobForm.builder()
          .form(form)
          .formName(tag.formName())
          .formTagName(tag.tagName())
          .build();
      final var model = ImmutableModel.<DialobForm>builder()
        .bodyType(body.getBodyType())
        .bodyHash(form.getRev())
        .body(body)
        .id(form.getId())
        .build();
      builder.putForms(model.getId(), model);
    }
    return this;
  }
  
  
  private WorldFactory append(Optional<Ref> init) {
    if(init.isPresent()) {
      final var ref = init.get();
      builder
        .refId(ref.getId())
        .commitId(ref.getCommitId())
        .name(ref.getRefName());
    
      ref.getTransitives().getTree().getTreeNodes()
        .stream()
        .map(ProxyBlob::new)
        .filter(p -> p.getBodyType() != BodyType.UNKNOWN)
        .forEach(node -> append(node));
    } else {
      builder.name(BranchConstants.DEFAULT_BRANCH);
    }
    return this;
  }
  
  private WorldFactory append(ProxyBlob node) {
    switch(node.getBodyType()) {
      case ARTICLE: {
        final var p = node.mapTo(Article.class);
        builder.putArticles(p.getId(), p);
        return this;
      }
      case LOCALE: {
        final var p = node.mapTo(Locale.class);
        builder.putLocales(p.getId(), p);
        return this;
      }
      case ARTICLE_LINK: {
        final var p = node.mapTo(ArticleLink.class);
        builder.putArticleLinks(p.getId(), p);
        return this;
      }
      case ARTICLE_WORKFLOW: {
        final var p = node.mapTo(ArticleWorkflow.class);
        if(p.getBody().getFormName() != null && p.getBody().getFormTag() != null) {
          formsToFind.add(new FormToFind(p.getBody().getFormName(), p.getBody().getFormTag()));
        }
        builder.putArticleWorkflows(p.getId(), p);
        return this;
      }
      case ARTICLE_PAGE: {
        final var p = node.mapTo(ArticlePage.class);
        builder.putArticlePages(p.getId(), p);
        return this;
      }
      case ARTICLE_TEMPLATE: {
        final var p = node.mapTo(ArticleTemplate.class);
        builder.putArticleTemplates(p.getId(), p);
        return this;
      }
      case FLOW: {
        final var p = node.mapTo(Flow.class);
        builder.putFlows(p.getId(), p);
        return this;
      }
      case FLOW_TASK: {
        final var p = node.mapTo(FlowTask.class);
        builder.putFlowTasks(p.getId(), p);
        return this;
      }
      case DECISION_TABLE: {
        final var p = node.mapTo(DecisionTable.class);
        builder.putDecisionTables(p.getId(), p);
        return this;
      }
//        case DIALOB: {
//          final var p = node.mapTo(Dialob.class);
//          builder.putDialobs(p.getId(), p);
//          return;
//        }
//        case PRINTOUT: {
//          final var p = node.mapTo(Printout.class);
//          builder.putPrintouts(p.getId(), p);
//          return;
//        }
//        case PRINTOUT_PAGE: {
//          final var p = node.mapTo(PrintoutPage.class);
//          builder.putPrintoutPages(p.getId(), p);
//          return;
//        }
//        case PRINTOUT_SCRIPT: {
//          final var p = node.mapTo(PrintoutScript.class);
//          builder.putPrintoutScripts(p.getId(), p);
//          return;
//        }
//        case PRINTOUT_RESOURCE: {
//          final var p = node.mapTo(PrintoutResource.class);
//          builder.putPrintoutResources(p.getId(), p);
//          return;
//        }
      default: return this;
    }
  }

  
  @Getter
  private static class ProxyBlob {
    private final Blob blob;
    private final Node node;
    private final BodyType bodyType;
    
    public ProxyBlob(Node node) {
      this.blob = node.getTransitives().getBlob();
      this.node = node;
      BodyType bodyType = null;
      try {
        bodyType = BodyType.valueOf(blob.getBlobType());
      } catch(Exception e) {
        bodyType = BodyType.UNKNOWN;
        log.trace("Blob bodyType: " + blob.getBlobType() + " not supported");
      }
      this.bodyType = bodyType;
    }
    
    public <T extends Body> Model<T> mapTo(Class<T> clazz) {
      Objects.requireNonNull(bodyType, () -> "Not supported mapping of object: " + JsonObject.mapFrom(node).encodePrettily());
      Objects.requireNonNull(blob.getBlobValue(), () -> "Object not loaded: " + JsonObject.mapFrom(node).encodePrettily());
      
      return ImmutableModel.<T>builder()
          .bodyType(bodyType)
          .bodyHash(node.getBlobId().get().toString())
          .body(blob.getBlobValue().mapTo(clazz))
          .id(node.getObjectId())
          .build();
    } 
  }
}
