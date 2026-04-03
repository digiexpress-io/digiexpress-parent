package io.resys.limaone.spi.ast;

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

import io.resys.limaone.ast.ArticleWorkflow_AST.AnonStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.AnyStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.AwaitFormStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.CreateFormStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.CreateTaskStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.DevelopmentStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.DisabledStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.EndStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.InputsStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.LimitedTimeStatement;
import io.resys.limaone.ast.ArticleWorkflow_AST.UserRolesStatement;
import lombok.Getter;

public class WkStatementFactory {

  @Getter
  public static class ImmutableAnonStatement implements AnonStatement {
    private static final long serialVersionUID = -1L;
    private final AnyStatement next;
    private final Boolean anonAllowed;
    
    public ImmutableAnonStatement(AnyStatement next, Boolean anonAllowed) {
      super();
      this.next = next;
      this.anonAllowed = anonAllowed;
    }
  }

  @Getter
  public static class ImmutableDisabledStatement implements DisabledStatement {
    private static final long serialVersionUID = -2L;
    private final AnyStatement next;
    
    public ImmutableDisabledStatement(AnyStatement next) {
      super();
      this.next = next;
    }
  }

  @Getter
  public static class ImmutableDevelopmentStatement implements DevelopmentStatement {
    private static final long serialVersionUID = -3L;
    private final AnyStatement next;
    
    public ImmutableDevelopmentStatement(AnyStatement next) {
      super();
      this.next = next;
    }
  }

  @Getter
  public static class ImmutableInputsStatement implements InputsStatement {
    private static final long serialVersionUID = -4L;
    private final AnyStatement next;
    
    public ImmutableInputsStatement(AnyStatement next) {
      super();
      this.next = next;
    }
  }

  @Getter
  public static class ImmutableLimitedTimeStatement implements LimitedTimeStatement {
    private static final long serialVersionUID = -5L;
    
    private final OffsetDateTime startDate;
    private final OffsetDateTime endDate;
    private final AnyStatement next;
    
    public ImmutableLimitedTimeStatement(
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        AnyStatement next) {
      super();
      this.next = next;
      this.startDate = startDate;
      this.endDate = endDate;
    }
  }

  @Getter
  public static class ImmutableUserRolesStatement implements UserRolesStatement {
    private static final long serialVersionUID = -6L;
    private final AnyStatement next;
    private final String decisionTableName;
    private final String role;
    public ImmutableUserRolesStatement(String decisionTableName, String role, AnyStatement next) {
      super();
      this.next = next;
      this.decisionTableName = decisionTableName;
      this.role = role;
    }
  }

  @Getter
  public static class ImmutableCreateFormStatement implements CreateFormStatement {
    private static final long serialVersionUID = -7L;
    private final AwaitFormStatement next;
    private final String dependencyId;
    private final String formName;
    private final String formTagName;
    
    public ImmutableCreateFormStatement(String dependencyId, String formName, String formTagName, AwaitFormStatement next) {
      super();
      this.next = next;
      this.dependencyId = dependencyId;
      this.formName = formName;
      this.formTagName = formTagName;
    }
  }

  @Getter
  public static class ImmutableCreateTaskStatement implements CreateTaskStatement {
    private static final long serialVersionUID = -8L;
    private final String flowName;
    private final EndStatement next;
    
    public ImmutableCreateTaskStatement(String flowName, EndStatement next) {
      super();
      this.flowName = flowName;
      this.next = next;
    }
  }

  @Getter
  public static class ImmutableAwaitFormStatement implements AwaitFormStatement {
    private static final long serialVersionUID = -9L;
    private final AnyStatement next;
    
    public ImmutableAwaitFormStatement(AnyStatement next) {
      super();
      this.next = next;
    }
  }

  @Getter
  public static class ImmutableEndStatement implements EndStatement {
    private static final long serialVersionUID = -10L;
    private static final ImmutableEndStatement INSTANCE = new ImmutableEndStatement();
    
    private ImmutableEndStatement() {
      super();
    }
    
    public static ImmutableEndStatement getInstance() {
      return INSTANCE;
    }
  }
}
