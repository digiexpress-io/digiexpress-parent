package io.resys.limaone.spi.compiler.article;

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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.resys.limaone.ast.Article_AST;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ArticleProgram;
import io.resys.limaone.program.ImmutableArticleProgramResult;
import io.resys.limaone.program.ImmutableAuthProgramResult;
import io.resys.limaone.spi.ast.ArticleWorkflowVisitor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArticleProgramImpl implements ArticleProgram {

  private static final long serialVersionUID = -5191441381711565812L;
  private final List<OffsetDateTime> refreshDates;
  private final io.resys.limaone.program.Runtime runtime;
  private final String id;
  private final Article_AST articleAST;
  private final ProgramStatus status;
  private final List<ModelError> errors;
  private final List<ProgramAssociation> assocs;
  private final List<LocalizedSite> sites;
  
  public ArticleProgramImpl(
      io.resys.limaone.program.Runtime runtime,
      String id,
      Article_AST articleAST, 
      ProgramStatus status,
      List<ModelError> errors, 
      List<ProgramAssociation> assocs,
      List<LocalizedSite> sites) {
    
    super();
    this.id = id;
    this.runtime = runtime;
    this.articleAST = articleAST;
    this.status = status;
    this.errors = errors;
    this.assocs = assocs;
    this.sites = sites;
    
    final var refreshDates = new ArrayList<OffsetDateTime>();
    for(final var site : sites) {
      for(final var date : site.getRefreshDates()) {
        if(!refreshDates.contains(date)) {
          refreshDates.add(date);
        }
      }
    }
    this.refreshDates = Collections.unmodifiableList(refreshDates);
  }
  
  @Override
  public String getId() {
    return id;
  }
  @Override
  public String getName() {
    return articleAST.getName();
  }
  @Override
  public BodyType getType() {
    return BodyType.ARTICLE;
  }
  @Override
  public ProgramStatus getStatus() {
    return status;
  }
  @Override
  public List<Parameter> getHeaders() {
    return Collections.emptyList();
  }
  @Override
  public List<ModelError> getErrors() {
    return errors;
  }
  @Override
  public List<ProgramAssociation> getAssociations() {
    return assocs;
  }
  @Override
  public List<OffsetDateTime> getRefreshDates() {
    return refreshDates;
  }
  @Override
  public List<String> getLocales() {
    return sites.stream().map(site -> site.getLocale()).toList();
  }

  @Override
  public ArticleProgramResult run(ArticleProgramInput input) {
    return ImmutableArticleProgramResult.builder()
        .putAllSites(sites.stream().collect(Collectors.toMap(e -> e.getLocale(), e -> e)))
        .build();
  }

  @Override
  public AuthProgramResult run(AuthProgramInput input) {

    
    final var dt = runtime.getBundle().queryDecisions()
        .name(ArticleWorkflowVisitor.DT_AUTH_NAME)
        .findOne();
    
    if(dt.isEmpty() || StringUtils.isBlank(input.getParticipant().getRepresentativeIdentity())) {
      // report as error ... be really vocal in logs on purpose
      log.error("Decision table: {} for user roles is not defined!", ArticleWorkflowVisitor.DT_AUTH_NAME);
      return ImmutableAuthProgramResult.builder()
          .status(AuthStatus.NOT_USED)
          .build();
    } 
    
    final var processNames = new ArrayList<String>();
    for(final var role : input.getParticipant().getIdentityRoles()) {
      final var roles = dt.get()
        .run(Map.of(ArticleWorkflowVisitor.DT_ROLE_INPUT_NAME, role))
        .andFind().stream()
        .flatMap(row -> {
          final var outputName = row.get(ArticleWorkflowVisitor.DT_ROLE_OUTPUT_NAME);
          if(outputName == null) {
            return new ArrayList<String>().stream();
          }
          return Arrays.asList(outputName.toString().split(ArticleWorkflowVisitor.ROLE_SPLIT)).stream();
        })
        .collect(Collectors.toList());
      
      processNames.addAll(roles);
    }
    
    return ImmutableAuthProgramResult.builder()
        .roles(input.getParticipant().getIdentityRoles())
        .allowed(processNames)
        .build();
  }
}
