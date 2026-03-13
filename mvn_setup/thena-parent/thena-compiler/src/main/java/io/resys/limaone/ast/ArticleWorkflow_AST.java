package io.resys.limaone.ast;

import java.io.Serializable;
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
    AnyStatement getNext();
  }
  
  interface UserRolesStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.USER_ROLES; }    
    AnyStatement getNext();
  }
  
  interface CreateFormStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.CREATE_FORM; }    
    AwaitFormStatement getNext();
  }
  
  interface CreateTaskStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.CREATE_TASK; }
    EndStatement getNext();
  }
  
  interface AwaitFormStatement extends AnyStatement {
    default WkStatementType getType() { return WkStatementType.AWAIT_FORM; }
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
