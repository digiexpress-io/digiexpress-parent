package io.resys.limaone.fs;

import java.util.List;
import java.util.Map;

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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.ast.FlowTask_AST;
import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.DecisionTable.DecisionStatement;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.program.Program.ProgramAssociation;
import io.resys.limaone.program.Program.ProgramStatus;
import jakarta.annotation.Nullable;


public interface WorldFsBody {
  
  @Value.Immutable @JsonSerialize(as = ImmutableArticlePageBody.class) @JsonDeserialize(as = ImmutableArticlePageBody.class)
  interface ArticlePageBody extends WorldFsBody {
    String getContent();
  }
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableWrenchBody.class) @JsonDeserialize(as = ImmutableWrenchBody.class)
  interface WrenchBody extends WorldFsBody {
    Map<String, WrenchAstBody<Flow_AST>> getFlows();
    Map<String, WrenchAstBody<FlowTask_AST>> getServices();
    Map<String, WrenchAstBody<DecisionTable_AST>> getDecisions();
    
    default WrenchAstBody<?> getEntity(String id) {
      if(this.getDecisions().containsKey(id)) {
        return this.getDecisions().get(id); 
      } else if(this.getFlows().containsKey(id)) {
        return this.getFlows().get(id); 
      }
      return this.getServices().get(id);
    }
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableWrenchAstBody.class) @JsonDeserialize(as = ImmutableWrenchAstBody.class)
  interface WrenchAstBody<A extends Simple_AST>  extends WorldFsBody {
    String getId();
    @Nullable A getAst();
    List<DecisionStatement> getCommands();
    List<ModelError> getErrors();
    List<ProgramAssociation> getAssociations();
    ProgramStatus getStatus();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableWrenchAstBodyChange.class) @JsonDeserialize(as = ImmutableWrenchAstBodyChange.class)
  interface WrenchAstBodyChange {
    String getId();
    Model.BodyType getBodyType();
    @Nullable String getBodySyntax();
    List<DecisionStatement> getBodyStatment();
  }
}