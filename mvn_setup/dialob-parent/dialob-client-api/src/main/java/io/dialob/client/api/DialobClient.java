package io.dialob.client.api;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.dialob.api.form.Form;
import io.dialob.api.form.FormValidationError;
import io.dialob.api.proto.Actions;
import io.dialob.api.proto.ValueSet;
import io.dialob.api.questionnaire.Answer;
import io.dialob.api.questionnaire.ContextValue;
import io.dialob.api.questionnaire.Questionnaire;
import io.dialob.client.api.DialobErrorHandler.DocumentNotFoundException;
import io.dialob.program.DialobProgram;

public interface DialobClient {
  
  //origin-dialob-session-boot all tests
  ProgramBuilder program();
  QuestionnaireExecutorBuilder executor(ProgramEnvir envir);   
  EnvirBuilder envir();

  DialobClientConfig getConfig();  
  
  
  interface QuestionnaireExecutorBuilder {
    QuestionnaireExecutor create(String id, String rev, Consumer<QuestionnaireInit> initWith) throws DocumentNotFoundException ;
    QuestionnaireExecutor create(String id, Consumer<QuestionnaireInit> initWith) throws DocumentNotFoundException ;
    QuestionnaireExecutor restore(Questionnaire queestionnaire) throws DocumentNotFoundException ;
  }
  
  interface QuestionnaireInit {
    QuestionnaireInit activeItem(String activeItem); // TODO:: not needed
    QuestionnaireInit status(Questionnaire.Metadata.Status status); // TODO:: always new, NEW - NEW DOC, user not touched, OPEN - user has started
    QuestionnaireInit valueSets(List<ValueSet> valueSets); // TODO:: not needed
    QuestionnaireInit questionnaire(Questionnaire questionnaire); // TODO:: required why?
    QuestionnaireInit submitUrl(String submitUrl); //TODO:: classifier on submit/ not really needed
    
    QuestionnaireInit id(String id);
    QuestionnaireInit rev(String rev);
    QuestionnaireInit creator(String owner);
    QuestionnaireInit owner(String owner);
    QuestionnaireInit language(String language); //Optional
    QuestionnaireInit additionalProperties(Map<String, Object> additionalProperties); //Optional
    QuestionnaireInit contextValues(List<ContextValue> contextValues); //Optional
    QuestionnaireInit answers(List<Answer> answers); //Optional
  }
  
  interface QuestionnaireExecutor {
    // TODO:: only in tests false, everywhere else true???
    QuestionnaireExecutor createOnly(boolean createOnly);
    QuestionnaireExecutor actions(Actions actions);
    Actions execute();
    ExecutorBody executeAndGetBody();
    QuestionnaireSession toSession();
  }
  
  @Value.Immutable
  interface ExecutorBody {
    Actions getActions();
    QuestionnaireSession getSession();
    default Questionnaire getQuestionnaire() { return getSession().getQuestionnaire(); }
  }
  
  interface ProgramBuilder {
    ProgramBuilder form(Form form);
    DialobProgram build();
  }
  
  interface EnvirBuilder {
    EnvirBuilder from(ProgramEnvir envir);
    EnvirCommandFormatBuilder addCommand();
    ProgramEnvir build();
  }

  
  interface EnvirCommandFormatBuilder {
    EnvirCommandFormatBuilder id(String externalId);
    EnvirCommandFormatBuilder version(String version);
    
    EnvirCommandFormatBuilder cachless();
    EnvirCommandFormatBuilder form(String json);
    EnvirCommandFormatBuilder form(InputStream json);
    EnvirCommandFormatBuilder form(Form entity);    
    
    EnvirBuilder build();
  }

  
  
  interface ProgramEnvir {
    ProgramWrapper findByFormId(String formId)  throws DocumentNotFoundException; 
    ProgramWrapper findByFormIdAndRev(String formId, String formRev)  throws DocumentNotFoundException;
    List<ProgramWrapper> findAll();
    
    Map<String, ProgramWrapper> getValues();
  }
  
  

  
  @Value.Immutable
  interface ProgramWrapper {
    String getId();
    ProgramStatus getStatus();
    Form getDocument();
    
    List<ProgramMessage> getErrors();
    @JsonIgnore
    Optional<DialobProgram> getProgram();
  } 
  
  @Value.Immutable
  interface ProgramMessage {
    String getId();
    
    @Nullable
    String getMsg();
    
    @Nullable
    FormValidationError getSrc();
    @JsonIgnore
    @Nullable
    Exception getException();
  }
  
  enum ProgramStatus { 
    UP, 
    AST_ERROR, 
    PROGRAM_ERROR, 
    DEPENDENCY_ERROR }
}
