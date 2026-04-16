package io.resys.limaone.persistence;

import java.util.Collections;
import java.util.List;

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

import java.util.Optional;
import java.util.UUID;

import org.immutables.value.Value;

import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.authoring.CopyAny;
import io.resys.limaone.authoring.DebugAny;
import io.resys.limaone.authoring.DeleteAny;
import io.resys.limaone.authoring.DeleteArticleLink;
import io.resys.limaone.authoring.DeleteArticleWorkflow;
import io.resys.limaone.authoring.ModifyArticle;
import io.resys.limaone.authoring.ModifyArticleLink;
import io.resys.limaone.authoring.ModifyArticlePage;
import io.resys.limaone.authoring.ModifyArticleTemplate;
import io.resys.limaone.authoring.ModifyArticleWorkflow;
import io.resys.limaone.authoring.ModifyDecisionTable;
import io.resys.limaone.authoring.ModifyDeployment;
import io.resys.limaone.authoring.ModifyFlow;
import io.resys.limaone.authoring.ModifyFlowTask;
import io.resys.limaone.authoring.ModifyLocale;
import io.resys.limaone.authoring.NewArticle;
import io.resys.limaone.authoring.NewArticleLink;
import io.resys.limaone.authoring.NewArticlePage;
import io.resys.limaone.authoring.NewArticleTemplate;
import io.resys.limaone.authoring.NewArticleWorkflow;
import io.resys.limaone.authoring.NewDecisionTable;
import io.resys.limaone.authoring.NewDeployment;
import io.resys.limaone.authoring.NewFlow;
import io.resys.limaone.authoring.NewFlowTask;
import io.resys.limaone.authoring.NewLocale;
import io.resys.limaone.authoring.NewPrintout;
import io.resys.limaone.authoring.NewPrintoutPage;
import io.resys.limaone.authoring.TID;
import io.resys.limaone.persistence.world.TID_FS;
import io.resys.limaone.persistence.world.WorldDiffQueryImpl;
import io.resys.limaone.persistence.world.WorldSummaryQueryImpl;
import io.resys.limaone.program.Runtime.EnvironmentProperties;
import io.resys.thena.api.actions.TenantActions.MemberQuery;
import io.resys.thena.api.actions.TenantActions.TenantAware;
import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.Member;
import io.smallrye.mutiny.Multi;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AuthoringImpl implements Authoring {

  private final AuthoringConfig config;

  @Override
  public DefaultModel defaultModel() {
    return new DefaultModelImpl(config, false);
  }
  @Override
  public WorldQuery worldQuery() {
    return config.getPersistence().worldQuery();
  }

  @Override
  public WorldFsQuery worldFsQuery() {
    return config.getPersistence().worldFsQuery();
  }
  @Override
  public TID tid() {
    final var modelDb = config.getEnvir().getModelDb();
    final var isTenantAware = modelDb instanceof TenantAware;
    return isTenantAware ? new TID_FS(config) : new TID_NOT_SUPPORTED();
  }
  @Override
  public Authoring withBranchName(Optional<String> branchName) {
    return new AuthoringImpl(ImmutableAuthoringConfig.builder()
      .from(config)
      .persistence(config.getPersistence().withBranchName(branchName == null ? Optional.empty() : branchName))
      .build());
  }
  @Override
  public WorldIndexQuery worldIndexQuery() {
    return config.getPersistence().worldIndexQuery();
  }
  @Override
  public WorldDiffQuery worldDiffQuery() {
    return new WorldDiffQueryImpl(config);
  }
  @Override
  public WorldSummaryQuery worldSummaryQuery() {
    return new WorldSummaryQueryImpl(config);
  }
  @Override
  public WorldImport worldImport() {
    return new WorldImportImpl(config);
  }
  @Override
  public WorldRefQuery worldRefQuery() {
    return config.getPersistence().worldRefQuery();
  }
  @Override
  public DebugModel debugModel() {
    return new DebugModel() {
      @Override public DebugAny debugAny() { return new DebugAnyImpl(config); }
    };
  }
  @Override
  public CopyAsModel copyAsModel() {
    return new CopyAsModel() {
      @Override public CopyAny copyAny() { return new CopyAnyImpl(config); }
    };
  }
  @Override
  public ModifyModel modifyModel() {
    return new ModifyModel() {
      @Override public ModifyLocale modifyLocale() { return new ModifyLocaleImpl(config); }
      @Override public ModifyFlowTask modifyFlowTask() { return new ModifyFlowTaskImpl(config); }
      @Override public ModifyFlow modifyFlow() { return new ModifyFlowImpl(config); }
      @Override public ModifyDecisionTable modifyDecisionTable() { return new ModifyDecisionTableImpl(config); }
      @Override public ModifyArticleWorkflow modifyArticleWorkflow() { return new ModifyArticleWorkflowImpl(config); }
      @Override public ModifyArticleTemplate modifyArticleTemplate() { return new ModifyArticleTemplateImpl(config); }
      @Override public ModifyArticlePage modifyArticlePage() { return new ModifyArticlePageImpl(config); }
      @Override public ModifyArticleLink modifyArticleLink() { return new ModifyArticleLinkImpl(config); }
      @Override public ModifyArticle modifyArticle() { return new ModifyArticleImpl(config); }
      @Override public ModifyDeployment modifyDeployment() { return new ModifyDeploymentImpl(config); }
    };
  }

  @Override
  public DeleteModel deleteModel() {
    return new DeleteModel() {
      @Override public DeleteArticleLink deleteArticleLink() { return new DeleteArticleLinkImpl(config); }
      @Override public DeleteArticleWorkflow deleteArticleWorkflow() { return new DeleteArticleWorkflowImpl(config); }
      @Override public DeleteAny deleteAny() { return new DeleteAnyImpl(config); }
    };
  }
  
  @Override
  public NewModel newModel() {
    return new NewModel() {
      @Override public NewLocale newLocale() { return new NewLocaleImpl(config); }
      @Override public NewFlowTask newFlowTask() { return new NewFlowTaskImpl(config); }
      @Override public NewFlow newFlow() { return new NewFlowImpl(config); }
      @Override public NewDecisionTable newDecisionTable() { return new NewDecisionTableImpl(config); }
      @Override public NewArticleWorkflow newArticleWorkflow() { return new NewArticleWorkflowImpl(config); }
      @Override public NewArticleTemplate newArticleTemplate() { return new NewArticleTemplateImpl(config); }
      @Override public NewArticlePage newArticlePage() { return new NewArticlePageImpl(config); }
      @Override public NewArticleLink newArticleLink() { return new NewArticleLinkImpl(config); }
      @Override public NewArticle newArticle() { return new NewArticleImpl(config); }
      @Override public NewDeployment newDeployment() { return new NewDeploymentImpl(config); }
      @Override public NewPrintout newPrintout() { return new NewPrintoutImpl(config); }
      @Override public NewPrintoutPage newPrintoutPage() { return new NewPrintoutPageImpl(config); }
    };
  }


  @Override
  public Authoring withDefaultTenant(Optional<String> tenant) {
    return new AuthoringImpl(ImmutableAuthoringConfig.builder()
      .from(config)
      .persistence(config.getPersistence().withTenant(tenant))
      .build());
  }
  
  public static Authoring of(EnvironmentProperties envir) {
    final var modelDb = envir.getModelDb();
    return new AuthoringImpl(ImmutableAuthoringConfig.builder()
        .envir(envir)
        .persistence(modelDb)
        .build());
  }
  public AuthoringConfig getConfig() {
    return config;
  }
  
  @Value.Immutable
  public interface AuthoringConfig {
    EnvironmentProperties getEnvir();
    ModelWorldDb getPersistence();
  }
  
  
  public static class TID_NOT_SUPPORTED implements TID {

    @Override
    public AliasQuery aliasQuery() {
      return new AliasQuery() {
        @Override
        public Multi<Alias> findAll() {
          return Multi.createFrom().empty();
        }
      };
    }

    @Override
    public NewAlias newAlias() {
      throw new UnsupportedOperationException("alias not enabled for envir");
    }

    @Override
    public ModifyAlias modifyAlias() {
      throw new UnsupportedOperationException("alias not enabled for envir");
    }

    @Override
    public UpsertMember upsertMember() {
      throw new UnsupportedOperationException("alias not enabled for envir");
    }

    @Override
    public MemberQuery memberQuery() {
      return new MemberQuery() {
        @Override
        public MemberQuery refTenant(String refTenant) {
          return this;
        }
        @Override
        public Multi<Member> findAll() {
          return Multi.createFrom().empty();
        }
        @Override
        public MemberQuery externalId(String externalId) {
          return this;
        }
        @Override
        public MemberQuery aliasId(UUID aliasId) {
          return this;
        }
        @Override
        public List<Member> findAllSync() {
          return Collections.emptyList();
        }
      };
    }
    
  }
}
