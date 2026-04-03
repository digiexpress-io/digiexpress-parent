package io.resys.limaone.ast;

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

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.ast.AST_Parser.Dependency_AST;


@Value.Immutable
@JsonSerialize(as = ImmutableArticleWorkflow_AST.class)
@JsonDeserialize(as = ImmutableArticleWorkflow_AST.class)
public interface ArticleWorkflow_AST extends Simple_AST {
  List<Dependency_AST> getDependencies();
  AnyStatement getStatement();
  
  //root marker
  interface AnyStatement extends Serializable {
    WkStatementType getType();
  }
  
  interface AnonStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.ANON; }
    Boolean getAnonAllowed();
    AnyStatement getNext();
  }
  
  interface DisabledStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.DISABLED; }
    AnyStatement getNext();
  }
  interface DevelopmentStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.DEVELOPMENT; }
    AnyStatement getNext();
  }
  interface InputsStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.INPUTS; }
    AnyStatement getNext();
  }
  
  interface LimitedTimeStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.LIMITED_TIME; }
    OffsetDateTime getStartDate();
    OffsetDateTime getEndDate();
    AnyStatement getNext();
  }
  
  interface UserRolesStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.USER_ROLES; }    
    String getDecisionTableName();
    String getRole();
    AnyStatement getNext();
  }
  
  interface CreateFormStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.CREATE_FORM; }   
    String getDependencyId();
    String getFormName();
    String getFormTagName();
    AwaitFormStatement getNext();
  }
  
  interface CreateTaskStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.CREATE_TASK; }
    String getFlowName();
    EndStatement getNext();
  }
  
  interface AwaitFormStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.AWAIT_FORM; }
    AnyStatement getNext();
  }

  interface EndStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.END; }
  }
  
  enum WkStatementType {
    ANON,
    DEVELOPMENT,
    DISABLED,
    INPUTS,
    LIMITED_TIME,
    USER_ROLES,
    
    CREATE_FORM,
    CREATE_TASK,
    
    AWAIT_FORM,
    END
  }
}
