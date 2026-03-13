package io.resys.limaone.spi.program;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.mutable.MutableBoolean;

import io.resys.limaone.ast.ArticleWorkflow_AST;
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
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.program.ImmutableCreateFormInstanceInput;
import io.resys.limaone.program.ImmutableWorkflowForm;
import io.resys.limaone.program.ImmutableWorkflowInstanceResult;
import io.resys.limaone.program.WorkflowProgram.WorkflowAssignmentProps;
import io.resys.limaone.program.WorkflowProgram.WorkflowDefaultProps;
import io.resys.limaone.program.WorkflowProgram.WorkflowExecutionStatus;
import io.resys.limaone.program.WorkflowProgram.WorkflowIdentityProps;
import io.resys.limaone.program.WorkflowProgram.WorkflowInputProps;
import io.resys.limaone.program.WorkflowProgram.WorkflowInstanceResult;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class WorkflowInstanceExecutor {
  private final static String DT_ROLE_INPUT_NAME = "role";
  private final static String DT_ROLE_OUTPUT_NAME = "processName";  
  private final static String ROLE_SPLIT = ";";  
  
  private final io.resys.limaone.program.Runtime runtime;
  private final OffsetDateTime NOW = OffsetDateTime.now();
  private final WorkflowIdentityProps identity;
  private final WorkflowInputProps programInput;
  private final List<ModelError> errors = new ArrayList<>();
  private final MutableBoolean allowAccess = new MutableBoolean(true);
  private final MutableBoolean formOk = new MutableBoolean(false);
  private final ImmutableWorkflowForm.Builder result = ImmutableWorkflowForm.builder();
  
  private final ExecutorResult REACHED_END = new ExecutorResult() {};
  private final ExecutorProps NO_PROPS = new ExecutorProps() {};

  
  interface ExecutorResult {}
  interface ExecutorProps {}
  
  
  public WorkflowInstanceResult walk(ArticleWorkflow_AST ast) {
    final var failSafeEnd = NOW.plusMonths(6);
    result.workflowName(ast.getName())
      .expiresInSeconds(ChronoUnit.SECONDS.between(NOW, failSafeEnd))
      .expiresAt(failSafeEnd);
    
    try {
      visit(ast.getStatement(), NO_PROPS);
    } catch(Exception exception) {
      this.errors.add(ImmutableModelError.builder()
          .exception(exception)
          .msg("Msg: " + exception.getMessage() + System.lineSeparator() + 
              JsonObject.mapFrom(ast).encodePrettily()
          ).build()
      );
    }
    return ImmutableWorkflowInstanceResult.builder()
        .accessAllowed(allowAccess.get())
        .form(this.formOk.isTrue() ? Optional.of(this.result.build()) : Optional.empty())
        .errors(errors)
        .status(errors.isEmpty() ? WorkflowExecutionStatus.COMPLETED : WorkflowExecutionStatus.ERROR)
        .build();
  }
  
  public ExecutorResult visitAnonStatement(AnonStatement statement, ExecutorProps props) {
    // Anonymous statement - continue to next
    if(Boolean.TRUE.equals(identity.getAnon()) && statement.getAnonAllowed()) {
      return visit(statement.getNext(), NO_PROPS);  
    }
    errors.add(ImmutableModelError.builder().msg("Workflow execution is disabled for anon user").build());
    allowAccess.setFalse();
    return REACHED_END;
  }
  
  public ExecutorResult visitDisabledStatement(DisabledStatement statement, ExecutorProps props) {
    // Disabled statement - nothing to do ... can't execute, user has disabled
    errors.add(ImmutableModelError.builder().msg("Workflow execution is disabled").build());
  
    return REACHED_END;
  }
  
  public ExecutorResult visitDevelopmentStatement(DevelopmentStatement statement, ExecutorProps props) {
    if(!runtime.getProperties().isDev()) {
      allowAccess.setFalse();
      return REACHED_END;
    }
    return visit(statement.getNext(), NO_PROPS);
  }
  
  public ExecutorResult visitInputsStatement(InputsStatement statement, ExecutorProps props) {
    // Process workflow inputs
    return visit(statement.getNext(), NO_PROPS);
  }
  
  public ExecutorResult visitLimitedTimeStatement(LimitedTimeStatement statement, ExecutorProps props) {
    // Check time constraints
    
    if (NOW.isBefore(statement.getStartDate()) || NOW.isAfter(statement.getEndDate())) {
      errors.add(ImmutableModelError.builder().msg("Workflow execution is outside allowed time window").build());
      return REACHED_END;
    }
    final var expiresInSeconds = ChronoUnit.SECONDS.between(NOW, statement.getEndDate());
    result
      .expiresInSeconds(expiresInSeconds)
      .expiresAt(statement.getEndDate());
    
    return visit(statement.getNext(), NO_PROPS);
  }
  
  public ExecutorResult visitUserRolesStatement(UserRolesStatement statement, ExecutorProps props) {
    // Process user roles via decision table
    final var dt = runtime.getBundle().queryDecisions()
        .name(statement.getDecisionTableName())
        .findOne();
    
    if(dt.isEmpty()) {
      // report as error ... be really vocal in logs on purpose
      log.error("Decision table: {} for user roles is not defined!", statement.getDecisionTableName());
      return visit(statement.getNext(), NO_PROPS);
    } 
      
    final var processNames = new ArrayList<String>();
    for(final var role : identity.getIdentityRoles()) {
      
      final var roles = dt.get()
        .run(Map.of(DT_ROLE_INPUT_NAME, role))
        .andFind().stream()
        .flatMap(row -> {
          final var outputName = row.get(DT_ROLE_OUTPUT_NAME);
          if(outputName == null) {
            return new ArrayList<String>().stream();
          }
          return Arrays.asList(outputName.toString().split(ROLE_SPLIT)).stream();
        })
        .collect(Collectors.toList());
      
      processNames.addAll(roles);
    }
    
    if(processNames.contains(statement.getRole())) {
      return visit(statement.getNext(), NO_PROPS);  
    }
  
    this.allowAccess.setFalse();
    return this.REACHED_END;
  }
  
  public ExecutorResult visitCreateFormStatement(CreateFormStatement statement, ExecutorProps props) {
    
    final var builder = ImmutableCreateFormInstanceInput.builder().locale(programInput.getLocale());
    
    builder
      .putContext("FirstNames", identity.getFirstName())
      .putContext("LastName", identity.getLastName())
      .putContext("SocialSecurityNumber", identity.getIdentity()) // same field is used for company id and ssn
      .putContext("Email", identity.getEmail())
      .putContext("Address", identity.getAddress())
      .putContext("ProtectionOrder", identity.getProtectionOrder());    
    
    if(identity.getCompanyName() != null) {
      builder
        .putContext("CompanyName", identity.getCompanyName())
        .putContext("CompanyId", identity.getIdentity());  // same field is used for company id and ssn
    }
    
    if(identity.getRepresentativeIdentity() != null) {
      builder
      .putContext("RepresentativeEnabled", true)
      .putContext("RepresentativeFirstName", identity.getRepresentativeFirstName())
      .putContext("RepresentativeLastName", identity.getRepresentativeLastName())
      .putContext("RepresentativeIdentity", identity.getRepresentativeIdentity());
    } else {
      builder.putContext("RepresentativeEnabled", false);
    }
    
    if(programInput instanceof WorkflowDefaultProps) {
      final var defaultProps = (WorkflowDefaultProps) programInput;
      if (defaultProps.getArticleName() != null) {
        builder.putContext("inputContextId", defaultProps.getArticleName());
        result.articleName(defaultProps.getArticleName());
      }
      if (defaultProps.getParentArticleName() != null) {
        builder.putContext("inputParentContextId", defaultProps.getParentArticleName());
        result.parentArticleName(defaultProps.getParentArticleName());        
      }      
    }

    final var dialob = runtime.getBundle().queryDialobs()
        .name(statement.getDependencyId())
        .getOne().run(builder.build());
    
    result
      .userLocale(programInput.getLocale())
      .userIdentity(identity.getIdentity())
      .anon(identity.getAnon())
      .tagName(runtime.getBundle().getName())
      .formName(statement.getFormName())
      .formVersion(statement.getFormTagName())
      .formSessionId(dialob.getFormSessionId())
      .assignment(programInput instanceof WorkflowAssignmentProps);
    formOk.setTrue();
    return this.visit(statement.getNext(), props);
  }
  
  public ExecutorResult visitCreateTaskStatement(CreateTaskStatement statement, ExecutorProps props) {
    result.flowName(statement.getFlowName());
    return REACHED_END;
  }
  
  public ExecutorResult visitAwaitFormStatement(AwaitFormStatement statement, ExecutorProps props) {
    return visit(statement.getNext(), props);
  }
  
  public ExecutorResult visitEndStatement(EndStatement statement, ExecutorProps props) {
    // Workflow termination
    return REACHED_END;
  }
  
  public ExecutorResult visit(AnyStatement statement, ExecutorProps props) {
    switch (statement.getType()) {
      case ANON:
        return visitAnonStatement((AnonStatement) statement, props);
      case DISABLED:
        return visitDisabledStatement((DisabledStatement) statement, props);
      case DEVELOPMENT:
        return visitDevelopmentStatement((DevelopmentStatement) statement, props);
      case INPUTS:
        return visitInputsStatement((InputsStatement) statement, props);
      case LIMITED_TIME:
        return visitLimitedTimeStatement((LimitedTimeStatement) statement, props);
      case USER_ROLES:
        return visitUserRolesStatement((UserRolesStatement) statement, props);
      case CREATE_FORM:
        return visitCreateFormStatement((CreateFormStatement) statement, props);
      case CREATE_TASK:
        return visitCreateTaskStatement((CreateTaskStatement) statement, props);
      case AWAIT_FORM:
        return visitAwaitFormStatement((AwaitFormStatement) statement, props);
      case END:
        return visitEndStatement((EndStatement) statement, props);
      default:
        throw new IllegalArgumentException("Unknown statement type: " + statement.getType());
    }
  }
}
