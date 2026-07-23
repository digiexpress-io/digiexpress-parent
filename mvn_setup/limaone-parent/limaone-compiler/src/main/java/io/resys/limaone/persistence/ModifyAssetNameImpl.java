package io.resys.limaone.persistence;

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

import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableModifyNameProps;
import io.resys.limaone.authoring.ModifyAssetName;
import io.resys.limaone.model.Article;
import io.resys.limaone.model.ArticleTemplate;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.DecisionTable.StatementType;
import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.DialobForm;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.ImmutableArticle;
import io.resys.limaone.model.ImmutableArticleTemplate;
import io.resys.limaone.model.ImmutableArticleWorkflow;
import io.resys.limaone.model.ImmutableDecisionStatement;
import io.resys.limaone.model.ImmutableDecisionTable;
import io.resys.limaone.model.ImmutableDeployment;
import io.resys.limaone.model.ImmutableDialobForm;
import io.resys.limaone.model.ImmutableFlow;
import io.resys.limaone.model.ImmutableFlowTask;
import io.resys.limaone.model.ImmutablePrintout;
import io.resys.limaone.model.ImmutablePrintoutResource;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Printout;
import io.resys.limaone.model.PrintoutResource;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.thena.support.RepoAssert;
import io.resys.thena.support.RepoAssert.RepoAssertException;
import io.smallrye.mutiny.Uni;


public class ModifyAssetNameImpl extends AuthoringTemplate<ModifyAssetNameImpl, Model<?>> implements ModifyAssetName {

  private ModifyNameProps props;

  public ModifyAssetNameImpl(AuthoringConfig config) {
    super(config);
  }

  @Override
  public ModifyAssetNameImpl props(ModifyNameProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyAssetNameImpl props(Consumer<ImmutableModifyNameProps.Builder> props) {
    final var builder = ImmutableModifyNameProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<?>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docsId(props.getId())
      .build(nextWorld -> {
        
        final var model = nextWorld.getCurrentWorld().findAnyObject(props.getId());
        RepoAssert.isTrue(model.isPresent(), () -> "model must be loaded to rename it!");
        final var nextState = buildNextState(model.get(), props.getName());
        
        return nextWorld.mergeModel(props.getId(), props.getName(), nextState);
      });
  }
  
  @SuppressWarnings("unchecked")
  public static Model.Body buildNextState(Model<?> model, String name) {
    
    final var bodyType = model.getBodyType();
    switch (bodyType) {
    case FLOW: {
      final Model<Flow> start = (Model<Flow>) model;
      final var flowValue = start.getBody().getFlowValue();
      
      return ImmutableFlow.builder()
          .from(start.getBody())
          .flowName(name)
          .flowValue(flowValue.replace("id: " + start.getBody().getFlowName(), "id: " + name))
          .build();
    }
    
    case ARTICLE: {
      final Model<Article> start = (Model<Article>) model;
      return ImmutableArticle.builder()
          .from(start.getBody())
          .name(name)
          .build();
    }

    case ARTICLE_TEMPLATE: {
      final Model<ArticleTemplate> start = (Model<ArticleTemplate>) model;
      return ImmutableArticleTemplate.builder()
          .from(start.getBody())
          .name(name)
          .build();
    }
    
    case ARTICLE_WORKFLOW: {
      final Model<ArticleWorkflow> start = (Model<ArticleWorkflow>) model;
      return ImmutableArticleWorkflow.builder()
          .from(start.getBody())
          .flowName(name)
          .build();
    }
    
    case PRINTOUT: {
      final Model<Printout> start = (Model<Printout>) model;
      return ImmutablePrintout.builder()
          .from(start.getBody())
          .serviceName(name)
          .build();
    }
    
    case PRINTOUT_RESOURCE: {
      final Model<PrintoutResource> start = (Model<PrintoutResource>) model;
      return ImmutablePrintoutResource.builder()
          .from(start.getBody())
          .resourceName(name)
          .build();
    }
    
    case DECISION_TABLE: {
      final Model<DecisionTable> start = (Model<DecisionTable>) model;
      final var next = ImmutableDecisionTable.builder().name(name);
      for(final var node : start.getBody().getNodes()) {
        if(node.getType() == StatementType.SET_NAME) {
          next.addNodes(ImmutableDecisionStatement.builder()
              .from(node)
              .value(name)
              .build());
        } else {
          next.addNodes(node);        
        }
      }
      
      return next.build();
    }
    
    case FLOW_TASK: {
      final Model<FlowTask> start = (Model<FlowTask>) model;
      final var taskValue = start.getBody().getTaskValue();

      return ImmutableFlowTask.builder()
          .from(start.getBody())
          .taskName(name)
          .taskValue(taskValue.replace("public class " + start.getBody().getTaskName(), "public class " + name))
          .build();
    }
    
    case DEPLOYMENT: {
      final Model<Deployment> start = (Model<Deployment>) model;
      return ImmutableDeployment.builder()
          .from(start.getBody())
          .name(name)
          .build();
    }
    
    // Not supported at the moment
    case DIALOB_FORM: {
      final Model<DialobForm> start = (Model<DialobForm>) model;
      return ImmutableDialobForm.builder()
          .from(start.getBody())
          .formName(name)
          .build();
    }

    
    case DIALOB_FORM_META:
    case PRINTOUT_PAGE:
    case LOCALE:
    case FOLDER:
    case ARTICLE_LINK:
    case ARTICLE_PAGE:
    case UNKNOWN: 
    default: throw new RepoAssertException("Unsupported renaming of: " + bodyType + "!"); 
    }
  }
}
